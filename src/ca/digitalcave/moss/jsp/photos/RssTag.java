package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;

public class RssTag implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	private static int galleryNumber = 0;
	
	private String packageName = ".";
	
	private int size = 600;
	private int quality = 85;
	
	private String order = "sorted";
	
	private boolean showTitle = false;
	private boolean showDate = false;
	private boolean showCaption = false;
	private boolean showFilename = false;

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
			
			pageContext.getOut().println("<div id='gallery" + count + "' style='width: " + getFullSize() + "px; height: " + getFullSize() + "px'>");

			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF" + ImageFilter.GALLERIES_PATH + getPackageName()));
			
			if ("random".equals(getOrder().toLowerCase())) Collections.shuffle(images);
			else {
				Collections.sort(images);
				if ("reverse".equals(getOrder().toLowerCase())){
					Collections.reverse(images);
				}
			}

			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					if (getThumbSize() != 0){					
						pageContext.getOut().println("<a href='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, getFullSize(), getFullQuality()) + "'>");
					}
					pageContext.getOut().println("<img src='" + Common.getUrlFromFile(pageContext.getServletContext(), imagePath, (getThumbSize() == 0 ? getFullSize() : getThumbSize()), (getThumbSize() == 0 ? getFullQuality() : getThumbQuality())) + "'");

					String title = null, caption = null;
					Date date = null;
					
					if (isShowTitle() || isShowCaption()){
						try {
							//Try to load metadata; possibly used for title / caption
							InputStream is = pageContext.getServletContext().getResourceAsStream(imagePath.replaceAll("%20", " "));
							
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
								date = exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
							}
						}
						catch (Throwable t) {}
					}
					
					StringBuilder info = new StringBuilder();
					if (isShowTitle() && title != null){
						pageContext.getOut().println(" title='");
						pageContext.getOut().println(escapeHtml(title)); 
						pageContext.getOut().println("'");
					}
					
					if (isShowCaption() && caption != null){
						info.append("<p>").append(escapeHtml(caption)).append("</p>");
					}
					
					if (isShowDate() && date != null){
						info.append("<p>").append(new SimpleDateFormat("yyyy-MM-dd").format(date)).append("</p>");
					}					
					
					if (isShowFilename()){
						final String filename = imagePath.replaceAll("^/.*/", "");
						info.append("<p>").append(escapeHtml(filename)).append("</p>");
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
			
			//Transition options
			pageContext.getOut().write("transition: 'flash', transition_speed: 1000,");
			
			//Common settings 
			pageContext.getOut().write("show_counter: false, min_scale_ratio: 1, max_scale_ratio: 1, width: " + getFullSize() + ", height: " + getFullSize() + " ");
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
	
	private String escapeHtml(String s){
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
