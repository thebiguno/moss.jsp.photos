package ca.digitalcave.moss.jsp.photos.model;

import java.util.Date;

public class ImageParams {
	private final String packageName;
	private final String baseName;
	private final String extension;
	private final boolean fullQuality;
	private final int size;
	private final int quality;
	private final String title;
	private final String caption;
	private final Date captureDate;
	
	public ImageParams(String packageName, String baseName, String extension, boolean fullQuality, int size, int quality, String title, String caption, Date captureDate) {
		super();
		this.packageName = packageName;
		this.baseName = baseName;
		this.extension = extension;
		this.fullQuality = fullQuality;
		this.size = size;
		this.quality = quality;
		this.title = title;
		this.caption = caption;
		this.captureDate = captureDate;
	}
	public String getPackageName() {
		return packageName;
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
}