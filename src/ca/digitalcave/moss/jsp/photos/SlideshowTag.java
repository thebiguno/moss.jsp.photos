package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class SlideshowTag implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	private String packageName = ".";
	private int size = -100; //Default to slightly smaller than full window
	private int quality = 85;
	private boolean random = false;
	private boolean center = false;
	
	private int fadeSpeed = 25;
	private int photoSpeed = 5000;
	
	private String matchRegex = ".*png|.*jpg|.*jpeg|.*bmp|.*png|.*gif";
	private String excludeRegex = "\\..*"; //Hide all dot files
	
	private int slideshowCounter = 0; //Used to ensure unique divs on each load

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public boolean isRandom() {
		return random;
	}
	public void setRandom(boolean random) {
		this.random = random;
	}
	public boolean isCenter() {
		return center;
	}
	public void setCenter(boolean center) {
		this.center = center;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
	public int getFadeSpeed() {
		return fadeSpeed;
	}
	public void setFadeSpeed(int fadeSpeed) {
		this.fadeSpeed = fadeSpeed;
	}
	public int getPhotoSpeed() {
		return photoSpeed;
	}
	public void setPhotoSpeed(int photoSpeed) {
		this.photoSpeed = photoSpeed;
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
			int imageCounter = 0;
						
//			pageContext.getOut().println("<script type='text/javascript'>function startSlideshow(){new SlideShow(document.getElementById('slideshow" + slideshowCounter + "'), " + getFadeSpeed() + ", " + getPhotoSpeed() + ", false); alert('Foo');} window.onLoad = startSlideshow;</script>");			
//			pageContext.getOut().println("<script type='text/javascript'>new SlideShow(document.getElementById('slideshow" + slideshowCounter + "'), " + getFadeSpeed() + ", " + getPhotoSpeed() + ", false); alert('Foo');</script>");
			pageContext.getOut().println("<div style='position: relative; clear: both;'>");
			pageContext.getOut().println("<span id='slideshow" + slideshowCounter + "'>");
			pageContext.getOut().println("<script type='text/javascript'>var size = getWindowSize()</script>");
			
			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
			if (isRandom())
				Collections.shuffle(images);
			else
				Collections.sort(images);
			
			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					//Negative numbers are interpreted as 'full screen - X pixels', where X is the negative number
					if (getSize() < 0){
						pageContext.getOut().println("<img id='slideshow" + slideshowCounter + "image" + imageCounter + "' src='' alt='' class='slideshow' style='opacity: 0; filter:alpha(opacity=0);position: absolute;'/>");
						pageContext.getOut().println("" +
								"<script type='text/javascript'>\n" +
								//We round down to 100px even, make it more likely that others will have already seen the image (and it will be cached at this size)
								"document.getElementById('slideshow" + slideshowCounter + "image" + imageCounter + "').src = '" + Common.getUrlStubFromFile(pageContext, imagePath) + "'.replace(/XXX_SIZE_XXX/, (Math.round(size.height / 100) * 100) + " + getSize() + " + 'h').replace(/YYY_QUALITY_YYY/, '" + getQuality() + "');");
						
						if (isCenter()){
							pageContext.getOut().println("document.getElementById('slideshow" + slideshowCounter + "image" + imageCounter + "').onload = function(){var image = document.getElementById('slideshow" + slideshowCounter + "image" + imageCounter + "'); image.style.left = ((size.width - image.naturalWidth) / 2) + 'px'; image.style.top = ((size.height - image.naturalHeight - 100) / 2) + 'px';}");
						}
						
						pageContext.getOut().println("</script>");
					}
					//Zero size is full image, no matter what the resolution
					else if (getSize() == 0){
						pageContext.getOut().println("<img id='slideshow" + slideshowCounter + "image" + imageCounter + "' src='" + Common.getFullQualityUrlFromFile(pageContext, imagePath) + "' alt='' class='slideshow' style='opacity: 0; filter:alpha(opacity=0);position: absolute; top: 0px; left: 0px'/>");
					}
					//Otherwise, interpret as 'normal' moss JSP photo sizes (hypotenuse)
					else{
						pageContext.getOut().println("<img id='slideshow" + slideshowCounter + "image" + imageCounter + "' src='" + Common.getUrlFromFile(pageContext, imagePath, getSize(), getQuality()) + "' alt='' class='slideshow' style='opacity: 0; filter:alpha(opacity=0);position: absolute; top: 0px; left: 0px'/>");
					}
				}
				
				imageCounter++;
			}

			pageContext.getOut().println("<span/>");
			pageContext.getOut().println("</div>");
			
			pageContext.getOut().println("<script type='text/javascript'>window.onLoad = new SlideShow(document.getElementById('slideshow" + slideshowCounter + "'), " + getFadeSpeed() + ", " + getPhotoSpeed() + ", false);</script>");
			
//			slideshowCounter++;
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
