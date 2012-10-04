package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.jsp.photos.ImageFilter;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.GallerySettings;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class RssService implements GalleryService {

	@SuppressWarnings("unchecked")
	public void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ServletContext servletContext = (ServletContext) request.getAttribute(ImageFilter.ATTR_SERVLET_CONTEXT);
		final GallerySettings galleryConfig = (GallerySettings) request.getAttribute(ImageFilter.ATTR_GALLERY_CONFIG);
		final String galleryName = (String) request.getAttribute(ImageFilter.ATTR_GALLERY_NAME);
		
		if (servletContext == null || galleryConfig == null || galleryName == null) {
			throw new IOException("Required values not in request attribtues");
		}

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle(galleryConfig.getRssTitle());
		feed.setDescription(galleryConfig.getRssDescription());
		feed.setLink(galleryConfig.getRssLink());
		feed.setEntries(new ArrayList<Object>());

		//Check the gallery for gallery-specific settings.  If there is no settings file, return 403
		if (!galleryConfig.isShowRss()){
			//You must have a settings.xml file, or you cannot access the content.
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		//Get a list of files from the context path
		List<String> images = new ArrayList<String>(request.getSession().getServletContext().getResourcePaths("/WEB-INF/galleries" + galleryName));
		List<ImageParams> imageParams = new ArrayList<ImageParams>();
		for (String imagePath : images) {
			if (imagePath.toLowerCase().matches("^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$")){
				String imageURI = ImageFilter.getUrlFromFile(servletContext, imagePath, galleryConfig.getRssSize(), galleryConfig.getRssQuality(), "jpg");
				try {
					ImageParams ip = ImageFilter.getImageParams(imagePath, imageURI, servletContext);
					imageParams.add(ip);
				}
				catch (UnauthorizedException e){}
			}
		}
		
		final String baseUrl = request.getRequestURL().toString().replaceAll("(http://[a-zA-Z0-9\\.]+[:0-9]*).*", "$1");
		Collections.sort(imageParams);
		Collections.reverse(imageParams);
		
		for (ImageParams ip : imageParams) {
			//Since RSS items are date-sensitive, we can only show items that have a date.
			if (ip.getCaptureDate() != null){
				SyndEntry entry = new SyndEntryImpl();
				entry.setTitle(ip.getTitle() != null ? ip.getTitle() : "Untitled Image");
				entry.setLink(baseUrl + ImageFilter.getUrlFromFile(servletContext, ip.getImagePath(), galleryConfig.getRssSize(), galleryConfig.getRssQuality(), "html"));
				entry.setPublishedDate(ip.getCaptureDate());
				SyndContent content = new SyndContentImpl();
				content.setType("text/html");
				content.setValue((ip.getCaption() != null ? ip.getCaption() : "Uncaptioned Image") 
						+ "<br/><img src='" + baseUrl + ImageFilter.getUrlFromFile(servletContext, ip.getImagePath(), galleryConfig.getRssSize(), galleryConfig.getRssQuality(), "jpg") + "'>");
				entry.setDescription(content);

				feed.getEntries().add(entry);
			}
		}
		
		SyndFeedOutput output = new SyndFeedOutput();
		try {
			output.output(feed, new OutputStreamWriter(response.getOutputStream()));
		}
		catch (FeedException e){
			throw new IOException(e);
		}
	}
}
