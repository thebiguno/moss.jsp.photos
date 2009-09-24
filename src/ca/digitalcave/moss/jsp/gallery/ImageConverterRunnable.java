/**
 * 
 */
package ca.digitalcave.moss.jsp.gallery;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import ca.digitalcave.moss.image.ImageFunctions;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

public class ImageConverterRunnable implements Serializable, Callable<byte[]> {
		public static final long serialVersionUID = 0;
		
		private final byte[] originalImage;
		private int size;
		private final String sizeType;
		private final int quality;
//		private final static Semaphore semaphore = new Semaphore(5);
		
		public ImageConverterRunnable(byte[] originalImage, int size, String sizeType, int quality) {
			this.originalImage = originalImage;
			this.size = size;
			this.sizeType = sizeType;
			this.quality = quality;
		}
		
		public byte[] call() throws Exception {
			try {
//				semaphore.acquire();
				System.out.println("Converting image...");
				BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(originalImage));
				bis.mark(bis.available() + 1);

				BufferedImage bi = ImageIO.read(bis);
				bis.reset();

				//Metadata will only work for .jpg images; ignore exceptions.
				Metadata metadata = null;
				try {
					metadata = JpegMetadataReader.readMetadata(bis);
				}
				catch (JpegProcessingException jpe) {}

				//String title = null;
				int rotationDegrees = 0;

				//Try to load metadata
				if (metadata != null){
					//Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
					//title = iptcDirectory.getString(IptcDirectory.TAG_HEADLINE);

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
								rotationDegrees = 0;
							break;
							}
						}
					} 
					catch (MetadataException me){} //No big deal if metadata is not set
				}

				double w = bi.getWidth();
				double h = bi.getHeight();
				
				if ("w".equals(sizeType)){
					if (h > w)
						size = (int) ((size / h) * w); 
				}
				else if ("h".equals(sizeType)){
					if (w > h)
						size = (int) ((size / h) * w); 				
				}
				else {
					//We calculate the size of the new image based off the hypotenuse,
					// scaled such that longer / thin images (those with a higher width 
					// to height ratio / height to width ratio, depending on which is larger)
					// get scaled up more than square images.
					double angle = Math.atan(h / w);

					double newHeight = size * Math.sin(angle);
					double newWidth = size * Math.cos(angle);

					double scaleFactor = 1 + Math.log10((Math.max(w, h) / Math.min(w, h)));

					size = (int) (Math.max(newWidth, newHeight) * scaleFactor);
				}
				
				if (size != 0)
					bi = ImageFunctions.scaleImage(bi, size);
				
				if (rotationDegrees != 0)
					bi = ImageFunctions.rotate(bi, rotationDegrees);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageFunctions.writeImage(bi, bos, (quality / 100f), "jpg");
				bos.flush();
//				semaphore.release();
				return bos.toByteArray(); 
			}
			catch (IOException ioe){
//				semaphore.release();
				throw new RuntimeException(ioe);
			}
//			catch (InterruptedException ie){
//				throw new RuntimeException(ie);
//			}
		}
	}