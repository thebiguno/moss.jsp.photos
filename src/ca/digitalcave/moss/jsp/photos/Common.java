package ca.digitalcave.moss.jsp.photos;

import javax.servlet.jsp.PageContext;

public class Common {
	public static String getFullQualityUrlFromFile(PageContext pageContext, String path){
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
				+ "XXXSIZEXXX" 
				+ ImageFilter.IMAGE_SEPARATOR
				+ "YYYQUALITYYY" 
				+ ".jpg").replaceAll(" ", "%20");

	}
	
	public static String getUrlFromFile(PageContext pageContext, String path, int size, int quality){
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
			+ size 
			+ ImageFilter.IMAGE_SEPARATOR
			+ quality 
			+ ".jpg").replaceAll(" ", "%20");
	}
}
