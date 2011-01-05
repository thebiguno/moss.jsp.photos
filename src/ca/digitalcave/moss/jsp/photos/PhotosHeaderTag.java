package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class PhotosHeaderTag implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	public void setParent(Tag t) {
		parent = t;
	}

	public Tag getParent() {
		return parent;
	}

	public int doStartTag() throws JspException {
		try {
			pageContext.getOut().write("<script src='http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js'></script>\n");
			pageContext.getOut().write("<script type='text/javascript' src='" + pageContext.getServletContext().getContextPath() + ImageFilter.JAVASCRIPT_PATH + "/galleria/galleria.js'></script>\n");
			pageContext.getOut().write("<script type='text/javascript' src='" + pageContext.getServletContext().getContextPath() + ImageFilter.JAVASCRIPT_PATH + "/galleria/themes/classic/galleria.classic.js'></script>\n");

			//TODO Remove 
//			pageContext.getOut().write("<link rel='stylesheet' href='" + pageContext.getServletContext().getContextPath() + ImageFilter.CSS_PATH + "/lightbox.css' type='text/css' media='screen' />\n");
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
