package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

public class GalleryTag implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	private static int galleryNumber = 0;
	
	private String packageName = ".";
	
	private int thumbSize = 130; //If thumbSize is 0 we will use full images (scaled in galleria) for thumbs.  This allows pre-loading an entire page for quicker access; however it will slow down the initial rendering for large pages.
	private int fullSize = 800;
	private int thumbQuality = 75;
	private int fullQuality = 85;
	
	private boolean center = true;
	private String order = "sorted";
	
	private boolean slideshow = false; //Is slideshow enabled?
	private int slideshowDelay = 5000; //How long to wait in between slideshow slides (in millis); only used if slideshow is enabled
	private boolean slideshowAllowOverride = false; //Can the user stop the slideshow?  (i.e. is the carosel there?)
	
	private boolean showTitle = false;
	private boolean showDate = false;
	private boolean showCaption = false;
	private boolean showFilename = false;

	private boolean showRssLink = false;
	private boolean showFullQualityDownload = false;

	private String matchRegex = "^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$";
	private String excludeRegex = ".*/\\.[^/]*"; //Hide all dot files

	public String getOrder() {
		return order;
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
	public boolean isShowDate() {
		return showDate;
	}
	public void setShowDate(boolean showDate) {
		this.showDate = showDate;
	}
	public boolean isShowCaption() {
		return showCaption;
	}
	public void setShowCaption(boolean showCaption) {
		this.showCaption = showCaption;
	}
	public boolean isShowFilename() {
		return showFilename;
	}
	public void setShowFilename(boolean showFilename) {
		this.showFilename = showFilename;
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
	
	public void setOrder(String order) {
		this.order = order;
	}
	
	public boolean isShowFullQualityDownload() {
		return showFullQualityDownload;
	}
	public void setShowFullQualityDownload(boolean fullQualityTitleLink) {
		this.showFullQualityDownload = fullQualityTitleLink;
	}
	public boolean isShowRssLink() {
		return showRssLink;
	}
	public void setShowRssLink(boolean showRssLink) {
		this.showRssLink = showRssLink;
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
			
			if (isShowRssLink()){
				pageContext.getOut().println("<div style='float: right'><a href='" + pageContext.getServletContext().getContextPath() + Common.GALLERIES_PATH + packageName + "/gallery.rss'><img src='" + pageContext.getServletContext().getContextPath() + Common.IMAGE_PATH + "/rss/rss.png' alt='Subscribe to RSS Feed' border='none'></a></div><div style='clear: both'/>");
			}
			
			final int count = galleryNumber++;
			
			pageContext.getOut().println("<div id='gallery" + count + "' style='width: " + (int) (getFullSize() * 1.1) + "px; height: " + (int) (getFullSize() * 1.1) + "px'>");

			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF" + Common.GALLERIES_PATH + getPackageName()));
			
			if ("random".equals(getOrder().toLowerCase())) Collections.shuffle(images);
			else {
				Collections.sort(images);
				if ("reverse".equals(getOrder().toLowerCase())){
					Collections.reverse(images);
				}
			}

			for (String imagePath : images) {
				try {
					if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
						String imageURI = Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getFullSize(), getFullQuality(), "jpg");
						if (getThumbSize() != 0){					
							pageContext.getOut().println("<a href='" + imageURI + "'>");
						}

						ImageParams imageParams = Common.getImageParams(imageURI, pageContext.getServletContext());

						pageContext.getOut().println("<img src='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, (getThumbSize() == 0 ? getFullSize() : getThumbSize()), (getThumbSize() == 0 ? getFullQuality() : getThumbQuality()), "jpg") + "'");
						StringBuilder info = new StringBuilder();
						if (isShowTitle() && imageParams.getTitle() != null){
							pageContext.getOut().println(" title='");
							pageContext.getOut().println(Common.escapeHtml(imageParams.getTitle())); 
							pageContext.getOut().println("'");
						}

						if (isShowCaption() && imageParams.getCaption() != null){
							info.append("<p>").append(Common.escapeHtml(imageParams.getCaption())).append("</p>");
						}

						if (isShowDate() && imageParams.getCaptureDate() != null){
							info.append("<p>").append(new SimpleDateFormat("yyyy-MM-dd").format(imageParams.getCaptureDate())).append("</p>");
						}					

						if (isShowFilename()){
							final String filename = imagePath.replaceAll("^/.*/", "");
							info.append("<p>").append(Common.escapeHtml(filename)).append("</p>");
						}

						if (info.length() > 0){
							pageContext.getOut().println(" alt='");
							pageContext.getOut().println(info); 
							pageContext.getOut().println("'");
						}
						if (isShowFullQualityDownload()){
							pageContext.getOut().println(" longdesc='" + Common.getFullQualityUrlFromFile(pageContext.getServletContext(), imagePath) + "'");
						}
						pageContext.getOut().println("></img>\n");
						if (getThumbSize() != 0){
							pageContext.getOut().println("</a>\n");
						}
					}
				}
				catch (UnauthorizedException e){}
			}			
			
			pageContext.getOut().write("</div> <!-- gallery -->\n");
			if (isCenter()){
				pageContext.getOut().write("</div> <!-- center -->\n");
			}

			pageContext.getOut().write("<script type='text/javascript'>$('#gallery" + count + "').galleria({");
			
			if (isSlideshow()){
				pageContext.getOut().write("autoplay: " + getSlideshowDelay() + ",");
				if (!isSlideshowAllowOverride()){
					pageContext.getOut().write("thumbnails: false,");
					pageContext.getOut().write("show_imagenav: false,");
					//pageContext.getOut().write("pause_on_interaction: false, ");
				}
			}
			else {
				pageContext.getOut().write("autoplay: false,");
			}

			//Thumbnail options
			pageContext.getOut().write("thumb_crop: false,");
			pageContext.getOut().write("image_crop: 'width',");
			
			//Transition options
			pageContext.getOut().write("transition: 'flash', transition_speed: 1000,");
			
			//Common settings 
			pageContext.getOut().write("show_counter: false, min_scale_ratio: 1, max_scale_ratio: 1, width: " + getFullSize() + ", height: " + getFullSize() + " ");
			pageContext.getOut().write("});</script>\n");
			
			if (isShowFullQualityDownload()){
				pageContext.getOut().write("<p><a href='" + pageContext.getServletContext().getContextPath() + Common.GALLERIES_PATH + packageName + "/all.zip'>Download All High Resolution Images</a></p>");
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
