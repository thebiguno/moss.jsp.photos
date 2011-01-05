package ca.digitalcave.moss.jsp.photos;

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
	
	private int thumbSize = 256;
	private int fullSize = 800;
	private int thumbQuality = 75;
	private int fullQuality = 85;
	
	private boolean random = false;
	
//	private boolean showTitle = false;
//	private boolean includeLink = true;
	private boolean showFullQualityDownload = false;

	private String matchRegex = "^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$";
	private String excludeRegex = ".*/\\.[^/]*"; //Hide all dot files

	public boolean isRandom() {
		return random;
	}
	
	public void setRandom(boolean random) {
		this.random = random;
	}
	
	public boolean isShowFullQualityDownload() {
		return showFullQualityDownload;
	}
	
	public void setShowFullQualityDownload(boolean fullQualityTitleLink) {
		this.showFullQualityDownload = fullQualityTitleLink;
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

			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
			if (isRandom())
				Collections.shuffle(images);
			else
				Collections.sort(images);

			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					pageContext.getOut().println("<a href='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getFullSize(), getFullQuality()) + "'>");
					pageContext.getOut().println("<img src='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getThumbSize(), getThumbQuality()) + "' alt=''></img>");
					pageContext.getOut().println("</a>\n");
				}
			}			
			
			pageContext.getOut().write("</div> <!-- gallery -->\n");

			pageContext.getOut().write("<script>$('.gallery').galleria({"
//					+ "'height': '100%',"
//					+ "'width': '100%'"
					+ "});</script>\n"
			);
			
			if (isShowFullQualityDownload()){
				pageContext.getOut().write("<p><a href='" + pageContext.getServletContext().getContextPath() + ImageFilter.GALLERIES_PATH + packageName + "/all.zip'>Download All High Resolution Images</a></p>");
			}
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
}
