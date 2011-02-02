package ca.digitalcave.moss.jsp.photos;

import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;

public class Common {
	public final static String IMAGE_SEPARATOR = "%3A";
	public final static String GALLERIES_PATH = "/galleries";
	public final static String JAVASCRIPT_PATH = "/js";	

	
	public static String getFullQualityUrlFromFile(ServletContext servletContext, String path){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (servletContext.getContextPath() 
				+ Common.GALLERIES_PATH 
				+ packageName
				+ "/"
				+ baseName 
				+ Common.IMAGE_SEPARATOR 
				+ ext
				+ Common.IMAGE_SEPARATOR
				+ "full"
				+ ".jpg").replaceAll(" ", "%20");
	}
	
	public static String getUrlStubFromFile(PageContext pageContext, String path){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (pageContext.getServletContext().getContextPath() 
				+ Common.GALLERIES_PATH 
				+ packageName
				+ "/"
				+ baseName 
				+ Common.IMAGE_SEPARATOR
				+ ext 
				+ Common.IMAGE_SEPARATOR
				+ "XXX_SIZE_XXX" 
				+ Common.IMAGE_SEPARATOR
				+ "YYY_QUALITY_YYY" 
				+ ".jpg").replaceAll(" ", "%20");

	}
	
	public static String getUrlFromFile(ServletContext servletContext, String path, int size, int quality, String extension){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (servletContext.getContextPath() 
			+ Common.GALLERIES_PATH 
			+ packageName
			+ "/"
			+ baseName 
			+ Common.IMAGE_SEPARATOR
			+ ext 
			+ Common.IMAGE_SEPARATOR
			+ size 
			+ Common.IMAGE_SEPARATOR
			+ quality 
			+ "."
			+ extension).replaceAll(" ", "%20");
	}
	
	/**
	 * Returns one of 'html' or 'flash' depending on the parameters, currently set cookies, and
	 * (if nothing else is set) user agent of the request.  
	 * @param request
	 * @return
	 */
	public static String getGalleryType(HttpServletRequest request, HttpServletResponse response){
		String type = null;
		
		if (type == null && "flash".equals(request.getParameter("type"))) {
			type = "flash";
		}
		if (type == null && "html".equals(request.getParameter("type"))) {
			type = "html";
		}
		
		Cookie[] cookies = request.getCookies();
		for (int i = 0; cookies != null && i < cookies.length; i++){
			if (type == null && "type".equals(cookies[i].getName())){
				if ("flash".equals(cookies[i].getValue())) type = "flash";
				if ("html".equals(cookies[i].getValue())) type = "html";
			}
		}

		String userAgent = request.getHeader("User-Agent");
		if (type == null && userAgent == null) type = "html";
		if (type == null && userAgent.toLowerCase().contains("iphone")) type = "html";
		
		if (type == null) type = "flash";
		
		Cookie cookie = new Cookie("type", type);
		cookie.setMaxAge(24 * 60 * 60);
		response.addCookie(cookie);			

		return type;
	}
		
	public static String escapeHtml(String s){
		if (s == null)
			return null;
		return s
		.replaceAll("'", "&apos;")
		.replaceAll("\"", "&quot;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("&", "&amp;")
		.replaceAll("[\r\n]", "<br/>");
	}
	
	public static String escapeXml(String s){
		if (s == null)
			return null;
		return s
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("[\r\n]", "\n");
	}
	
	
	public static ImageParams getImageParams(String requestURI, ServletContext servletContext) throws UnauthorizedException {
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + servletContext.getContextPath() + Common.GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");
		String requestUriFile = requestUriWithoutContextAndGalleriesPrefix.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z]{3,4}$", "");

		String[] split = requestUriFile.split(Common.IMAGE_SEPARATOR);
		boolean fullQuality = false;
		if (split[split.length - 1].equals("full"))
			fullQuality = true;
		else if (split.length < 4){
			throw new UnauthorizedException("Invalid image request");
		}

		String qualityString, sizeString, extension, baseName;
		int size, quality;
		
		if (fullQuality){
			qualityString = null;
			sizeString = null;
			size = 0;
			quality = 100;
			extension = split[split.length - 2];
			baseName = requestUriFile.replaceAll(Common.IMAGE_SEPARATOR + extension + Common.IMAGE_SEPARATOR + "full", "");
		}
		else {
			qualityString = split[split.length - 1];
			String sizeAndTypeString = split[split.length - 2];
			sizeString = sizeAndTypeString.replaceAll("[^0-9]", "");
			size = Integer.parseInt(sizeString);
			quality = Integer.parseInt(qualityString);
			extension = split[split.length - 3];
			baseName = requestUriFile.replaceAll(Common.IMAGE_SEPARATOR + extension + Common.IMAGE_SEPARATOR + sizeAndTypeString + Common.IMAGE_SEPARATOR + qualityString, "");			
		}
		
		//Check the gallery for gallery-specific overrides on max / min size and quality.
		InputStream settings = servletContext.getResourceAsStream("/WEB-INF" + Common.GALLERIES_PATH + packageName + "/settings.xml");
		if (settings == null){
			//You must have a settings.xml file, or you cannot access the content.
			throw new UnauthorizedException("Settings file not found");
		}
		else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);
				
				
				Boolean fullQualityAllowed = getBooleanSettings(doc, "full-quality", "allowed");
				if (fullQuality){
					if (fullQualityAllowed == null || fullQualityAllowed == false){
						throw new UnauthorizedException("Full quality not allowed");
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
				Logger logger = Logger.getLogger(Common.class.getName());
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
		
		InputStream is = servletContext.getResourceAsStream(("/WEB-INF" + Common.GALLERIES_PATH + packageName + baseName + "." + extension).replaceAll("%20", " "));
		
		String title = null, caption = null;
		Date captureDate = null;
		try {			
			Metadata metadata = JpegMetadataReader.readMetadata(is);
			Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
			Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
			
			//OBJ_NAME is the field that Lightroom's 'Title' attribute is saved to.
			if (iptcDirectory.containsTag(IptcDirectory.TAG_OBJECT_NAME)){
				title = iptcDirectory.getString(IptcDirectory.TAG_OBJECT_NAME);
			}
			if (iptcDirectory.containsTag(IptcDirectory.TAG_CAPTION)){
				caption = iptcDirectory.getString(IptcDirectory.TAG_CAPTION);
			}
			if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME_ORIGINAL)){
				captureDate = exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
			}
		}
		catch (Throwable t) {} //This information is not required.		
		
		return new ImageParams(packageName, baseName, extension, fullQuality, size, quality, title, caption, captureDate);
	}
	
	public static String getStringSettings(Document doc, String element, String attribute){
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
			return attrNode.getTextContent();
		}
		catch (NumberFormatException nfe){
			return null;
		}
	}
	
	public static Integer getIntegerSettings(Document doc, String element, String attribute){
		try {return Integer.parseInt(getStringSettings(doc, element, attribute));}
		catch (Throwable e){return null;}
	}
	
	public static Boolean getBooleanSettings(Document doc, String element, String attribute){
		try {return Boolean.parseBoolean(getStringSettings(doc, element, attribute));}
		catch (Throwable e){return null;}
	}

	public final static String IMAGE_PATH = "/images";
}
