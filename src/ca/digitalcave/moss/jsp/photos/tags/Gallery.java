package ca.digitalcave.moss.jsp.photos.tags;

import javax.servlet.http.HttpServletRequest;
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
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			
			request.setAttribute(ImageFilter.ATTR_GALLERY_NAME, galleryName);
			request.setAttribute(ImageFilter.ATTR_SERVLET_CONTEXT, pageContext.getServletContext());			
			request.setAttribute(ImageFilter.ATTR_GALLERY_CONFIG, ImageFilter.getGalleryConfig(pageContext.getServletContext(), galleryName));
			
			pageContext.getOut().println(ImageFilter.getGallery(request));
		}
		catch (Throwable e){
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
