package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class PhotosHeaderTag implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;
	
	private String rssPackages;
	
	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	public String getRssPackages() {
		return rssPackages;
	}
	public void setRssPackages(String rssPackages) {
		this.rssPackages = rssPackages;
	}
	public void setParent(Tag t) {
		parent = t;
	}

	public Tag getParent() {
		return parent;
	}

	public int doStartTag() throws JspException {
		try {
			final String rssPackages = (getRssPackages() != null ? getRssPackages() : pageContext.getRequest().getParameter("rssPackages"));
			if (rssPackages != null && rssPackages.length() > 0){
				for (String rssPackage : rssPackages.split(",")) {
					pageContext.getOut().write("<link rel='alternate' type='application/rss+xml' title='RSS Feed for " + rssPackage.replaceFirst("/", "") + " gallery' href='" + pageContext.getServletContext().getContextPath() + Common.GALLERIES_PATH + rssPackage + "/gallery.rss' />");
				}
			}

			
			pageContext.getOut().write("<script type='text/javascript' src='http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js'></script>\n");
			pageContext.getOut().write("<script type='text/javascript' src='" + pageContext.getServletContext().getContextPath() + Common.JAVASCRIPT_PATH + "/galleria/galleria.js'></script>\n");
			pageContext.getOut().write("<script type='text/javascript' src='" + pageContext.getServletContext().getContextPath() + Common.JAVASCRIPT_PATH + "/galleria-themes/classic-modified/galleria.classic.js'></script>\n");
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
