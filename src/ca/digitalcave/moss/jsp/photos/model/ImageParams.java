package ca.digitalcave.moss.jsp.photos.model;

import java.util.Date;

public class ImageParams implements Comparable<ImageParams> {
	private final String imagePath;
	private final String galleryName;
	private final String baseName;
	private final String extension;
	private final boolean fullQuality;
	private final int size;
	private final int quality;
	private final String title;
	private final String caption;
	private final Date captureDate;
	
	public ImageParams(String imagePath, String galleryName, String baseName, String extension, boolean fullQuality, int size, int quality, String title, String caption, Date captureDate) {
		super();
		this.imagePath = imagePath;
		this.galleryName = galleryName;
		this.baseName = baseName;
		this.extension = extension;
		this.fullQuality = fullQuality;
		this.size = size;
		this.quality = quality;
		this.title = title;
		this.caption = caption;
		this.captureDate = captureDate;
	}
	public String getImagePath() {
		return imagePath;
	}
	public String getGalleryName() {
		return galleryName;
	}
	public String getBaseName() {
		return baseName;
	}
	public String getExtension() {
		return extension;
	}
	public boolean isFullQuality() {
		return fullQuality;
	}
	public int getSize() {
		return size;
	}
	public int getQuality() {
		return quality;
	}
	public String getTitle() {
		return title;
	}
	public String getCaption() {
		return caption;
	}
	public Date getCaptureDate() {
		return captureDate;
	}
	
	@Override
	public int compareTo(ImageParams o) {
		if (getCaptureDate() != null && o.getCaptureDate() != null)
			return getCaptureDate().compareTo(o.getCaptureDate());
		if (getImagePath() != null && o.getImagePath() != null)
			return getImagePath().compareTo(o.getImagePath());
		return 0;
	}
}