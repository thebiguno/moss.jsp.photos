/**
 * 
 */
package org.homeunix.thecave.moss.jsp.lightbox;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.FilterConfig;

import org.homeunix.thecave.moss.image.ImageFunctions;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

/**
 * Class to handle resizing and loading of images in a thread-safe manner.
 * @author wyatt
 *
 */
class GalleryManager {

	private final Set<String> conversionKeys = Collections.synchronizedSet(new HashSet<String>());
	private final Map<String, byte[]> cache = Collections.synchronizedMap(new HashMap<String, byte[]>()); 
	
	private GalleryManager() {}

	private static class GalleryManagerSingletonHolder { 
		private static final GalleryManager INSTANCE = new GalleryManager();
	}

	public static GalleryManager getInstance() {
		return GalleryManagerSingletonHolder.INSTANCE;
	}
		
	/**
	 * Given a request URI, returns the image associated with the request.
	 * The request URI must be in the form:
	 *  /galleries/path/to/gallery/baseimage_ext_size_quality.jpg
	 * where path/to/gallery is the path to the source gallery, relative
	 * to the context root, basename is the image name, ext is the original 
	 * image extension, size is the size of the scaled version, and quality 
	 * is the quality of the scaled version (100 is full quality (largest size),
	 * 0 is lowest quality (smallest image). 
	 * @param requestURI
	 * @param context File referencing the root of the webapp.  If null, we will look 
	 * for images in the classpath, and cache images in memory only. 
	 * @return
	 */
	public InputStream getImageInputStream(String requestURI, FilterConfig config){
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + ImageFilter.GALLERIES_PATH, "");
		
		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");
		String requestUriFile = requestUriWithoutContextAndGalleriesPrefix.replaceAll("^/.*/", "").replaceAll("\\.jpg$", "");

		String[] split = requestUriFile.split("_");
		if (split.length < 4)
			return new ByteArrayInputStream(new byte[0]);
		
		String qualityString = split[split.length - 1];
		String sizeString = split[split.length - 2];
		int size = Integer.parseInt(sizeString);
		float quality = ((float) Integer.parseInt(qualityString) / 100f);
		String ext = split[split.length - 3];
		String baseName = requestUriFile.replaceAll("_" + ext + "_" + sizeString + "_" + qualityString, "");
		
		return getImageInputStream(packageName, baseName, ext, size, quality);
	}
	
	/**
	 * Returns an input stream according to packageName, baseName, ext, size, 
	 * and quality.
	 * @param packageName
	 * @param baseName
	 * @param ext
	 * @param size
	 * @param quality
	 * @param context
	 * @return
	 */
	public InputStream getImageInputStream(String packageName, String baseName, String ext, int size, float quality){
		//If context is null, we assume we are running from a .war; otherwise, we
		// can access the files within context.
		String cacheKey = packageName + baseName + "_" + ext + "_" + size + "_" + ((int) (quality * 100)) + ".jpg"; 

		//If the image has not been cached, we need to convert it and cache it.
		if (cache.get(cacheKey) == null){
			//We load source images from the classpath
			InputStream is = GalleryManager.class.getResourceAsStream(packageName + baseName + "." + ext);

			if (is == null)
				throw new RuntimeException("Could not find image.");

			conversionKeys.add(cacheKey);
			byte[] cachedImage = convertImage(is, size, quality);
			cache.put(cacheKey, cachedImage);
			conversionKeys.remove(cacheKey);
		}
		
		//Return the (now) cached image.
		return new ByteArrayInputStream(cache.get(cacheKey));
	}
	
	/**
	 * Returns a byte array containing the converted image, at the requested
	 * size and quality.  The input stream points to the source image. 
	 * @param is
	 * @param size
	 * @param quality
	 * @return
	 */
	private byte[] convertImage(InputStream is, int size, float quality){
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			bis.mark(bis.available() + 1);

			BufferedImage bi = ImageIO.read(bis);
			bis.reset();

			//Metadata will only work for .jpg images; ignore exceptions.
			Metadata metadata = null;
			try {
				metadata = JpegMetadataReader.readMetadata(bis);
			}
			catch (JpegProcessingException jpe) {}

//			String title = null;
			int rotationDegrees = 0;

			//Try to load metadata
			if (metadata != null){
//				Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
//				title = iptcDirectory.getString(IptcDirectory.TAG_HEADLINE);

				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				try {
					int orientation = exifDirectory.getInt(ExifDirectory.TAG_ORIENTATION);
					switch (orientation) {
					case 3:
						rotationDegrees = 180;
						break;
					case 6:
						rotationDegrees = 90;
						break;
					case 8:
						rotationDegrees = 270;
						break;

					default:
						rotationDegrees = 0;
					break;
					}
				}
				catch (MetadataException me){
					throw new RuntimeException(me);
				}
			}

			bi = ImageFunctions.scaleImage(bi, size);
			if (rotationDegrees != 0){
				bi = ImageFunctions.rotate(bi, rotationDegrees);
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageFunctions.writeImage(bi, bos, quality, "jpg");
			bos.flush();
			
			return bos.toByteArray(); 
		}
		catch (IOException ioe){
			throw new RuntimeException(ioe);
		}
	}
}