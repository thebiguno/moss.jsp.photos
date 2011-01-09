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

	private static int galleryNumber = 0;
	
	private String packageName = ".";
	
	private int thumbSize = 0; //If thumbSize is 0 we will use full images (scaled in galleria) for thumbs.  This allows pre-loading an entire page for quicker access.
	private int fullSize = 800;
	private int thumbQuality = 75;
	private int fullQuality = 85;
	
	private boolean center = true;
	private boolean random = false;
	
	private boolean slideshow = false; //Is slideshow enabled?
	private int slideshowDelay = 5000; //How long to wait in between slideshow slides (in millis); only used if slideshow is enabled
	private boolean slideshowAllowOverride = false; //Can the user stop the slideshow?  (i.e. is the carosel there?)
	
	private boolean showTitle = false;
	private boolean showFullQualityDownload = false;

	private String matchRegex = "^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$";
	private String excludeRegex = ".*/\\.[^/]*"; //Hide all dot files

	public boolean isRandom() {
		return random;
	}
	
	public boolean isCenter() {
		return center;
	}
	public void setCenter(boolean center) {
		this.center = center;
	}
	
	public boolean isShowTitle() {
		return showTitle;
	}
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}
	public boolean isSlideshow() {
		return slideshow;
	}
	public void setSlideshow(boolean slideshow) {
		this.slideshow = slideshow;
	}
	public int getSlideshowDelay() {
		return slideshowDelay;
	}
	public void setSlideshowDelay(int slideshowDelay) {
		this.slideshowDelay = slideshowDelay;
	}
	public boolean isSlideshowAllowOverride() {
		return slideshowAllowOverride;
	}
	public void setSlideshowAllowOverride(boolean slideshowAllowOverride) {
		this.slideshowAllowOverride = slideshowAllowOverride;
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
			if (isCenter()){
				pageContext.getOut().println("<div style='margin-left: auto; margin-right: auto; width: " + getFullSize() + "px;'>");
			}
			
			final int count = galleryNumber++;
			
			pageContext.getOut().println("<div id='gallery" + count + "' style='width: " + getFullSize() + "; height: " + getFullSize() + "'>");

			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
			
			if (isRandom()) Collections.shuffle(images);
			else Collections.sort(images);

			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					pageContext.getOut().println("<a href='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getFullSize(), getFullQuality()) + "'>");
					pageContext.getOut().println("<img src='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, (getThumbSize() == 0 ? getFullSize() : getThumbSize()), (getThumbSize() == 0 ? getFullQuality() : getThumbQuality())) + "'");
					if (isShowTitle()){
						pageContext.getOut().println(" alt='" + imagePath.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "") + "'");
					}
					if (isShowFullQualityDownload()){
						pageContext.getOut().println(" longdesc='" + Common.getFullQualityUrlFromFile(pageContext.getServletContext(), imagePath) + "'");
					}
					pageContext.getOut().println("></img>");
					pageContext.getOut().println("</a>\n");
				}
			}			
			
			pageContext.getOut().write("</div> <!-- gallery -->\n");
			if (isCenter()){
				pageContext.getOut().write("</div> <!-- center -->\n");
			}

			pageContext.getOut().write("<script>$('#gallery" + count + "').galleria({");
			
			if (isSlideshow()){
				pageContext.getOut().write("autoplay: " + getSlideshowDelay() + ",");
				if (!isSlideshowAllowOverride()){
					pageContext.getOut().write("thumbnails: false,");
					pageContext.getOut().write("show_imagenav: false,");
					pageContext.getOut().write("pause_on_interaction: false, ");
				}
			}
			else {
				pageContext.getOut().write("autoplay: false,");
			}

			//Thumbnail options
			pageContext.getOut().write("thumb_crop: false,");
			
			//Transition options
			pageContext.getOut().write("transition: 'flash', transition_speed: 1000,");
			
			//Common settings 
			pageContext.getOut().write("preload: 'all', show_counter: false, min_scale_ratio: 1, max_scale_ratio: 1, width: " + getFullSize() + ", height: " + getFullSize() + " ");
			pageContext.getOut().write("});</script>\n");
			
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
