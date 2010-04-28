/*
 * Created on May 28, 2008 by wyatt
 */
package ca.digitalcave.moss.jsp.photos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.digitalcave.moss.common.StreamUtil;

public class ImageFilter implements Filter {
	final static String JAVASCRIPT_PATH = "/js";
	final static String CSS_PATH = "/css";
	final static String IMAGE_PATH = "/images";
	final static String GALLERIES_PATH = "/galleries";
	
	final static String IMAGE_SEPARATOR = "_";
	
	private FilterConfig config;
	private Logger logger = Logger.getLogger(ImageFilter.class.getName());
	
	//private final static ExecutorService executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	private final static ExecutorService executor = new ThreadPoolExecutor(2, 3, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		//Built in script / style components
		for (String path : new String[]{JAVASCRIPT_PATH, CSS_PATH, IMAGE_PATH}) {
			if (request.getRequestURI().matches(config.getServletContext().getContextPath() + path + "/.*\\.[a-zA-Z]{2,3}")){
				String name = request.getRequestURI().replaceAll(config.getServletContext().getContextPath() + path + "/", "");
				InputStream is = ImageFilter.class.getResourceAsStream("resources/" + name);
				if (is != null){
					StreamUtil.copyStream(is, res.getOutputStream());
					return;
				}
			}
		}
		
		//Gallery images zip - located under /galleries/<packageName>/all.zip
		if (request.getRequestURI().matches(config.getServletContext().getContextPath() + GALLERIES_PATH + "/.+/all\\.zip")){
			doServeZip(request, response);
			return;
		}
		//Gallery images - located under /galleries/<packageName>/<filename>
		else if (request.getRequestURI().matches(config.getServletContext().getContextPath() + GALLERIES_PATH + "/.+" )){
			doServeImage(request, response);
			return;
		}
		
		
		chain.doFilter(req, res);
	}
	
	/**
	 * Given a request URI, returns a .zip file containing all the high quality images.  The request
	 * URI must be in the form:
	 *  /galleries/package/name/all.zip
	 * where package/name is the path to the source gallery, relative to WEB-INF/galleries.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void doServeZip(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String requestURI = request.getRequestURI();
		
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");

		//Check the gallery for gallery-specific settings.  If full resolution downloads are not allowed, return 403.
		InputStream settings = config.getServletContext().getResourceAsStream("/WEB-INF" + GALLERIES_PATH + packageName + "/settings.xml");
		if (settings == null){
			//You must have a settings.xml file, or you cannot access the content.
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);
				
				
				Boolean fullQualityAllowed = getBooleanSettings(doc, "full-quality", "allowed");
				if (fullQualityAllowed == null || fullQualityAllowed == false){
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
			}
			catch (Exception e){
				logger.log(Level.WARNING, "Error while opening settings.xml for gallery package " + packageName, e);
			}
		}
		
		//Create the zip file
		ByteArrayOutputStream zipTemp = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(zipTemp);
		
		//Get a list of files from the context path
		List<String> images = new ArrayList<String>(request.getSession().getServletContext().getResourcePaths("/WEB-INF/galleries" + packageName));
		
		for (String image : images) {
			if (image.toLowerCase().matches("^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$")){
				InputStream imageInputStream = config.getServletContext().getResourceAsStream(image);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamUtil.copyStream(imageInputStream, baos);
				String fileName = image.replaceAll("/.*/", "");
				ZipEntry zipEntry = new ZipEntry(packageName.replaceAll("^/", "") + "/" + fileName);
				zipEntry.setSize(baos.size());
				zout.putNextEntry(zipEntry);
				zout.write(baos.toByteArray());
				zout.closeEntry();
			}
		}
		
		zout.finish();
		zout.flush();
		
		response.setContentLength(zipTemp.size());
		response.setContentType("application/zip");
		response.addHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Disposition","inline; filename=" + packageName.replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("^_", "").replaceAll("_$", "") + ".zip;");
		
		response.getOutputStream().write(zipTemp.toByteArray());

		return;
