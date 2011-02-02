package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.jsp.photos.Common;
import ca.digitalcave.moss.jsp.photos.ImageMetadata;
import ca.digitalcave.moss.jsp.photos.data.ImageParams;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;

public class SingleImageService {
	/**
	 * Serves a single image.  This is mostly used as the target of RSS links.  Not formatting, etc, just a 
	 * simple raw HTML page with image and metadata.
	 */
	public static void doServe(HttpServletRequest request, HttpServletResponse response, FilterConfig config) throws IOException {
		response.getOutputStream().println("<html><body>");

		String requestURI = request.getRequestURI();
		try {
			ImageParams ip = Common.getImageParams(requestURI, config);
			InputStream is = config.getServletContext().getResourceAsStream(("/WEB-INF" + Common.GALLERIES_PATH + ip.getPackageName() + ip.getBaseName() + "." + ip.getExtension()).replaceAll("%20", " "));
			ImageMetadata im = Common.getImageMetadata(is);
			
			if (im.getTitle() != null){
				response.getOutputStream().println("<h1>");
				response.getOutputStream().println(Common.escapeHtml(im.getTitle()));
				response.getOutputStream().println("</h1>");
			}
			
			if (im.getCaption() != null){
				response.getOutputStream().println("<p>");
				response.getOutputStream().println(Common.escapeHtml(im.getCaption()));
				response.getOutputStream().println("</p>");
			}
			
			response.getOutputStream().println("<img src='");
			response.getOutputStream().println(requestURI.replaceAll("jsp$", "jpg"));
			response.getOutputStream().println("'>");

			if (im.getCaptureDate() != null){
				response.getOutputStream().println("<p>");
				response.getOutputStream().println(new SimpleDateFormat("yyyy-MM-dd").format(im.getCaptureDate()));
				response.getOutputStream().println("</p>");
			}

			response.getOutputStream().println("</body></html>");
		}
		catch (UnauthorizedException e){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
