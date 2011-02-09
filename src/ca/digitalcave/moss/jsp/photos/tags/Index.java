package ca.digitalcave.moss.jsp.photos.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import ca.digitalcave.moss.jsp.photos.ImageFilter;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.GalleryConfig;

public class Index implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

	private String rootGalleryName;
	
	public String getRootGalleryName() {
		return rootGalleryName;
	}

	public void setRootGalleryName(String rootGalleryName) {
		this.rootGalleryName = rootGalleryName;
		if (!this.rootGalleryName.startsWith("/")) this.rootGalleryName = "/" + this.rootGalleryName;
		if (!this.rootGalleryName.endsWith("/")) this.rootGalleryName = this.rootGalleryName + "/";
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
			final ServletContext servletContext = pageContext.getServletContext();
			final String contextPath = servletContext.getContextPath();
			
			@SuppressWarnings("unchecked")
			List<String> galleries = new ArrayList<String>(servletContext.getResourcePaths("/WEB-INF" + ImageFilter.GALLERIES_PATH + rootGalleryName));
			Collections.sort(galleries);
			
			pageContext.getOut().println("<ul>");
			for (String gallery : galleries) {
				//We check if there is a settings.xml in the folder; this verifies that a) it actually is a gallery, and b) that we can load something from it.
				try {
					gallery = gallery.replaceAll("^/WEB-INF" + ImageFilter.GALLERIES_PATH, "");
					GalleryConfig galleryConfig = ImageFilter.getGalleryConfig(servletContext, gallery);
					if (galleryConfig.isShowIndex()){
						pageContext.getOut().println("<li><a href='" + contextPath + ImageFilter.GALLERIES_PATH + gallery + "index.html'>" + galleryConfig.getIndexTitle() + "</a></li>");
					}
				}
				catch (UnauthorizedException e){}
			}
			pageContext.getOut().println("</ul>");

			pageContext.getOut().println();
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
