/*
 * Created on May 28, 2008 by wyatt
 */
package org.homeunix.thecave.moss.jsp.gallery;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.homeunix.thecave.moss.common.StreamUtil;
import org.homeunix.thecave.moss.image.ImageFunctions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

public class ImageFilter implements Filter {
	final static String JAVASCRIPT_PATH = "/js";
	final static String CSS_PATH = "/css";
	final static String IMAGE_PATH = "/images";
	final static String GALLERIES_PATH = "/galleries";
	
	private FilterConfig config;
	private Logger logger = Logger.getLogger(ImageFilter.class.getName());
	
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
		
		//Built in Lightbox components
		for (String path : new String[]{JAVASCRIPT_PATH, CSS_PATH, IMAGE_PATH}) {
			if (((HttpServletRequest) req).getRequestURI().matches(config.getServletContext().getContextPath() + path + "/.*\\.[a-zA-Z]{2,3}")){
				String name = ((HttpServletRequest) req).getRequestURI().replaceAll(config.getServletContext().getContextPath() + path + "/", "");
				InputStream is = ImageFilter.class.getResourceAsStream("resources/" + name);
				if (is != null){
					StreamUtil.copyStream(is, res.getOutputStream());
					return;
				}
			}
		}
		
		//Gallery images - located under /galleries/<packageName>/filename
		if (((HttpServletRequest) req).getRequestURI().matches(config.getServletContext().getContextPath() + GALLERIES_PATH + "/.+" )){
			StreamUtil.copyStream(getImageInputStream(((HttpServletRequest) req).getRequestURI(), config), res.getOutputStream());
			return;
		}
		
