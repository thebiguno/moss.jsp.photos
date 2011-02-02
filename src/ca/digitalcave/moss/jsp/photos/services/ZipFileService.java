package ca.digitalcave.moss.jsp.photos.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ca.digitalcave.moss.common.StreamUtil;
import ca.digitalcave.moss.jsp.photos.Common;

public class ZipFileService {
	/**
	 * Given a request URI, returns a .zip file containing all the high quality images.  The request
	 * URI must be in the form:
	 *  /galleries/package/name/all.zip
	 * where package/name is the path to the source gallery, relative to WEB-INF/galleries.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void doServe(HttpServletRequest request, HttpServletResponse response, FilterConfig config) throws IOException{
		String requestURI = request.getRequestURI();
		
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + Common.GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");

		//Check the gallery for gallery-specific settings.  If full resolution downloads are not allowed, return 403.
		InputStream settings = config.getServletContext().getResourceAsStream("/WEB-INF" + Common.GALLERIES_PATH + packageName + "/settings.xml");
		if (settings == null){
			//You must have a settings.xml file, or you cannot access the content.
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);
				
				
				Boolean fullQualityAllowed = Common.getBooleanSettings(doc, "full-quality", "allowed");
				if (fullQualityAllowed == null || fullQualityAllowed == false){
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
			}
			catch (Exception e){
				Logger logger = Logger.getLogger(ZipFileService.class.getName());
				logger.log(Level.WARNING, "Error while opening settings.xml for gallery package " + packageName, e);
			}
		}
		
		//Create the zip file
		ByteArrayOutputStream zipTemp = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(zipTemp);
		
		//Get a list of files from the context path
		List<String> images = new ArrayList<String>(request.getSession().getServletContext().getResourcePaths("/WEB-INF/galleries" + packageName));
		
		for (String image : images) {
			if (image.toLowerCase().matches("^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$")){
				InputStream imageInputStream = config.getServletContext().getResourceAsStream(image);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamUtil.copyStream(imageInputStream, baos);
				String fileName = image.replaceAll("/.*/", "");
				ZipEntry zipEntry = new ZipEntry(packageName.replaceAll("^/", "") + "/" + fileName);
				zipEntry.setSize(baos.size());
				zout.putNextEntry(zipEntry);
				zout.write(baos.toByteArray());
				zout.closeEntry();
			}
		}
		
		zout.finish();
		zout.flush();
		
		response.setContentLength(zipTemp.size());
		response.setContentType("application/zip");
		response.addHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Disposition","inline; filename=" + packageName.replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("^_", "").replaceAll("_$", "") + ".zip;");
		
		response.getOutputStream().write(zipTemp.toByteArray());

		return;
	}
}
