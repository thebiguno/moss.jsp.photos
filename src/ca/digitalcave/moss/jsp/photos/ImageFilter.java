/*
 * Created on May 28, 2008 by wyatt
 */
package ca.digitalcave.moss.jsp.photos;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.digitalcave.moss.common.LogUtil;
import ca.digitalcave.moss.common.StreamUtil;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.Config;
import ca.digitalcave.moss.jsp.photos.model.ConfigFactory;
import ca.digitalcave.moss.jsp.photos.model.GallerySettings;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;
import ca.digitalcave.moss.jsp.photos.services.ImageService;
import ca.digitalcave.moss.jsp.photos.services.IndexService;
import ca.digitalcave.moss.jsp.photos.services.RssService;
import ca.digitalcave.moss.jsp.photos.services.SingleImageService;
import ca.digitalcave.moss.jsp.photos.services.ZipFileService;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;

public class ImageFilter implements Filter {
	private FilterConfig filterConfig;
	private Config config = null;
	private long lastConfigLoad = 0;

	private final static String CSS_PATH = "/css";
	public final static String IMAGE_PATH = "/images";
	public final static String JAVASCRIPT_PATH = "/js";
	public final static String GALLERIES_PATH = "/galleries";
	public final static String IMAGE_SEPARATOR = "%7B";

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}

		if (config == null || lastConfigLoad + 60000 < System.currentTimeMillis()){
			config = ConfigFactory.loadConfig(filterConfig);
			lastConfigLoad = System.currentTimeMillis();
			if (config != null) LogUtil.setLogLevel(config.getLogLevel());
		}
		
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;

		final String requestURI = request.getRequestURI();
		ServletContext servletContext = filterConfig.getServletContext();
		final String contextPath = servletContext.getContextPath();

		//Built in script / style components
		for (String path : new String[]{ImageFilter.JAVASCRIPT_PATH, ImageFilter.CSS_PATH, ImageFilter.IMAGE_PATH}) {
			if (requestURI.matches(contextPath + path + "/.*\\.[a-zA-Z]{2,3}")){
				if (requestURI.endsWith(".js")) res.setContentType("text/javascript");
				else if (requestURI.endsWith(".css")) res.setContentType("text/css");
				String name = requestURI.replaceAll(contextPath + path + "/", "");
				InputStream is = ImageFilter.class.getResourceAsStream("resources/" + name);
				if (is != null){
					StreamUtil.copyStream(is, res.getOutputStream());
					if (ImageFilter.JAVASCRIPT_PATH.equals(path)) response.setContentType("application/x-javascript");
					else if (ImageFilter.CSS_PATH.equals(path)) response.setContentType("text/css");
					return;
				}
			}
		}

		//Various gallery services
		if (requestURI.matches("^" + contextPath + ImageFilter.GALLERIES_PATH + ".*$")){
			//Find path to gallery source
			final String galleryName = requestURI.replaceAll("^" + contextPath + ImageFilter.GALLERIES_PATH, "").replaceAll("[^/]+$", "");

			try {
				GallerySettings galleryConfig = getGalleryConfig(servletContext, galleryName);

				request.setAttribute(ImageFilter.ATTR_GALLERY_CONFIG, galleryConfig);
				request.setAttribute(ImageFilter.ATTR_GALLERY_NAME, galleryName);
				request.setAttribute(ImageFilter.ATTR_SERVLET_CONTEXT, servletContext);
				
				//Gallery index page; basically the same as a gallery tag
				if (requestURI.matches("^" + contextPath + ImageFilter.GALLERIES_PATH + galleryName + "index\\.html$")){
					if (galleryConfig.isShowIndex()){
						new IndexService().doServe(request, response);
					}
					else {
						 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					}
					return;
				}

				//Gallery images zip - located under /galleries/<packageName>/all.zip
				else if (requestURI.matches("^" + contextPath + ImageFilter.GALLERIES_PATH + galleryName + "index\\.zip$")){
					if (galleryConfig.isShowZip()){
						new ZipFileService().doServe(request, response);
					}
					else {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					}
					return;
				}

				//RSS filter - located under /galleries/<packageName>/gallery.xml
				else if (requestURI.toLowerCase().matches("^" + contextPath + ImageFilter.GALLERIES_PATH + galleryName + "index\\.xml$")){
					if (galleryConfig.isShowRss()){
						new RssService().doServe(request, response);
					}
					else {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					}
					return;
				}
				
				//Single page gallery image with metadata; located under /galleries/<packageName>/<image_name>.html
				else if (requestURI.matches("^" + contextPath + ImageFilter.GALLERIES_PATH + galleryName + "[^/]+\\.html$")){
					if (galleryConfig.isShowSingleImage()){
						new SingleImageService().doServe(request, response);
					}
					else {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					}
					return;
				}		

				//Gallery images - converted and resized on demand.  Located under /galleries/<packageName>/<filename>
				else if (requestURI.matches("^" + contextPath + ImageFilter.GALLERIES_PATH + galleryName + "[^/]+\\.jpg$")){
					new ImageService().doServe(request, response);
					return;
				}
			}
			catch (UnauthorizedException e){
				throw new ServletException(e);
			}
		}

		chain.doFilter(request, response);
	}

	public void destroy() {
		config = null;
	}
	
	public static String getFullQualityUrlFromFile(ServletContext servletContext, String path){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");

		return (servletContext.getContextPath() 
				+ ImageFilter.GALLERIES_PATH 
				+ packageName
				+ "/"
				+ baseName 
				+ ImageFilter.IMAGE_SEPARATOR 
				+ ext
				+ ImageFilter.IMAGE_SEPARATOR
				+ "full"
				+ ".jpg").replaceAll(" ", "%20");
	}

	public static String getUrlStubFromFile(PageContext pageContext, String path){
		String packageName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");

		return (pageContext.getServletContext().getContextPath() 
				+ ImageFilter.GALLERIES_PATH 
				+ packageName
				+ "/"
				+ baseName 
				+ ImageFilter.IMAGE_SEPARATOR
				+ ext 
				+ ImageFilter.IMAGE_SEPARATOR
				+ "XXX_SIZE_XXX" 
				+ ImageFilter.IMAGE_SEPARATOR
				+ "YYY_QUALITY_YYY" 
				+ ".jpg").replaceAll(" ", "%20");

	}

	public static String getUrlFromFile(ServletContext servletContext, String path, int size, int quality, String extension){
		String galleryName = path.replaceAll("^/WEB-INF/galleries", "").replaceAll("/[^/]+$", "");
		String baseName = path.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z0-9]+", "");
		String ext = path.replaceAll("^.+\\.", "");

		return (servletContext.getContextPath() 
				+ ImageFilter.GALLERIES_PATH 
				+ galleryName
				+ "/"
				+ baseName 
				+ ImageFilter.IMAGE_SEPARATOR
				+ ext 
				+ ImageFilter.IMAGE_SEPARATOR
				+ size 
				+ ImageFilter.IMAGE_SEPARATOR
				+ quality 
				+ "."
				+ extension).replaceAll(" ", "%20");
	}

	/**
	 * Returns one of 'html' or 'flash' depending on the parameters, currently set cookies, and
	 * (if nothing else is set) user agent of the request.  
	 * @param request
	 * @return
	 */
	public static String getGalleryType(HttpServletRequest request, HttpServletResponse response){
		String type = null;

		if (type == null && "flash".equals(request.getParameter("type"))) {
			type = "flash";
		}
		if (type == null && "html".equals(request.getParameter("type"))) {
			type = "html";
		}

		Cookie[] cookies = request.getCookies();
		for (int i = 0; cookies != null && i < cookies.length; i++){
			if (type == null && "type".equals(cookies[i].getName())){
				if ("flash".equals(cookies[i].getValue())) type = "flash";
				if ("html".equals(cookies[i].getValue())) type = "html";
			}
		}

		String userAgent = request.getHeader("User-Agent");
		if (type == null && userAgent == null) type = "html";
		if (type == null && userAgent.toLowerCase().contains("iphone")) type = "html";

		if (type == null) type = "flash";

		Cookie cookie = new Cookie("type", type);
		cookie.setMaxAge(24 * 60 * 60);
		response.addCookie(cookie);			

		return type;
	}

	public static String escapeHtml(String s){
		if (s == null)
			return null;
		return s
		.replaceAll("'", "&apos;")
		.replaceAll("\"", "&quot;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("&", "&amp;")
		.replaceAll("[\r\n]", "<br/>");
	}

	public static String escapeXml(String s){
		if (s == null)
			return null;
		return s
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("[\r\n]", "\n");
	}

	public static GallerySettings getGalleryConfig(ServletContext servletContext, String galleryName) throws UnauthorizedException {
		final InputStream settings = servletContext.getResourceAsStream("/WEB-INF" + ImageFilter.GALLERIES_PATH + galleryName + "settings.xml");
		final GallerySettings galleryConfig = new GallerySettings();
		if (settings == null){
			//You must have a settings.xml file, or you cannot access the content.
			throw new UnauthorizedException("Settings file not found for gallery '" + galleryName + "'");
		}
		else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);

				galleryConfig.setIndexTemplate(getStringSettings(doc, "index", "template", null));
				galleryConfig.setIndexTitle(getStringSettings(doc, "index", "title", ""));
				galleryConfig.setIndexSize(getIntegerSettings(doc, "index", "size", 800));
				galleryConfig.setIndexQuality(getIntegerSettings(doc, "index", "quality", 85));
				galleryConfig.setIndexThumbSize(getIntegerSettings(doc, "index", "thumbSize", 130));
				galleryConfig.setIndexThumbQuality(getIntegerSettings(doc, "index", "thumbQuality", 70));
				galleryConfig.setIndexCenter(getBooleanSettings(doc, "index", "center", true));
				galleryConfig.setIndexOrder(getStringSettings(doc, "index", "order", "order"));
				galleryConfig.setIndexShowTitle(getBooleanSettings(doc, "index", "showTitle", true));
				galleryConfig.setIndexShowCaption(getBooleanSettings(doc, "index", "showCaption", true));
				galleryConfig.setIndexShowDate(getBooleanSettings(doc, "index", "showDate", true));
				galleryConfig.setIndexShowFilename(getBooleanSettings(doc, "index", "showFilename", false));
				galleryConfig.setIndexShowDownload(getBooleanSettings(doc, "index", "showDownload", false));
				galleryConfig.setIndexSlideshow(getBooleanSettings(doc, "index", "slideshow", false));
				galleryConfig.setIndexSlideshowDelay(getIntegerSettings(doc, "index", "slideshowDelay", 6000)); //Only used if indexSlideshow = true
				galleryConfig.setIndexSlideshowOverride(getBooleanSettings(doc, "index", "slideshowOverride", true)); //If the user clicks, stop slideshow

				galleryConfig.setSingleImageTemplate(getStringSettings(doc, "singleImage", "template", null));

				galleryConfig.setImageMaxSize(getIntegerSettings(doc, "image", "maxSize", 0));
				galleryConfig.setImageMaxQuality(getIntegerSettings(doc, "image", "maxQuality", 0));
				galleryConfig.setImageMinSize(getIntegerSettings(doc, "image", "minSize", 100));
				galleryConfig.setImageMinQuality(getIntegerSettings(doc, "image", "minQuality", 50));
				galleryConfig.setFullQualityAllowed(getBooleanSettings(doc, "image", "fullQualityAllowed", false));

				galleryConfig.setRssTitle(getStringSettings(doc, "rss", "title", null));
				galleryConfig.setRssDescription(getStringSettings(doc, "rss", "description", null));
				galleryConfig.setRssLink(getStringSettings(doc, "rss", "link", null));
				galleryConfig.setRssSize(getIntegerSettings(doc, "rss", "size", 500));
				galleryConfig.setRssQuality(getIntegerSettings(doc, "rss", "quality", 65));
				
				galleryConfig.setZipAllowed(getBooleanSettings(doc, "zip", "allowed", false));
			}
			catch (Exception e){
				throw new UnauthorizedException(e.getMessage());
			}
		}

		return galleryConfig;
	}

	public static ImageParams getImageParams(String imagePath, String requestURI, ServletContext servletContext) throws UnauthorizedException {
		//Set the thread priority to be lower, so that other requests get processed in a reasonable time.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);

		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + servletContext.getContextPath() + ImageFilter.GALLERIES_PATH, "");

		//Find path to gallery source
		String galleryName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");
		String requestUriFile = requestUriWithoutContextAndGalleriesPrefix.replaceAll("^/.*/", "").replaceAll("\\.[a-zA-Z]{3,4}$", "");

		String[] split = requestUriFile.split(ImageFilter.IMAGE_SEPARATOR);
		boolean fullQuality = false;
		if (split[split.length - 1].equals("full"))
			fullQuality = true;
		else if (split.length < 4){
			throw new UnauthorizedException("Invalid image request");
		}

		String qualityString, sizeString, extension, baseName;
		int size, quality;

		if (fullQuality){
			qualityString = null;
			sizeString = null;
			size = 0;
			quality = 100;
			extension = split[split.length - 2];
			baseName = requestUriFile.replaceAll(ImageFilter.IMAGE_SEPARATOR + extension + ImageFilter.IMAGE_SEPARATOR + "full", "");
		}
		else {
			qualityString = split[split.length - 1];
			String sizeAndTypeString = split[split.length - 2];
			sizeString = sizeAndTypeString.replaceAll("[^0-9]", "");
			size = Integer.parseInt(sizeString);
			quality = Integer.parseInt(qualityString);
			extension = split[split.length - 3];
			baseName = requestUriFile.replaceAll(ImageFilter.IMAGE_SEPARATOR + extension + ImageFilter.IMAGE_SEPARATOR + sizeAndTypeString + ImageFilter.IMAGE_SEPARATOR + qualityString, "");			
		}

		//Check the gallery for gallery-specific overrides on max / min size and quality.


		//Check for sane values on size / quality - only applicable for non-full quality images
		if (!fullQuality){
			if (size < 10)
				size = 10;
			if (size > 4000)
				size = 4000;
			if (quality > 100)
				quality = 100;
			if (quality < 0)
				quality = 0;
		}

		InputStream is = servletContext.getResourceAsStream(("/WEB-INF" + ImageFilter.GALLERIES_PATH + galleryName + baseName + "." + extension).replaceAll("%20", " "));

		String title = null, caption = null;
		Date captureDate = null;
		try {			
			Metadata metadata = JpegMetadataReader.readMetadata(is);
			Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
			Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);

			//OBJ_NAME is the field that Lightroom's 'Title' attribute is saved to.
			if (iptcDirectory.containsTag(IptcDirectory.TAG_OBJECT_NAME)){
				title = iptcDirectory.getString(IptcDirectory.TAG_OBJECT_NAME);
			}
			if (iptcDirectory.containsTag(IptcDirectory.TAG_CAPTION)){
				caption = iptcDirectory.getString(IptcDirectory.TAG_CAPTION);
			}
			if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME_ORIGINAL)){
				captureDate = exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
			}
		}
		catch (Throwable t) {} //This information is not required.		

		return new ImageParams(imagePath, galleryName, baseName, extension, fullQuality, size, quality, title, caption, captureDate);
	}

	private static String getStringSettings(Document doc, String element, String attribute, String defaultValue){
		if (doc == null)
			return defaultValue;
		NodeList children = doc.getElementsByTagName(element);
		if (children.getLength() == 0)
			return defaultValue;
		Node child = children.item(0);
		Node attrNode = child.getAttributes().getNamedItem(attribute);
		if (attrNode == null)
			return defaultValue;
		try {
			return attrNode.getTextContent();
		}
		catch (NumberFormatException nfe){
			return defaultValue;
		}
	}

	private static int getIntegerSettings(Document doc, String element, String attribute, int defaultValue){
		try {return Integer.parseInt(getStringSettings(doc, element, attribute, defaultValue + ""));}
		catch (Throwable e){return defaultValue;}
	}

	private static boolean getBooleanSettings(Document doc, String element, String attribute, boolean defaultValue){
		try {return Boolean.parseBoolean(getStringSettings(doc, element, attribute, defaultValue + ""));}
		catch (Throwable e){return defaultValue;}
	}

	private static final String MATCH_REGEX = "^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$";
	private static final String EXCLUDE_REGEX = ".*/\\.[^/]*"; //Hide all dot files

	public static final String ATTR_SERVLET_CONTEXT = "ca.digitalcave.servletContext";
	public static final String ATTR_GALLERY_CONFIG = "ca.digitalcave.galleryConfig";
	public static final String ATTR_GALLERY_NAME = "ca.digitalcave.galleryName";

	private static int galleryNumber = 0;
	public static String getGallery(HttpServletRequest request) throws UnauthorizedException {
		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);
		final GallerySettings galleryConfig = (GallerySettings) request.getAttribute(ImageFilter.ATTR_GALLERY_CONFIG);
		final String galleryName = (String) request.getAttribute(ImageFilter.ATTR_GALLERY_NAME);
		
		if (servletContext == null || galleryConfig == null || galleryName == null) {
			return "";
		}
		
		
		final StringBuilder sb = new StringBuilder();

		if (galleryConfig.isIndexCenter()){
			sb.append("<div style='margin-left: auto; margin-right: auto; width: ");
			sb.append(galleryConfig.getIndexSize());
			sb.append("px;'>\n");
		}

		final int count = galleryNumber++;

		sb.append("<div id='gallery");
		sb.append(count);
		sb.append("' style='width: ");
		sb.append(galleryConfig.getIndexSize());
		sb.append("px; height: ");
		sb.append(galleryConfig.getIndexSize() + 150); //Allow for height of bottom bar
		sb.append("px'>\n");

		@SuppressWarnings("unchecked")
		List<String> images = new ArrayList<String>(servletContext.getResourcePaths("/WEB-INF" + ImageFilter.GALLERIES_PATH + galleryName));

		if ("random".equals(galleryConfig.getIndexOrder().toLowerCase())) Collections.shuffle(images);
		else {
			Collections.sort(images);
			if ("reverse".equals(galleryConfig.getIndexOrder().toLowerCase())){
				Collections.reverse(images);
			}
		}

		final JSONArray data = new JSONArray();
		for (String imagePath : images) {
			try {
				final int thumbSize = galleryConfig.getIndexThumbSize();
				if (imagePath.toLowerCase().matches(MATCH_REGEX) && !imagePath.toLowerCase().matches(EXCLUDE_REGEX)){
					final JSONObject image = new JSONObject();
					try {
						final String imageURI = getUrlFromFile(servletContext, imagePath, galleryConfig.getIndexSize(), galleryConfig.getIndexQuality(), "jpg");
						final ImageParams imageParams = getImageParams(imagePath, imageURI, servletContext);
						image.put("image", imageURI);

						image.put("thumb", getUrlFromFile(servletContext, imagePath, (thumbSize == 0 ? galleryConfig.getIndexSize() : thumbSize), (thumbSize == 0 ? galleryConfig.getIndexQuality() : galleryConfig.getIndexThumbQuality()), "jpg"));
						if (galleryConfig.isIndexShowTitle() && imageParams.getTitle() != null){
							image.put("title", imageParams.getTitle());
						}

						StringBuilder info = new StringBuilder();
						if (galleryConfig.isIndexShowCaption() && imageParams.getCaption() != null){
							info.append("<p>").append(imageParams.getCaption()).append("</p>");
						}
						if (galleryConfig.isIndexShowDate() && imageParams.getCaptureDate() != null){
							info.append("<p>").append(new SimpleDateFormat("yyyy-MM-dd").format(imageParams.getCaptureDate())).append("</p>");
						}					
						if (galleryConfig.isIndexShowFilename()){
							final String filename = imagePath.replaceAll("^/.*/", "");
							info.append("<p>").append(escapeHtml(filename)).append("</p>");
						}
						if (info.length() > 0){
							image.put("description", info);
						}

						if (galleryConfig.isIndexShowDownload()){
							image.put("link", getFullQualityUrlFromFile(servletContext, imagePath));
						}
						data.put(image);
					}
					catch (JSONException e){
						;
					}
				}
			}
			catch (UnauthorizedException e){}
		}
		
		sb.append("</div> <!-- gallery -->\n");
		if (galleryConfig.isIndexCenter()){
			sb.append("</div> <!-- center -->\n");
		}

		sb.append("<script type='text/javascript'>\n");

		JSONObject configuration = new JSONObject();
		try {
			configuration.put("dataSource", data);
			configuration.put("debug", false);
			configuration.put("pauseOnInteraction", false);

			if (galleryConfig.isIndexSlideshow()){
				configuration.put("autoplay", galleryConfig.getIndexSlideshowDelay());
				if (!galleryConfig.isIndexSlideshowOverride()){
					configuration.put("thumbnails", false);
					configuration.put("showImagenav", false);
					configuration.put("swipe", false);
				}
			}
			else {
				configuration.put("autoplay", false);
			}

			//Thumbnail options
			configuration.put("thumbCrop", false);
			configuration.put("imageCrop", true);

			//Transition options
			configuration.put("touchTransistion", "slide");
			configuration.put("transition", "fadeslide");
			configuration.put("transitionSpeed", 500);

			//Common settings 
			configuration.put("showCounter", false);
			configuration.put("minScaleRatio", 1);
			configuration.put("maxScaleRatio", 1);
			configuration.put("width", galleryConfig.getIndexSize());
			configuration.put("height", galleryConfig.getIndexSize() + 150);
		}
		catch (JSONException e){
			;
		}
		
		sb.append("Galleria.run('#gallery");
		sb.append(count);
		sb.append("', ");
		sb.append(configuration.toString());
		sb.append(");\n");

		sb.append("</script>\n");

		if (galleryConfig.isIndexShowDownload() && galleryConfig.isZipAllowed()){
			sb.append("<p><a href='");
			sb.append(servletContext.getContextPath());
			sb.append(ImageFilter.GALLERIES_PATH);
			sb.append(galleryName);
			sb.append("index.zip'>Download All High Resolution Images</a></p>");
		}

		return sb.toString();
	}
	
	public static String getPhotoHeaders(HttpServletRequest request){
		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);
		final GallerySettings galleryConfig = (GallerySettings) request.getAttribute(ImageFilter.ATTR_GALLERY_CONFIG);
		final String galleryName = (String) request.getAttribute(ImageFilter.ATTR_GALLERY_NAME);
		
		if (servletContext == null) {
			return "";
		}

		final StringBuilder sb = new StringBuilder();
		
		if (galleryConfig != null && galleryConfig.isShowRss() && galleryName != null){
			sb.append("<link rel='alternate' type='application/rss+xml' title='RSS Feed for ");
			sb.append(galleryName.replace("^/", "").replace("/$", ""));
			sb.append(" gallery' href='");
			sb.append(servletContext.getContextPath());
			sb.append(ImageFilter.GALLERIES_PATH);
			sb.append(galleryName);
			sb.append("/index.xml' />\n");
		}
		
		sb.append("<script type='text/javascript' src='https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js'></script>\n");
		
		sb.append("<script type='text/javascript' src='");
		sb.append(servletContext.getContextPath());
		sb.append(ImageFilter.JAVASCRIPT_PATH);
		sb.append("/galleria/galleria-1.2.8.min.js'></script>\n");
		
		sb.append("<script type='text/javascript' src='");
		sb.append(servletContext.getContextPath());
		sb.append(ImageFilter.JAVASCRIPT_PATH);
		sb.append("/galleria/themes/classic/galleria.classic.min.js'></script>\n");

		return sb.toString();
	}
}
