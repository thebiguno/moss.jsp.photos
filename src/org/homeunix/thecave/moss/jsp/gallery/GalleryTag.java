package org.homeunix.thecave.moss.jsp.gallery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private int thumbQuality = 50;
	private int fullQuality = 75;
	
	private boolean showTitle = false;
	private boolean includeLink = true;
	private boolean fullQualityTitleLink = false;

	private String matchRegex = ".*png|.*jpg|.*jpeg|.*bmp|.*png|.*gif";
	private String excludeRegex = "\\..*"; //Hide all dot files

	public boolean isFullQualityTitleLink() {
		return fullQualityTitleLink;
	}
	
	public void setFullQualityTitleLink(boolean fullQualityTitleLink) {
		this.fullQualityTitleLink = fullQualityTitleLink;
	}
	
	public boolean isIncludeLink() {
		return includeLink;
	}
	
	public boolean isShowTitle() {
		return showTitle;
	}
	
	public void setIncludeLink(boolean includeLink) {
		this.includeLink = includeLink;
	}
	
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

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

	public int getThumbQuality() {
		return thumbQuality;
	}

	public void setThumbQuality(int thumbQuality) {
		this.thumbQuality = thumbQuality;
	}

	public int getFullQuality() {
		return fullQuality;
	}

	public void setFullQuality(int fullQuality) {
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

	@SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {
		try {
			pageContext.getOut().println("<div class='gallery'>");
			pageContext.getOut().println("<div class='gallery-start'/>");
			
			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
			Collections.sort(images);
			
			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					pageContext.getOut().println("\n\n\n<div class='gallery-image'>");
					pageContext.getOut().println("<div class='gallery-frame'>");
					if (isIncludeLink()){
						pageContext.getOut().println("<a href='" + getUrlFromFile(imagePath, getFullSize(), getFullQuality()) + "' rel='lightbox[" + getPackageName().replaceAll("/", "_") + "]'>");
					}
					pageContext.getOut().println("<img src='" + getUrlFromFile(imagePath, getThumbSize(), getThumbQuality()) + "' alt=''/>");
					if (isIncludeLink()){
						pageContext.getOut().println("</a>");
					}
					if (isShowTitle()){
						pageContext.getOut().print("<div class='gallery-title'>");
						pageContext.getOut().print(imagePath.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", ""));
						if (isFullQualityTitleLink()){
							pageContext.getOut().print("<div class='gallery-image-download'>");
							pageContext.getOut().print("<a href='" + getUrlFromFile(imagePath, 0, 100) + "'>");
							pageContext.getOut().print("Download High Resolution Image");
							pageContext.getOut().print("</a>");
							pageContext.getOut().println("</div> <!--gallery-image-download-->");
						}
						pageContext.getOut().println("</div> <!--gallery-title-->");
					}
					pageContext.getOut().println("</div> <!--gallery-frame-->");
					pageContext.getOut().println("</div> <!-- gallery-image -->");
				}
			}

			pageContext.getOut().println("<div class='gallery-end'/>");
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

	private String getUrlFromFile(String path, int size, int quality){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");
		
		return (pageContext.getServletContext().getContextPath() 
			+ ImageFilter.GALLERIES_PATH 
			+ packageName
			+ "/"
			+ baseName 
			+ "_" 
			+ ext 
			+ "_"
			+ size 
			+ "_" 
			+ quality 
			+ ".jpg").replaceAll(" ", "%20");
	}
}
