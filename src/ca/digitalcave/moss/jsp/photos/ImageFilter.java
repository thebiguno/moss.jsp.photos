/*
 * Created on May 28, 2008 by wyatt
 */
package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.common.StreamUtil;
import ca.digitalcave.moss.jsp.photos.services.ImageService;
import ca.digitalcave.moss.jsp.photos.services.RssService;
import ca.digitalcave.moss.jsp.photos.services.SingleImageService;
import ca.digitalcave.moss.jsp.photos.services.ZipFileService;

public class ImageFilter implements Filter {
	private final static String CSS_PATH = "/css";
	private FilterConfig config;
	
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		//Built in script / style components
		for (String path : new String[]{Common.JAVASCRIPT_PATH, CSS_PATH, Common.IMAGE_PATH}) {
			if (request.getRequestURI().matches(config.getServletContext().getContextPath() + path + "/.*\\.[a-zA-Z]{2,3}")){
				String name = request.getRequestURI().replaceAll(config.getServletContext().getContextPath() + path + "/", "");
				InputStream is = ImageFilter.class.getResourceAsStream("resources/" + name);
				if (is != null){
					StreamUtil.copyStream(is, res.getOutputStream());
					return;
				}
			}
		}

		//Gallery images zip - located under /galleries/<packageName>/all.zip
		if (request.getRequestURI().matches(config.getServletContext().getContextPath() + Common.GALLERIES_PATH + "/.+/all\\.zip")){
			ZipFileService.doServe(request, response, config);
			return;
		}
		
		//Single page gallery image with metadata; located under /galleries/<packageName>/<image_name>.jsp
		if (request.getRequestURI().matches(config.getServletContext().getContextPath() + Common.GALLERIES_PATH + "/.+/.+\\.jsp")){
			SingleImageService.doServe(request, response, config);
			return;
		}		
		
		//RSS filter - located under /galleries/<packageName>/gallery.rss
		else if (request.getRequestURI().toLowerCase().endsWith("/gallery.rss")){
			RssService.doServe(request, response, config);
			return;
		}
		
		//Gallery images - located under /galleries/<packageName>/<filename>
		else if (request.getRequestURI().matches(config.getServletContext().getContextPath() + Common.GALLERIES_PATH + "/.+" )){
			ImageService.doServe(request, response, config);
			return;
		}
		
		chain.doFilter(request, response);
	}
	
	public void destroy() {
		config = null;
	}
}
