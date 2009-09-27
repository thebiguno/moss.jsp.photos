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
	private int size = 600;
	private int quality = 85;
	
	private String matchRegex = ".*png|.*jpg|.*jpeg|.*bmp|.*png|.*gif";
	private String excludeRegex = "\\..*"; //Hide all dot files

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
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

			
			pageContext.getOut().println("<script type='text/javascript'>window.addEvent('load', function() {new SlideShow(document.getElementById('slideshow'), 50, 5000, false);});</script>");
			
			pageContext.getOut().println("<div style='position: relative; clear: both;'>");
			pageContext.getOut().println("<span id='slideshow'>");
			
			List<String> images = new ArrayList<String>(pageContext.getServletContext().getResourcePaths("/WEB-INF/galleries" + getPackageName()));
			Collections.sort(images);
			
			for (String imagePath : images) {
				if (imagePath.toLowerCase().matches(getMatchRegex()) && !imagePath.toLowerCase().matches(getExcludeRegex())){
					pageContext.getOut().println("<img src='" + Common.getUrlFromFile(pageContext, imagePath, getSize(), getQuality()) + "' alt='' class='slideshow' style='opacity: 0; filter:alpha(opacity=0);position: absolute; top: 0px; left: 0px'/>");
				}
			}

			pageContext.getOut().println("<span/>");
			pageContext.getOut().println("</div>");
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
