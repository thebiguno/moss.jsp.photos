package ca.digitalcave.moss.jsp.photos.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.common.StreamUtil;
import ca.digitalcave.moss.jsp.photos.Common;
import ca.digitalcave.moss.jsp.photos.ImageConverterCallable;
import ca.digitalcave.moss.jsp.photos.data.ImageParams;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;

public class ImageService {

	private final static ExecutorService executor = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	/**
	 * Given a request URI, returns the image associated with the request.
	 * The request URI must be in the form:
	 *  /galleries/package/name/baseimage_ext_size_quality.jpg
	 * where package/name is the path to the source gallery, relative
	 * to WEB-INF/galleries, basename is the image name, ext is the original 
	 * image extension, size is the size of the scaled version, and quality 
	 * is the quality of the scaled version (100 is full quality (largest size),
	 * 0 is lowest quality (smallest image). 
	 */
	public static void doServe(HttpServletRequest request, HttpServletResponse response, FilterConfig config) throws IOException{
		String requestURI = request.getRequestURI();
		
		try {
			ImageParams ip = Common.getImageParams(requestURI, config);

			//We load source images from the context path
			InputStream is = config.getServletContext().getResourceAsStream(("/WEB-INF" + Common.GALLERIES_PATH + ip.getPackageName() + ip.getBaseName() + "." + ip.getExtension()).replaceAll("%20", " "));

			if (is == null){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			if (ip.isFullQuality()){
				response.setContentType("application/octet-stream");
				StreamUtil.copyStream(is, response.getOutputStream());
			}
			else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamUtil.copyStream(is, baos);
				byte[] convertedImage = null;

				convertedImage = convertImage(baos.toByteArray(), ip.getSize(), "jpg", ip.getQuality());
				if (convertedImage != null)
					StreamUtil.copyStream(new ByteArrayInputStream(convertedImage), response.getOutputStream());
			}
		}
		catch (UnauthorizedException e){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	/**
	 * Returns a byte array containing the converted image, at the requested
	 * size and quality.  The input stream points to the source image. 
	 * @param is
	 * @param size
	 * @param quality
	 * @return
	 */
	private static byte[] convertImage(byte[] originalImage, int size, String sizeType, int quality){
		ImageConverterCallable imageConverterRunnable = new ImageConverterCallable(originalImage, size, sizeType, quality);
		try {
			Future<byte[]> future = executor.submit(imageConverterRunnable);
			return future.get();
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
