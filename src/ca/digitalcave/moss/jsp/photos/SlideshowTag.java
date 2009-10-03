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
	private int photoSpeed = 4000;
	
	private String loadingImage = "images/slideshow-loading.gif";
	
	private String matchRegex = ".*png|.*jpg|.*jpeg|.*bmp|.*png|.*gif";
	private String excludeRegex = "\\..*"; //Hide all dot files
	
	private int slideshowCounter = 0; //Used to ensure unique divs on each load

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getLoadingImage() {
		return loadingImage;
	}
	public void setLoadingImage(String loadingImage) {
		this.loadingImage = loadingImage;
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
			
			pageContext.getOut().println("<img id='slideshow" + imageCounter + "' src='" + getLoadingImage() + "' alt='Loading Slideshow (requires Javascript)...' style='position: absolute;'>");
			
			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
			if (isRandom())
				Collections.shuffle(images);
			else
				Collections.sort(images);
			
			pageContext.getOut().println("<script type='text/javascript'>");
			pageContext.getOut().println("var size = getWindowSize(); var sourceList = [");
			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					pageContext.getOut().println("'" + Common.getUrlStubFromFile(pageContext, imagePath) + "'.replace(/XXX_SIZE_XXX/, (Math.round(size.height / 100) * 100) + " + getSize() + " + 'h').replace(/YYY_QUALITY_YYY/, '" + getQuality() + "'),");
				}
			}
			pageContext.getOut().println("];");
			pageContext.getOut().println("new Slideshow(document.getElementById('slideshow" + slideshowCounter + "'), sourceList, " + getFadeSpeed() + ", " + getPhotoSpeed() + ", " + isCenter() + ");");
			pageContext.getOut().println("</script>");
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
