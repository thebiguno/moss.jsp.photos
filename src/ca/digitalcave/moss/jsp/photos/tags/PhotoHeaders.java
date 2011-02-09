package ca.digitalcave.moss.jsp.photos.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import ca.digitalcave.moss.jsp.photos.ImageFilter;

public class PhotoHeaders implements Tag {
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
			pageContext.getOut().println(ImageFilter.getPhotoHeaders(pageContext.getServletContext(), null));
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
