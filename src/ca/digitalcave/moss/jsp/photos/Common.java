package ca.digitalcave.moss.jsp.photos;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

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
}
