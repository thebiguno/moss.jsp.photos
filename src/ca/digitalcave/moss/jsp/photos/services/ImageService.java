package ca.digitalcave.moss.jsp.photos.services;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.common.StreamUtil;
import ca.digitalcave.moss.jsp.photos.ImageFilter;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

public class ImageService implements GalleryService {

	private final static ExecutorService executor = new ThreadPoolExecutor(0, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

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
	public void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException{
		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);

		if (servletContext == null) {
			throw new IOException("Required values not in request attribtues");
		}		
		
		try {
			ImageParams ip = ImageFilter.getImageParams(null, request.getRequestURI(), servletContext);

			//We load source images from the context path
			InputStream is = servletContext.getResourceAsStream(("/WEB-INF" + ImageFilter.GALLERIES_PATH + ip.getGalleryName() + ip.getBaseName() + "." + ip.getExtension()).replaceAll("%20", " "));

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

				try {
					ImageConverterCallable callable = new ImageConverterCallable(baos.toByteArray(), ip.getSize(), ip.getQuality());
					Future<byte[]> future = executor.submit(callable);
					convertedImage = future.get();
				}
				catch (Exception e){}
				
				if (convertedImage != null)
					StreamUtil.copyStream(new ByteArrayInputStream(convertedImage), response.getOutputStream());
			}
		}
		catch (UnauthorizedException e){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	/**
	 * Helper class, to spread out image requests to not be executed all concurrently.  Used with
	 * the executor service.
	 *  
	 * @author wyatt
	 */
	private class ImageConverterCallable implements Callable<byte[]> {
		private final byte[] data;
		private final int size;
		private final int quality;
		
		public ImageConverterCallable(byte[] data, int size, int quality) {
			this.data = data;
			this.size = size;
			this.quality = quality;
		}
		
		@Override
		public byte[] call() throws Exception {
			return convertImage(data, size, "jpg", quality);
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
	private byte[] convertImage(byte[] originalImage, int size, String sizeType, int quality){
		try {
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(originalImage));
			bis.mark(bis.available() + 1);

			BufferedImage bi = ImageIO.read(bis);
			bis.reset();

			//Try to load metadata; used for determining image rotation
			Metadata metadata = null;
			try {
				//Metadata will only work for .jpg images; ignore exceptions.
				metadata = JpegMetadataReader.readMetadata(bis);
			}
			catch (JpegProcessingException jpe) {}

			int rotationDegrees = 0;
			if (metadata != null){					
				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				try {
					if (exifDirectory.containsTag(ExifDirectory.TAG_ORIENTATION)){
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
							//Do nothing
							break;
						}
					}
				} 
				catch (MetadataException me){} //No big deal if metadata is not set

			}

			//Find the image dimensions based on requirements
			if (rotationDegrees == 90 || rotationDegrees == 270){
				if ("w".equals(sizeType))
					sizeType = "h";
				else if ("h".equals(sizeType))
					sizeType = "w";
			}
			Dimension d = getScaledDimension(size, bi.getWidth(), bi.getHeight(), sizeType);


			bi = getBufferedImage(bi.getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH));

			if (rotationDegrees != 0){
				//Create the transform object
				AffineTransform transform = new AffineTransform();
				switch (rotationDegrees) {
				case 180:
					//180 Degrees
					transform.translate(bi.getWidth(), bi.getHeight());
					transform.rotate(Math.toRadians(180));
					break;
				case 90:
					//90 Degrees
					transform.translate(bi.getHeight(), 0);
					transform.rotate(Math.toRadians(90));
					break;
				case 270:
					//270 Degrees
					transform.translate(0, bi.getWidth());
					transform.rotate(Math.toRadians(270));
					break;
				}				
				//Do the actual transformations
				RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
				AffineTransformOp op = new AffineTransformOp(transform, renderingHints);
				//BufferedImage dst = op.createCompatibleDestImage(bi, ColorModel.getRGBdefault());
				bi = op.filter(bi, null);
			}


			//Get the ImageWriter which allows us to set quality, and write the image
			ByteArrayOutputStream bos = new ByteArrayOutputStream();				
			Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpg");
			ImageWriter imageWriter = writers.next();
			ImageWriteParam imageWriterParam = imageWriter.getDefaultWriteParam();
			imageWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriterParam.setCompressionQuality(quality / 100f);
			ImageOutputStream output = new MemoryCacheImageOutputStream(bos);
			IIOImage image = new IIOImage(bi, null, null);
			imageWriter.setOutput(output);
			imageWriter.write(null, image, imageWriterParam);
			bos.flush();
			return bos.toByteArray(); 
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage getBufferedImage(Image img){
		BufferedImage bi = new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_RGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(img, 0, 0, null);
		bg.dispose();
		return bi;
	}

	/**
	 * This returns the amount to scale the image, based on the given
	 * width and height of the current image.  The desired size is provided
	 * by the img tag parameters in the HTML file.  Depending on whether
	 * the size type is 'h', 'w', or null, we will either set the scale
	 * based off of height, width, or a calculation of the hypotenuse.
	 */
	private Dimension getScaledDimension(int size, int w, int h, String sizeType){
		Dimension d;
		if ("w".equals(sizeType)){
			d = new Dimension(size, (int) ((double) h / w * size));
		}
		else if ("h".equals(sizeType)){
			d = new Dimension((int) ((double) w / h * size), size); 				
		}
		else {
			//We calculate the size of the new image based off the hypotenuse,
			// scaled such that longer / thin images (those with a higher width 
			// to height ratio / height to width ratio, depending on which is larger)
			// get scaled up more than square images.
			double angle = Math.atan((double) h / (double) w);

			double newHeight = size * Math.sin(angle);
			double newWidth = size * Math.cos(angle);

			double scaleFactor = 1 + Math.log10((Math.max((double) w, h) / Math.min((double) w, h)));

			d = new Dimension((int) (newWidth * scaleFactor), (int) (newHeight * scaleFactor));
		}

		return d;
	}	
}
