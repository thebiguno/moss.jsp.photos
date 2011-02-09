package ca.digitalcave.moss.jsp.photos.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import ca.digitalcave.moss.jsp.photos.ImageFilter;

public class Gallery implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	private String galleryName;
	
	public String getGalleryName() {
		return galleryName;
	}

	public void setGalleryName(String galleryName) {
		this.galleryName = galleryName;
		if (!this.galleryName.startsWith("/")) this.galleryName = "/" + this.galleryName;
		if (!this.galleryName.endsWith("/")) this.galleryName = this.galleryName + "/";
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

	public int doStartTag() throws JspException {
		try {
			pageContext.getOut().println(ImageFilter.getGallery(pageContext.getServletContext(), getGalleryName()));
		}
		catch (Exception e){
			throw new JspException(e);
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
