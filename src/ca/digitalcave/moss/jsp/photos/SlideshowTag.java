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
	
	private String type = "html";
	
	private int fadeSpeed = 500;
	private int photoSpeed = 4000;
	
	private String matchRegex = ".*png|.*jpg|.*jpeg|.*bmp|.*png|.*gif";
	private String excludeRegex = "\\..*"; //Hide all dot files
	
	private int slideshowCounter = 0; //Used to ensure unique divs on each load

	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
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
		if ("flash".equals(getType()) && size < 100)
			size = 100;
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
			
			if ("flash".equals(getType())){
				final String slideshowTxt = "slideshow.txt?packageName=" + getPackageName() + 
				"++size=" + getSize() + 
				"++quality=" + getQuality() +
				"++random=" + isRandom() +
				"++matchRegex=" + getMatchRegex() + 
				"++excludeRegex=" + getExcludeRegex(); 				
				
				pageContext.getOut().println(
						"<div style='position: relative; width: " + getSize() + "px; height: " + getSize() + "px; overflow:hidden'>" + 
						"<OBJECT classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000'" +
						"codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0'" +
						"WIDTH='2000' HEIGHT='2000' id='flashslide' ALIGN=''>" +
						"<PARAM NAME=movie " +
						"VALUE='flashslide.swf?src=flash-here.com&" +
						"imglist_fn=" + slideshowTxt + "&" +
						"img_path=&" +
						"interval=" + getPhotoSpeed() + "&" +
						"navbar=0&" +
						"w=" + getSize() + "&" +
						"h=" + getSize() + "'>" +
						"<PARAM NAME=quality VALUE=high> " +
						"<PARAM NAME=scale VALUE=noscale> " +
						"<PARAM NAME=wmode VALUE=transparent> " +
						"<PARAM NAME=bgcolor VALUE=#FFFFFF> " +
						"<EMBED src='flashslide.swf?src=flash-here.com&" +
						"imglist_fn=" + slideshowTxt + "&" +
						"img_path=&" +
						"interval=" + getPhotoSpeed() + "&" +
						"navbar=0&" +
						"w=" + getSize() + "&" +
						"h=" + getSize() + "' " +
						"quality=high " +
						"scale=noscale " +
						"wmode=transparent " +
						"bgcolor=#FFFFFF  " +
						"WIDTH='2000' " +
						"HEIGHT='2000' " +
						"NAME='flashslide' " +
						"ALIGN=''" +
						"TYPE='application/x-shockwave-flash' PLUGINSPAGE='http://www.macromedia.com/go/getflashplayer'></EMBED>" +
						"</OBJECT>" +
						"</div>"
				);
			}
			else {
				int imageCounter = 0;


				List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
				if (isRandom())
					Collections.shuffle(images);
				else
					Collections.sort(images);

				if (images.size() > 0){
					pageContext.getOut().println("<div id='slideshow" + imageCounter + "div'><img id='slideshow" + imageCounter + "' src='" + Common.getUrlStubFromFile(pageContext, images.get(0)).replace("XXX_SIZE_XXX", "400h").replace("YYY_QUALITY_YYY", "" + getQuality()) + "' alt=''/></div>");
					pageContext.getOut().println("<script type='text/javascript'>");
					pageContext.getOut().println("var size = getWindowSize(); var sourceList = [");				
					for (int i = 0; i < images.size(); i++) {
						String imagePath = images.get(i);
						if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
							pageContext.getOut().print("'" + Common.getUrlStubFromFile(pageContext, imagePath) + "'.replace(/XXX_SIZE_XXX/, (Math.round(Math.min(size.width, size.height) / 100) * 100) + " + getSize() + " + (size.width > size.height ? 'h' : 'w')).replace(/YYY_QUALITY_YYY/, '" + getQuality() + "')");
							if (i < (images.size() - 1))
								pageContext.getOut().println(",");
						}
					}
					pageContext.getOut().println("];");
					pageContext.getOut().println("new Slideshow(document.getElementById('slideshow" + slideshowCounter + "'), sourceList, " + getFadeSpeed() + ", " + getPhotoSpeed() + ", " + isCenter() + ");");
					pageContext.getOut().println("</script>");
				}
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
