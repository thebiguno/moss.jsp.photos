package ca.digitalcave.moss.jsp.photos;

import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;

public class Common {
	public static String getFullQualityUrlFromFile(ServletContext servletContext, String path){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (servletContext.getContextPath() 
				+ ImageFilter.GALLERIES_PATH 
				+ packageName
				+ "/"
				+ baseName 
				+ ImageFilter.IMAGE_SEPARATOR 
				+ ext
				+ ImageFilter.IMAGE_SEPARATOR
				+ "full"
				+ ".jpg").replaceAll(" ", "%20");
	}
	
	public static String getUrlStubFromFile(PageContext pageContext, String path){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (pageContext.getServletContext().getContextPath() 
				+ ImageFilter.GALLERIES_PATH 
				+ packageName
				+ "/"
				+ baseName 
				+ ImageFilter.IMAGE_SEPARATOR
				+ ext 
				+ ImageFilter.IMAGE_SEPARATOR
				+ "XXX_SIZE_XXX" 
				+ ImageFilter.IMAGE_SEPARATOR
				+ "YYY_QUALITY_YYY" 
				+ ".jpg").replaceAll(" ", "%20");

	}
	
	public static String getUrlFromFile(ServletContext servletContext, String path, int size, int quality){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (servletContext.getContextPath() 
			+ ImageFilter.GALLERIES_PATH 
			+ packageName
			+ "/"
			+ baseName 
			+ ImageFilter.IMAGE_SEPARATOR
			+ ext 
			+ ImageFilter.IMAGE_SEPARATOR
			+ size 
			+ ImageFilter.IMAGE_SEPARATOR
			+ quality 
			+ ".jpg").replaceAll(" ", "%20");
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
	
	public static ImageMetadata getImageMetadata(InputStream image){
		String title = null, caption = null;
		Date captureDate = null;
		try {			
			Metadata metadata = JpegMetadataReader.readMetadata(image);
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
		catch (Throwable t) {}
		
		return new ImageMetadata(title, caption, captureDate);
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
}
