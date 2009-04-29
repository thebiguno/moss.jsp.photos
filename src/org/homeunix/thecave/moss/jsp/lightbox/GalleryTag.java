package org.homeunix.thecave.moss.jsp.lightbox;

import java.io.IOException;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class GalleryTag implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	private String packageName = ".";
	private int thumbSize = 128;
	private int fullSize = 800;
	private float thumbQuality = 0.5f;
	private float fullQuality = 0.75f;

	private String matchRegex = ".*png|.*jpg|.*jpeg|.*bmp|.*png|.*gif";
	private String excludeRegex = "\\..*"; //Hide all dot files


	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getThumbSize() {
		return thumbSize;
	}

	public void setThumbSize(int thumbSize) {
		this.thumbSize = thumbSize;
	}

	public int getFullSize() {
		return fullSize;
	}

	public void setFullSize(int fullSize) {
		this.fullSize = fullSize;
	}

	public float getThumbQuality() {
		return thumbQuality;
	}

	public void setThumbQuality(float thumbQuality) {
		this.thumbQuality = thumbQuality;
	}

	public float getFullQuality() {
		return fullQuality;
	}

	public void setFullQuality(float fullQuality) {
		this.fullQuality = fullQuality;
	}

	public String getMatchRegex() {
		return matchRegex;
	}

	public void setMatchRegex(String matchRegex) {
		this.matchRegex = matchRegex;
	}

	public String getExcludeRegex() {
		return excludeRegex;
	}

	public void setExcludeRegex(String excludeRegex) {
		this.excludeRegex = excludeRegex;
	}

	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	public void setParent(Tag parent) {
		this.parent = parent;
	}

	public Tag getParent() {
		return parent;
	}

	public int doStartTag() throws JspException {
		try {
//			String relativePath = pageContext.getServletContext().getRealPath("/");
//			File f = new File(relativePath + File.separator + getLocation());

			pageContext.getOut().write("\n\n<div class='gallery'>\n");

//			Package pkg = Package.getPackage("img");
//			ClassLoader classLoader = GalleryTag.class.getClassLoader();
//			
//			System.out.println(pkg);
			
			Set<?> images = pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName());
			
			for (Object object : images) {
				String imagePath = object.toString();
				pageContext.getOut().write("<div class='gallery-image'>\n");

				pageContext.getOut().write("<a href='" + getUrlFromFile(imagePath, getFullSize(), getFullQuality()) + "' rel='lightbox[" + getPackageName().replaceAll("/", "_") + "]'>");
				pageContext.getOut().write("<img src='" + getUrlFromFile(imagePath, getThumbSize(), getThumbQuality()) + "' alt=''/>");
				pageContext.getOut().write("</a>");

				pageContext.getOut().write("</div> <!-- gallery-image -->\n");
			}

			pageContext.getOut().write("</div> <!-- gallery -->\n");
		} 
		catch(IOException ioe) {
			throw new JspTagException("An IOException occurred.");
		}
		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {
		return SKIP_BODY;
	}

	public void release() {
		pageContext = null;
		parent = null;
	}

	private String getUrlFromFile(String path, int size, float quality){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return pageContext.getServletContext().getContextPath() 
			+ ImageFilter.GALLERIES_PATH 
			+ packageName
			+ "/"
			+ baseName 
			+ "_" 
			+ ext 
			+ "_"
			+ size 
			+ "_" 
			+ ((int) (quality * 100)) 
			+ ".jpg";
	}

//	private class ImageFilenameFilter implements FilenameFilter {
//		public boolean accept(File dir, String name) {
//			//If the name matches the include regex, does not 
//			// match the exclude regex, we include it.
//			return name.toLowerCase().matches(getMatchRegex()) &&
//			!name.toLowerCase().matches(getExcludeRegex()); 
//		}
//	}
}