		chain.doFilter(req, res);
	}

	/**
	 * Given a request URI, returns the image associated with the request.
	 * The request URI must be in the form:
	 *  /galleries/package/name/baseimage_ext_size_quality.jpg
	 * where package/name is the path to the source gallery, relative
	 * to WEB-INF/galleries, basename is the image name, ext is the original 
	 * image extension, size is the size of the scaled version, and quality 
	 * is the quality of the scaled version (100 is full quality (largest size),
	 * 0 is lowest quality (smallest image). 
	 * @param requestURI
	 * @param context File referencing the root of the webapp.  If null, we will look 
	 * for images in the classpath, and cache images in memory only. 
	 * @return
	 */
	private InputStream getImageInputStream(String requestURI, FilterConfig config){
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");
		String requestUriFile = requestUriWithoutContextAndGalleriesPrefix.replaceAll("^/.*/", "").replaceAll("\\.jpg$", "");

		String[] split = requestUriFile.split("_");
		if (split.length < 4)
			return new ByteArrayInputStream(new byte[0]);

		String qualityString = split[split.length - 1];
		String sizeAndTypeString = split[split.length - 2];
		String sizeType = sizeAndTypeString.replaceAll("[^a-z]", ""); //Size type will determine what sort of sizing algorithm to use.
		String sizeString = sizeAndTypeString.replaceAll("[^0-9]", "");
		int size = Integer.parseInt(sizeString);
		int quality = Integer.parseInt(qualityString);
		String ext = split[split.length - 3];
		String baseName = requestUriFile.replaceAll("_" + ext + "_" + sizeAndTypeString + "_" + qualityString, "");

		//Check the gallery for gallery-specific overrides on max / min size and quality.
		InputStream settings = config.getServletContext().getResourceAsStream("/WEB-INF" + GALLERIES_PATH + packageName + "/settings.xml");
		if (settings != null){
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);
				
				Integer sizeMax = getSettings(doc, "size", "max");
				Integer sizeMin = getSettings(doc, "size", "min");
				Integer qualityMax = getSettings(doc, "quality", "max");
				Integer qualityMin = getSettings(doc, "quality", "min");
				
				if (sizeMax != null && (size > sizeMax || size == 0))
					size = sizeMax;
				if (sizeMin != null && size < sizeMin && size != 0)
					size = sizeMin;
				if (qualityMax != null && quality > qualityMax)
					quality = qualityMax;
				if (qualityMin != null && quality < qualityMin)
					quality = qualityMin;
			}
			catch (Exception e){
				logger.log(Level.WARNING, "Error while opening settings.xml for gallery package " + packageName, e);
			}
		}
		
		//Check for sane values on size / quality
		if (size < 10 && size != 0)
			size = 10; //Size = 0 means full size; if they set it less than 0, we don't want to give them massive images.
		if (size > 4000)
			size = 4000;
		if (quality > 100)
			quality = 100;
		if (quality < 0)
			quality = 0;
		
		//We load source images from the context path
		InputStream is = config.getServletContext().getResourceAsStream(("/WEB-INF" + GALLERIES_PATH + packageName + baseName + "." + ext).replaceAll("%20", " "));

		if (is == null)
			throw new RuntimeException("Could not find image");

		return new ByteArrayInputStream(convertImage(is, size, sizeType, quality));
	}
	
	private Integer getSettings(Document doc, String element, String attribute){
		if (doc == null)
			return null;
		NodeList children = doc.getElementsByTagName(element);
		if (children.getLength() == 0)
			return null;
		Node child = children.item(0);
		Node attrNode = child.getAttributes().getNamedItem(attribute);
		if (attrNode == null)
			return null;
		try {
			return Integer.parseInt(attrNode.getTextContent());
		}
		catch (NumberFormatException nfe){
			return null;
		}
	}

	/**
	 * Returns a byte array containing the converted image, at the requested
	 * size and quality.  The input stream points to the source image. 
	 * @param is
	 * @param size
	 * @param quality
	 * @return
	 */
	private byte[] convertImage(InputStream is, int size, String sizeType, int quality){
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			bis.mark(bis.available() + 1);

			BufferedImage bi = ImageIO.read(bis);
			bis.reset();

			//Metadata will only work for .jpg images; ignore exceptions.
			Metadata metadata = null;
			try {
				metadata = JpegMetadataReader.readMetadata(bis);
			}
			catch (JpegProcessingException jpe) {}

			//String title = null;
			int rotationDegrees = 0;

			//Try to load metadata
			if (metadata != null){
				//Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
				//title = iptcDirectory.getString(IptcDirectory.TAG_HEADLINE);

				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				try {
					if (exifDirectory.containsTag(ExifDirectory.TAG_ORIENTATION)){
						int orientation = exifDirectory.getInt(ExifDirectory.TAG_ORIENTATION);
						switch (orientation) {
						case 3:
							rotationDegrees = 180;
							break;
						case 6:
							rotationDegrees = 90;
							break;
						case 8:
							rotationDegrees = 270;
							break;

						default:
							rotationDegrees = 0;
						break;
						}
					}
				} 
				catch (MetadataException me){} //No big deal if metadata is not set
			}

			double w = bi.getWidth();
			double h = bi.getHeight();
			
			if ("w".equals(sizeType)){
				if (h > w)
					size = (int) ((size / h) * w); 
			}
			else if ("h".equals(sizeType)){
				if (w > h)
					size = (int) ((size / h) * w); 				
			}
			else {
				//We calculate the size of the new image based off the hypotenuse,
				// scaled such that longer / thin images (those with a higher width 
				// to height ratio / height to width ratio, depending on which is larger)
				// get scaled up more than square images.
				double angle = Math.atan(h / w);

				double newHeight = size * Math.sin(angle);
				double newWidth = size * Math.cos(angle);

				double scaleFactor = 1 + Math.log10((Math.max(w, h) / Math.min(w, h)));

				size = (int) (Math.max(newWidth, newHeight) * scaleFactor);
			}
			
			if (size != 0)
				bi = ImageFunctions.scaleImage(bi, size);
			
			if (rotationDegrees != 0)
				bi = ImageFunctions.rotate(bi, rotationDegrees);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageFunctions.writeImage(bi, bos, (quality / 100f), "jpg");
			bos.flush();

			return bos.toByteArray(); 
		}
		catch (IOException ioe){
			throw new RuntimeException(ioe);
		}
	}
	
	public void destroy() {
		config = null;
	}
}
