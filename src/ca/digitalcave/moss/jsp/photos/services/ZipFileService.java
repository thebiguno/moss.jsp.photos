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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.common.StreamUtil;
import ca.digitalcave.moss.jsp.photos.ImageFilter;
import ca.digitalcave.moss.jsp.photos.model.GalleryConfig;

public class ZipFileService implements GalleryService {
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
	public void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException{
		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);
		final GalleryConfig galleryConfig = (GalleryConfig) request.getAttribute(ImageFilter.ATTR_GALLERY_CONFIG);
		final String galleryName = (String) request.getAttribute(ImageFilter.ATTR_GALLERY_NAME);
		
		if (servletContext == null || galleryConfig == null || galleryName == null) {
			throw new IOException("Required values not in request attribtues");
		}
		
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		
		//Check the gallery for gallery-specific settings.  If full resolution downloads are not allowed, return 403.
		try {				
			if (!galleryConfig.isFullQualityAllowed()){
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		catch (Exception e){
			Logger logger = Logger.getLogger(ZipFileService.class.getName());
			logger.log(Level.WARNING, "Error while opening settings.xml for gallery " + galleryName, e);
		}
		
		//Create the zip file
		ByteArrayOutputStream zipTemp = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(zipTemp);
		
		//Get a list of files from the context path
		List<String> images = new ArrayList<String>(request.getSession().getServletContext().getResourcePaths("/WEB-INF/galleries" + galleryName));
		
		for (String image : images) {
			if (image.toLowerCase().matches("^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$")){
				InputStream imageInputStream = servletContext.getResourceAsStream(image);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamUtil.copyStream(imageInputStream, baos);
				String fileName = image.replaceAll("/.*/", "");
				ZipEntry zipEntry = new ZipEntry(galleryName.replaceAll("^/", "") + "/" + fileName);
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
		response.setHeader("Content-Disposition","inline; filename=" + galleryName.replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("^_", "").replaceAll("_$", "") + ".zip;");
		
		response.getOutputStream().write(zipTemp.toByteArray());

		return;
	}
}
