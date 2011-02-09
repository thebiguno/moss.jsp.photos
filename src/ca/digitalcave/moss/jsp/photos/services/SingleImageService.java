package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.jsp.photos.ImageFilter;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.GalleryConfig;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

public class SingleImageService implements GalleryService {
	/**
	 * Serves a single image.  This is mostly used as the target of RSS links.  It uses the template specified
	 * in the settings.xml file under the 'singleImage' element, 'template' attribute.  This template can use the
	 * request attributes title, caption, imageSource, and date.
	 */
	public void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException {

		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);
		final GalleryConfig galleryConfig = (GalleryConfig) request.getAttribute(ImageFilter.ATTR_GALLERY_CONFIG);
		
		if (servletContext == null || galleryConfig == null) {
			throw new IOException("Required values not in request attribtues");
		}
		
		
		final String requestURI = request.getRequestURI();
		try {
			ImageParams imageParams = ImageFilter.getImageParams(null, requestURI, servletContext);
			
			final String title = ImageFilter.escapeHtml(imageParams.getTitle());
			final String caption = ImageFilter.escapeHtml(imageParams.getCaption());
			final String imageSource = requestURI.replaceAll("html$", "jpg");
			final String date = new SimpleDateFormat("yyyy-MM-dd").format(imageParams.getCaptureDate());
			
			RequestDispatcher rd = servletContext.getRequestDispatcher(galleryConfig.getSingleImageTemplate());
			request.setAttribute("title", title);
			request.setAttribute("caption", caption);
			request.setAttribute("imageSource", imageSource);
			request.setAttribute("date", date);
			
			rd.include(request, response);
		}
		catch (UnauthorizedException e){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
		catch (ServletException e){
			throw new IOException(e);
		}
	}
}
