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
	
	private String type = "html";
	
	private int thumbSize = 256;
	private int fullSize = 800;
	private int thumbQuality = 75;
	private int fullQuality = 85;
	
	private boolean random = false;
	
	private String flashBackgroundColor = "222222";
	private int flashRowCount = 1;
	private int flashColumnCount = 10;
	private int flashWidth = 1000;
	private int flashHeight = 800;
	private String flashThumbPosition = "BOTTOM";
	
	private boolean showTitle = false;
	private boolean includeLink = true;
	private boolean showFullQualityDownload = false;

	private String matchRegex = "^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$";
	private String excludeRegex = "\\..*"; //Hide all dot files

	public boolean isRandom() {
		return random;
	}
	
	public void setRandom(boolean random) {
		this.random = random;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getFlashHeight() {
		return flashHeight;
	}
	
	public void setFlashHeight(int flashHeight) {
		this.flashHeight = flashHeight;
	}
	
	public int getFlashWidth() {
		return flashWidth;
	}
	
	public void setFlashWidth(int flashWidth) {
		this.flashWidth = flashWidth;
	}
	
	public String getFlashBackgroundColor() {
		return flashBackgroundColor;
	}
	
	public void setFlashBackgroundColor(String flashBackgroundColor) {
		this.flashBackgroundColor = flashBackgroundColor;
	}
	
	public int getFlashColumnCount() {
		return flashColumnCount;
	}
	
	public void setFlashColumnCount(int flashColumnCount) {
		this.flashColumnCount = flashColumnCount;
	}
	
	public int getFlashRowCount() {
		return flashRowCount;
	}
	
	public void setFlashRowCount(int flashRowCount) {
		this.flashRowCount = flashRowCount;
	}
	
	public String getFlashThumbPosition() {
		return flashThumbPosition;
	}
	
	public void setFlashThumbPosition(String flashThumbPosition) {
		this.flashThumbPosition = flashThumbPosition;
	}
	
	public boolean isShowFullQualityDownload() {
		return showFullQualityDownload;
	}
	
	public void setShowFullQualityDownload(boolean fullQualityTitleLink) {
		this.showFullQualityDownload = fullQualityTitleLink;
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

	private static int counter = 0;
	
	@SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {
		try {
			if ("flash".equals(type)){
				pageContext.getOut().println("<div class='gallery'>");

				final String galleryXml = "gallery.xml?packageName=" + getPackageName() + 
					"++showTitle=" + isShowTitle() +
					"++showFullQuality=" + isShowFullQualityDownload() +
					"++thumbPosition=" + getFlashThumbPosition() +
					"++rowCount=" + getFlashRowCount() +
					"++columnCount=" + getFlashColumnCount() +
					"++fullSize=" + getFullSize() + 
					"++fullQuality=" + getFullQuality() +
					"++thumbSize=" + getThumbSize() + 
					"++thumbQuality=" + getThumbQuality() +
					"++random=" + isRandom() +
					"++matchRegex=" + getMatchRegex() + 
					"++excludeRegex=" + getExcludeRegex(); 
				
				pageContext.getOut().println(
						"<object width='" + getFlashWidth() + "' height='" + getFlashHeight() + "'>\n" +
						"<param name=\"movie\" value=\"simpleviewer.swf?galleryURL=" + galleryXml + "\"></param>\n" + 
						"<param name=\"allowFullScreen\" value=\"true\"></param>\n" +
						"<param name=\"allowscriptaccess\" value=\"always\"></param>\n" +
						"<param name=\"bgcolor\" value=\"" + getFlashBackgroundColor() + "\"></param>\n" +
						"<embed " +
						"src=\"simpleviewer.swf?galleryURL=" + galleryXml + "\"" + 
						"type=\"application/x-shockwave-flash\" " +
						"allowscriptaccess=\"always\" " +
						"allowfullscreen=\"true\" " +
						"bgcolor=\"" + getFlashBackgroundColor() + "\" " +
						"width='" + getFlashWidth() + "' " +
						"height='" + getFlashHeight() + "'>" + 
						"</embed>\n" +
						"</object>\n"
				);
				
				pageContext.getOut().write("</div> <!-- gallery -->\n");
			}			
			else { //Fallback to HTML
				pageContext.getOut().println("<div class='gallery'>");
				pageContext.getOut().println("<div class='gallery-start'></div>");

				List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
				if (isRandom())
					Collections.shuffle(images);
				else
					Collections.sort(images);


				for (String imagePath : images) {
					if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
						pageContext.getOut().println("\n\n\n<div class='gallery-image'>");
						pageContext.getOut().println("<div class='gallery-frame'>");
						if (isIncludeLink()){
							pageContext.getOut().println("<a id='lightbox" + counter + "' href='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getFullSize(), getFullQuality()) + "' rel='lightbox[" + getPackageName().replaceAll("/", "_") + "]'>");
						}
						pageContext.getOut().println("<img src='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getThumbSize(), getThumbQuality()) + "' alt=''></img>");
						if (isIncludeLink()){
							pageContext.getOut().println("</a>");
						}
						if (isShowTitle() || isShowFullQualityDownload()){
							pageContext.getOut().print("<div class='gallery-title'>");
							if (isShowTitle()){
								pageContext.getOut().print(imagePath.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", ""));
							}
							if (isShowFullQualityDownload()){
								pageContext.getOut().print("<div class='gallery-image-download'>");
								pageContext.getOut().print("<a href='" + Common.getFullQualityUrlFromFile(pageContext.getServletContext(), imagePath) + "'>");
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

				pageContext.getOut().println("<div class='gallery-end'></div>");
				pageContext.getOut().write("</div> <!-- gallery -->\n");

			}

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
