package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.jsp.photos.ImageFilter;
import ca.digitalcave.moss.jsp.photos.model.GalleryConfig;

/**
 * A service which outputs a gallery (configured with the settings.xml) to a template JSP file.
 * The template can access the variables "title", "headers", and "gallery".  "headers" must be
 * placed into the &lt;head&gt; element; "title" is a simple string, which can be placed into a
 * header tag, or the title.   The "gallery" variable is the complete gallery code, including 
 * div's, script's, etc. 
 * 
 * @author wyatt
 */
public class IndexService implements GalleryService {

	@Override
	public void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);
		final GalleryConfig galleryConfig = (GalleryConfig) request.getAttribute(ImageFilter.ATTR_GALLERY_CONFIG);
		final String galleryName = (String) request.getAttribute(ImageFilter.ATTR_GALLERY_NAME);
		
		if (servletContext == null || galleryConfig == null || galleryName == null) {
			throw new IOException("Required values not in request attribtues");
		}
		
		
		RequestDispatcher rd = servletContext.getRequestDispatcher(galleryConfig.getIndexTemplate());
		try {
			request.setAttribute("title", galleryConfig.getIndexTitle());
			request.setAttribute("headers", ImageFilter.getPhotoHeaders(request));
			request.setAttribute("gallery", ImageFilter.getGallery(request));
			rd.include(request, response);
		}
		catch (Exception e){
			throw new IOException(e);
		}
	}
}
