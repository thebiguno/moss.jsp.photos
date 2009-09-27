/**
 * 
 */
package ca.digitalcave.moss.jsp.photos;

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
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

public class ImageConverterCallable implements Serializable, Callable<byte[]> {
		public static final long serialVersionUID = 0;
		
		private final byte[] originalImage;
		private int size;
		private final String sizeType;
		private final int quality;
		
		public ImageConverterCallable(byte[] originalImage, int size, String sizeType, int quality) {
			this.originalImage = originalImage;
			this.size = size;
			this.sizeType = sizeType;
			this.quality = quality;
		}
		
		public byte[] call() throws Exception {
			try {
				BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(originalImage));
				bis.mark(bis.available() + 1);

				BufferedImage bi = ImageIO.read(bis);
				bis.reset();


				
				//Scale the image
				Dimension d = getScaledDimension(bi);
				bi = getBufferedImage(bi.getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH));
			    

				//Try to load metadata; used for determining image rotation
			    Metadata metadata = null;
			    try {
			    	//Metadata will only work for .jpg images; ignore exceptions.
			    	metadata = JpegMetadataReader.readMetadata(bis);
			    }
			    catch (JpegProcessingException jpe) {}
				
				if (metadata != null){
					//Create the transform object
					AffineTransform transform = new AffineTransform();
					
					Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
					try {
						if (exifDirectory.containsTag(ExifDirectory.TAG_ORIENTATION)){
							int orientation = exifDirectory.getInt(ExifDirectory.TAG_ORIENTATION);
							switch (orientation) {
							case 3:
								//180 Degrees
						    	transform.translate(bi.getWidth(), bi.getHeight());
						    	transform.quadrantRotate(2);
								break;
							case 6:
								//90 Degrees
						    	transform.translate(bi.getHeight(), 0);
						    	transform.quadrantRotate(1);
								break;
							case 8:
								//270 Degrees
						    	transform.translate(0, bi.getWidth());
						    	transform.quadrantRotate(3);
								break;

							default:
								//Do nothing
								break;
							}
						}
						
						//Do the actual transformations
						RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
						AffineTransformOp op = new AffineTransformOp(transform, renderingHints);
			    		//BufferedImage dst = op.createCompatibleDestImage(bi, ColorModel.getRGBdefault());
						bi = op.filter(bi, null);
					} 
					catch (MetadataException me){} //No big deal if metadata is not set
					
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
			catch (IOException ioe){
				throw new RuntimeException(ioe);
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
		private Dimension getScaledDimension(BufferedImage i){
			Dimension d;
			if ("w".equals(sizeType)){
				d = new Dimension(size, size / i.getHeight() * i.getWidth());
			}
			else if ("h".equals(sizeType)){
				d = new Dimension(size / i.getWidth() * i.getHeight(), size); 				
			}
			else {
				//We calculate the size of the new image based off the hypotenuse,
				// scaled such that longer / thin images (those with a higher width 
				// to height ratio / height to width ratio, depending on which is larger)
				// get scaled up more than square images.
				double angle = Math.atan((double) i.getHeight() / (double) i.getWidth());

				double newHeight = this.size * Math.sin(angle);
				double newWidth = this.size * Math.cos(angle);

				double scaleFactor = 1 + Math.log10((Math.max((double) i.getWidth(), i.getHeight()) / Math.min((double) i.getWidth(), i.getHeight())));

				d = new Dimension((int) (newWidth * scaleFactor), (int) (newHeight * scaleFactor));
			}
			
			return d;
		}
	}