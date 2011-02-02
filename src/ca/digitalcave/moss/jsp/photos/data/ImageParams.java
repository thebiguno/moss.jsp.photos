package ca.digitalcave.moss.jsp.photos.data;

public class ImageParams {
	private final String packageName;
	private final String baseName;
	private final String extension;
	private final boolean fullQuality;
	private final int size;
	private final int quality;
	public ImageParams(String packageName, String baseName, String extension, boolean fullQuality, int size, int quality) {
		super();
		this.packageName = packageName;
		this.baseName = baseName;
		this.extension = extension;
		this.fullQuality = fullQuality;
		this.size = size;
		this.quality = quality;
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
}