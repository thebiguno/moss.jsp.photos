/*
 * Created on May 28, 2008 by wyatt
 */
package org.homeunix.thecave.moss.jsp.gallery;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.io.Util;

public class ImageFilter implements Filter {
	final static String JAVASCRIPT_PATH = "/js";
	final static String CSS_PATH = "/css";
	final static String IMAGE_PATH = "/images";
	final static String GALLERIES_PATH = "/galleries";
	
	private FilterConfig config; 
	
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {		
		//Built in Lightbox components
		for (String path : new String[]{JAVASCRIPT_PATH, CSS_PATH, IMAGE_PATH}) {
			if (((HttpServletRequest) req).getRequestURI().matches(config.getServletContext().getContextPath() + path + "/.*\\.[a-zA-Z]{2,3}")){
				String name = ((HttpServletRequest) req).getRequestURI().replaceAll(config.getServletContext().getContextPath() + path + "/", "");
				InputStream is = ImageFilter.class.getResourceAsStream("resources/" + name);
				if (is != null){
					Util.copyStream(is, res.getOutputStream());
					return;
				}
			}
		}
		
		//Gallery images - located under /galleries/<location>/filename
		if (((HttpServletRequest) req).getRequestURI().matches(config.getServletContext().getContextPath() + GALLERIES_PATH + "/.+" )){
			Util.copyStream(GalleryManager.getInstance().getImageInputStream(((HttpServletRequest) req).getRequestURI(), config), res.getOutputStream());
			return;
		}
		
		chain.doFilter(req, res);
	}
	
	public void destroy() {
		config = null;
	}
}
