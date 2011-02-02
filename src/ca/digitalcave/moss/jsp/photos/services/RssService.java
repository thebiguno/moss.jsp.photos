package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ca.digitalcave.moss.jsp.photos.Common;
import ca.digitalcave.moss.jsp.photos.exception.UnauthorizedException;
import ca.digitalcave.moss.jsp.photos.model.ImageParams;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class RssService {

	@SuppressWarnings("unchecked")
	public static void doServe(HttpServletRequest request, HttpServletResponse response, FilterConfig config) throws IOException {
		Logger logger = Logger.getLogger(RssService.class.getName());
		
		String requestURI = request.getRequestURI();
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + Common.GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setEntries(new ArrayList<Object>());

		Integer rssSize = null, rssQuality = null;
		
		//Check the gallery for gallery-specific settings.  If there is no settings file, return 403
		InputStream settings = config.getServletContext().getResourceAsStream("/WEB-INF" + Common.GALLERIES_PATH + packageName + "/settings.xml");
		if (settings == null){
			//You must have a settings.xml file, or you cannot access the content.
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(settings);

				Integer sizeMax = Common.getIntegerSettings(doc, "size", "max");
				Integer sizeMin = Common.getIntegerSettings(doc, "size", "min");
				Integer qualityMax = Common.getIntegerSettings(doc, "quality", "max");
				Integer qualityMin = Common.getIntegerSettings(doc, "quality", "min");
				rssSize = Common.getIntegerSettings(doc, "rss", "size");
				rssQuality = Common.getIntegerSettings(doc, "rss", "quality");

				
				final String rssLink = Common.getStringSettings(doc, "rss", "link");
				final String rssTitle = Common.getStringSettings(doc, "rss", "title");
				final String rssDescription = Common.getStringSettings(doc, "rss", "description");
				
				feed.setTitle(rssTitle != null ? rssTitle : "Untitled RSS Feed");
				feed.setLink(rssLink != null ? rssLink : "http://example.com");
				feed.setDescription(rssDescription != null ? rssDescription : "");

				if (rssSize == null || rssQuality == null){
					throw new Exception("No RSS size / quality settings found");
				}
				
				if (sizeMax != null && rssSize > sizeMax)
					rssSize = sizeMax;
				if (sizeMin != null && rssSize < sizeMin)
					rssSize = sizeMin;
				if (qualityMax != null && rssQuality > qualityMax)
					rssQuality = qualityMax;
				if (qualityMin != null && rssQuality < qualityMin)
					rssQuality = qualityMin;
			}
			catch (Exception e){
				logger.log(Level.WARNING, "Error while opening settings.xml for gallery package " + packageName, e);
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		
		//Get a list of files from the context path
		List<String> images = new ArrayList<String>(request.getSession().getServletContext().getResourcePaths("/WEB-INF/galleries" + packageName));

		final String baseUrl = request.getRequestURL().toString().replaceAll("(http://[a-zA-Z0-9\\.]+[:0-9]*).*", "$1");
				
		for (String imagePath : images) {
			try {
				if (imagePath.toLowerCase().matches("^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$")){
					String imageURI = Common.getUrlFromFile(config.getServletContext(), imagePath, rssSize, rssQuality, "jpg");
					ImageParams imageParams = Common.getImageParams(imageURI, config.getServletContext());

					//Since RSS items are date-sensitive, we can only show items that have a date.
					if (imageParams.getCaptureDate() != null){
						SyndEntry entry = new SyndEntryImpl();
						entry.setTitle(imageParams.getTitle() != null ? imageParams.getTitle() : "Untitled Image");
						entry.setLink(baseUrl + Common.getUrlFromFile(config.getServletContext(), imagePath, rssSize, rssQuality, "jsp"));
						entry.setPublishedDate(imageParams.getCaptureDate());
						SyndContent content = new SyndContentImpl();
						content.setType("text/html");
						content.setValue((imageParams.getCaption() != null ? imageParams.getCaption() : "Uncaptioned Image") 
								+ "<br/><img src='" + baseUrl + Common.getUrlFromFile(config.getServletContext(), imagePath, rssSize, rssQuality, "jpg") + "'>");
						entry.setDescription(content);

						feed.getEntries().add(entry);
					}
				}
			}
			catch (UnauthorizedException e){}
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
