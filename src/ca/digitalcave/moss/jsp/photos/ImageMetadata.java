package ca.digitalcave.moss.jsp.photos;

import java.util.Date;

public class ImageMetadata {

	private final String title;
	private final String caption;
	private final Date captureDate;
	public ImageMetadata(String title, String caption, Date captureDate) {
		super();
		this.title = title;
		this.caption = caption;
		this.captureDate = captureDate;
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
