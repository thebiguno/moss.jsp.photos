package ca.digitalcave.moss.jsp.photos.model;

public class GalleryConfig {
	private String indexTemplate;
	private String indexTitle;
	private int indexSize;
	private int indexQuality;
	private int indexThumbSize;
	private int indexThumbQuality;
	private boolean indexCenter;
	private String indexOrder;
	private boolean indexShowTitle;
	private boolean indexShowCaption;
	private boolean indexShowDate;
	private boolean indexShowFilename;
	private boolean indexShowDownload;
	private boolean indexSlideshow;
	private int indexSlideshowDelay; //Only used if indexSlideshow = true
	private boolean indexSlideshowOverride; //If the user clicks, stop slideshow
	
	private String singleImageTemplate;
	
	private int imageMaxSize;
	private int imageMaxQuality;
	private int imageMinSize;
	private int imageMinQuality;
	
	private boolean fullQualityAllowed;
	
	private boolean zipAllowed;
	
	private int rssSize;
	private int rssQuality;
	private String rssTitle;
	private String rssDescription;
	private String rssLink;
	
	public String getIndexTemplate() {
		return indexTemplate;
	}
	public void setIndexTemplate(String indexTemplate) {
		this.indexTemplate = indexTemplate;
	}
	public String getSingleImageTemplate() {
		return singleImageTemplate;
	}
	public void setSingleImageTemplate(String singleImageTemplate) {
		this.singleImageTemplate = singleImageTemplate;
	}
	public int getImageMaxSize() {
		return imageMaxSize;
	}
	public void setImageMaxSize(int imageMaxSize) {
		this.imageMaxSize = imageMaxSize;
	}
	public int getImageMaxQuality() {
		return imageMaxQuality;
	}
	public void setImageMaxQuality(int imageMaxQuality) {
		this.imageMaxQuality = imageMaxQuality;
	}
	public int getImageMinSize() {
		return imageMinSize;
	}
	public void setImageMinSize(int imageMinSize) {
		this.imageMinSize = imageMinSize;
	}
	public int getImageMinQuality() {
		return imageMinQuality;
	}
	public void setImageMinQuality(int imageMinQuality) {
		this.imageMinQuality = imageMinQuality;
	}
	public boolean isFullQualityAllowed() {
		return fullQualityAllowed;
	}
	public void setFullQualityAllowed(boolean fullQualityAllowed) {
		this.fullQualityAllowed = fullQualityAllowed;
	}
	public boolean isShowZip() {
		return zipAllowed;
	}
	public void setZipAllowed(boolean zipAllowed) {
		this.zipAllowed = zipAllowed;
	}
	public int getRssSize() {
		return rssSize;
	}
	public void setRssSize(int rssSize) {
		this.rssSize = rssSize;
	}
	public int getRssQuality() {
		return rssQuality;
	}
	public void setRssQuality(int rssQuality) {
		this.rssQuality = rssQuality;
	}
	public String getRssTitle() {
		return rssTitle;
	}
	public void setRssTitle(String rssTitle) {
		this.rssTitle = rssTitle;
	}
	public String getRssDescription() {
		return rssDescription;
	}
	public void setRssDescription(String rssDescription) {
		this.rssDescription = rssDescription;
	}
	public String getRssLink() {
		return rssLink;
	}
	public void setRssLink(String rssLink) {
		this.rssLink = rssLink;
	}
	/**
	 * Derived property based on status of indexTemplate
	 * @return
	 */
	public boolean isShowIndex() {
		return getIndexTemplate() != null && getIndexTemplate().length() > 0;
	}
	/**
	 * Derived property based on status of singleImageTemplate
	 * @return
	 */
	public boolean isShowSingleImage() {
		return getSingleImageTemplate() != null && getSingleImageTemplate().length() > 0;
	}
	/**
	 * Derived property based on presence of all required RSS parameters
	 * @return
	 */
	public boolean isShowRss() {
		return getRssTitle() != null && getRssDescription() != null && getRssLink() != null;
	}
	public int getIndexSize() {
		return indexSize;
	}
	public void setIndexSize(int indexSize) {
		this.indexSize = indexSize;
	}
	public int getIndexQuality() {
		return indexQuality;
	}
	public void setIndexQuality(int indexQuality) {
		this.indexQuality = indexQuality;
	}
	public int getIndexThumbSize() {
		return indexThumbSize;
	}
	public void setIndexThumbSize(int indexThumbSize) {
		this.indexThumbSize = indexThumbSize;
	}
	public int getIndexThumbQuality() {
		return indexThumbQuality;
	}
	public void setIndexThumbQuality(int indexThumbQuality) {
		this.indexThumbQuality = indexThumbQuality;
	}
	public boolean isZipAllowed() {
		return zipAllowed;
	}
	public boolean isIndexCenter() {
		return indexCenter;
	}
	public void setIndexCenter(boolean indexCenter) {
		this.indexCenter = indexCenter;
	}
	public String getIndexOrder() {
		return indexOrder;
	}
	public void setIndexOrder(String indexOrder) {
		this.indexOrder = indexOrder;
	}
	public boolean isIndexShowTitle() {
		return indexShowTitle;
	}
	public void setIndexShowTitle(boolean indexShowTitle) {
		this.indexShowTitle = indexShowTitle;
	}
	public boolean isIndexShowCaption() {
		return indexShowCaption;
	}
	public void setIndexShowCaption(boolean indexShowCaption) {
		this.indexShowCaption = indexShowCaption;
	}
	public boolean isIndexShowDate() {
		return indexShowDate;
	}
	public void setIndexShowDate(boolean indexShowDate) {
		this.indexShowDate = indexShowDate;
	}
	public boolean isIndexShowFilename() {
		return indexShowFilename;
	}
	public void setIndexShowFilename(boolean indexShowFilename) {
		this.indexShowFilename = indexShowFilename;
	}
	public boolean isIndexShowDownload() {
		return indexShowDownload;
	}
	public void setIndexShowDownload(boolean indexShowDownload) {
		this.indexShowDownload = indexShowDownload;
	}
	public boolean isIndexSlideshow() {
		return indexSlideshow;
	}
	public void setIndexSlideshow(boolean indexSlideshow) {
		this.indexSlideshow = indexSlideshow;
	}
	public int getIndexSlideshowDelay() {
		return indexSlideshowDelay;
	}
	public void setIndexSlideshowDelay(int indexSlideshowDelay) {
		this.indexSlideshowDelay = indexSlideshowDelay;
	}
	public boolean isIndexSlideshowOverride() {
		return indexSlideshowOverride;
	}
	public void setIndexSlideshowOverride(boolean indexSlideshowOverride) {
		this.indexSlideshowOverride = indexSlideshowOverride;
	}
	public String getIndexTitle() {
		return indexTitle;
	}
	public void setIndexTitle(String indexTitle) {
		this.indexTitle = indexTitle;
	}
}