//		//We load source images from the context path
//		InputStream is = config.getServletContext().getResourceAsStream(("/WEB-INF" + GALLERIES_PATH + packageName + baseName + "." + ext).replaceAll("%20", " "));
//
//		if (is == null){
//			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//			return;
//		}
//		if (fullQuality){
//			response.setContentType("application/octet-stream");
//			StreamUtil.copyStream(is, response.getOutputStream());
//		}
//		else {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			StreamUtil.copyStream(is, baos);
//			byte[] convertedImage = null;
//			
//			convertedImage = convertImage(baos.toByteArray(), size, typeString, quality);
//			if (convertedImage != null)
//				StreamUtil.copyStream(new ByteArrayInputStream(convertedImage), response.getOutputStream());
//		}
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
	 */
	private void doServeImage(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String requestURI = request.getRequestURI();
		
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");
		String requestUriFile = requestUriWithoutContextAndGalleriesPrefix.replaceAll("^/.*/", "").replaceAll("\\.jpg$", "");

		String[] split = requestUriFile.split(IMAGE_SEPARATOR);
		boolean fullQuality = false;
		if (split[split.length - 1].equals("full"))
			fullQuality = true;
		else if (split.length < 4){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String qualityString, typeString, sizeString, ext, baseName;
		int size, quality;
		
		if (fullQuality){
			qualityString = null;
			typeString = null;
			sizeString = null;
			size = 0;
			quality = 100;
			ext = split[split.length - 2];
			baseName = requestUriFile.replaceAll(IMAGE_SEPARATOR + ext + IMAGE_SEPARATOR + "full", "");
		}
		else {
			qualityString = split[split.length - 1];
			String sizeAndTypeString = split[split.length - 2];
			typeString = sizeAndTypeString.replaceAll("[^a-zA-Z]", ""); //Size type will determine what sort of sizing algorithm to use.
			sizeString = sizeAndTypeString.replaceAll("[^0-9]", "");
			size = Integer.parseInt(sizeString);
			quality = Integer.parseInt(qualityString);
			ext = split[split.length - 3];
			baseName = requestUriFile.replaceAll(IMAGE_SEPARATOR + ext + IMAGE_SEPARATOR + sizeAndTypeString + IMAGE_SEPARATOR + qualityString, "");			
		}

		//Check the gallery for gallery-specific overrides on max / min size and quality.
		InputStream settings = config.getServletContext().getResourceAsStream("/WEB-INF" + GALLERIES_PATH + packageName + "/settings.xml");
		if (settings == null){
			//You must have a settings.xml file, or you cannot access the content.
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);
				
				
				Boolean fullQualityAllowed = getBooleanSettings(doc, "full-quality", "allowed");
				if (fullQuality){
					if (fullQualityAllowed == null || fullQualityAllowed == false){
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}
				else {
					Integer sizeMax = getIntegerSettings(doc, "size", "max");
					Integer sizeMin = getIntegerSettings(doc, "size", "min");
					Integer qualityMax = getIntegerSettings(doc, "quality", "max");
					Integer qualityMin = getIntegerSettings(doc, "quality", "min");
					
					if (sizeMax != null && size > sizeMax)
						size = sizeMax;
					if (sizeMin != null && size < sizeMin)
						size = sizeMin;
					if (qualityMax != null && quality > qualityMax)
						quality = qualityMax;
					if (qualityMin != null && quality < qualityMin)
						quality = qualityMin;
				}
			}
			catch (Exception e){
				logger.log(Level.WARNING, "Error while opening settings.xml for gallery package " + packageName, e);
			}
		}
		
		//Check for sane values on size / quality - only applicable for non-full quality images
		if (!fullQuality){
			if (size < 10)
				size = 10;
			if (size > 4000)
				size = 4000;
			if (quality > 100)
				quality = 100;
			if (quality < 0)
				quality = 0;
		}
		
		//We load source images from the context path
		InputStream is = config.getServletContext().getResourceAsStream(("/WEB-INF" + GALLERIES_PATH + packageName + baseName + "." + ext).replaceAll("%20", " "));

		if (is == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (fullQuality){
			response.setContentType("application/octet-stream");
			StreamUtil.copyStream(is, response.getOutputStream());
		}
		else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamUtil.copyStream(is, baos);
			byte[] convertedImage = null;
			
			convertedImage = convertImage(baos.toByteArray(), size, typeString, quality);
			if (convertedImage != null)
				StreamUtil.copyStream(new ByteArrayInputStream(convertedImage), response.getOutputStream());
		}
	}
	
	private Integer getIntegerSettings(Document doc, String element, String attribute){
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
	
	private Boolean getBooleanSettings(Document doc, String element, String attribute){
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
			return Boolean.parseBoolean(attrNode.getTextContent());
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
	private byte[] convertImage(byte[] originalImage, int size, String sizeType, int quality){
		ImageConverterCallable imageConverterRunnable = new ImageConverterCallable(originalImage, size, sizeType, quality);
		try {
			Future<byte[]> future = executor.submit(imageConverterRunnable);
			return future.get();
		}
		catch (Exception e){
//			logger.warning(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public void destroy() {
		config = null;
	}
}
