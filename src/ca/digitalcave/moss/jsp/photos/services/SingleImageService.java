package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.jsp.photos.Common;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

public class SingleImageService {
	/**
	 * Serves a single image.  This is mostly used as the target of RSS links.  Not formatting, etc, just a 
	 * simple raw HTML page with image and metadata.
	 */
	public static void doServe(HttpServletRequest request, HttpServletResponse response, FilterConfig config) throws IOException {

		String requestURI = request.getRequestURI();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			
			ImageParams imageParams = Common.getImageParams(requestURI, config.getServletContext());
			
			if (imageParams.getTitle() != null){
				sb.append("<h1>");
				sb.append(Common.escapeHtml(imageParams.getTitle()));
				sb.append("</h1>");
			}
			
			if (imageParams.getCaption() != null){
				sb.append("<p>");
				sb.append(Common.escapeHtml(imageParams.getCaption()));
				sb.append("</p>");
			}
			
			sb.append("<img src='");
			sb.append(requestURI.replaceAll("jsp$", "jpg"));
			sb.append("'>");

			if (imageParams.getCaptureDate() != null){
				sb.append("<p>");
				sb.append(new SimpleDateFormat("yyyy-MM-dd").format(imageParams.getCaptureDate()));
				sb.append("</p>");
			}

			sb.append("</body></html>");
			
			response.getOutputStream().println(sb.toString());
		}
		catch (UnauthorizedException e){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
