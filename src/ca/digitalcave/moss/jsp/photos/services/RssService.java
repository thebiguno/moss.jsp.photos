package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import ca.digitalcave.moss.jsp.photos.ImageMetadata;

public class RssService {

	private final static String dateFormatString = "EEE, dd MMM yyyy HH:mm:ss z";
	
	public static void doServe(HttpServletRequest request, HttpServletResponse response, FilterConfig config) throws IOException {
		Logger logger = Logger.getLogger(RssService.class.getName());
		
		String requestURI = request.getRequestURI();
		
		//Remove the /galleries prefix
		String requestUriWithoutContextAndGalleriesPrefix = requestURI.replaceAll("^" + config.getServletContext().getContextPath() + Common.GALLERIES_PATH, "");

		//Find path to gallery source
		String packageName = requestUriWithoutContextAndGalleriesPrefix.replaceAll("[^/]+$", "");

		Integer rssSize = null, rssQuality = null;
		String rssLink = null, rssTitle = null;
		
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
				rssLink = Common.getStringSettings(doc, "rss", "link");
				rssTitle = Common.getStringSettings(doc, "rss", "title");

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
		@SuppressWarnings("unchecked")
		List<String> images = new ArrayList<String>(request.getSession().getServletContext().getResourcePaths("/WEB-INF/galleries" + packageName));

		Date lastBuildDate = new Date(0);
		String language = "en-us";
		StringBuilder sb = new StringBuilder();
		
		final String baseUrl = request.getRequestURL().toString().replaceAll("(http://[a-zA-Z0-9]+[:0-9]*).*", "$1");
		
		for (String image : images) {
			if (image.toLowerCase().matches("^.*png$|^.*jpg$|^.*jpeg$|^.*bmp$|^.*png$|^.*gif$")){
				InputStream is = config.getServletContext().getResourceAsStream(image);
				ImageMetadata im = Common.getImageMetadata(is);
				
				//Since RSS items are date-sensitive, we can only show items that have a date.
				if (im.getCaptureDate() != null){
					if (im.getCaptureDate().after(lastBuildDate)) lastBuildDate = im.getCaptureDate();
					
					sb.append("<item>");
					sb.append("<title>");
					if (im.getTitle() != null) sb.append(Common.escapeXml(im.getTitle()));
					else sb.append("Untitled Image");
					sb.append("</title>");
					
					sb.append("<link>").append(baseUrl).append(Common.getUrlFromFile(config.getServletContext(), image, rssSize, rssQuality, "jsp")).append("</link>");
					sb.append("<guid>").append(baseUrl).append(Common.getUrlFromFile(config.getServletContext(), image, rssSize, rssQuality, "jsp")).append("</guid>");
					sb.append("<pubDate>").append(new SimpleDateFormat(dateFormatString).format(im.getCaptureDate())).append("</pubDate>");
					sb.append("<description>");
					if (im.getCaption() != null) sb.append(Common.escapeXml(im.getCaption()));
					else sb.append("Uncaptioned Image");
					sb.append("</description>");
					sb.append("</item>\n");
				}
			}
		}
		
		
		//Write the RSS headers out
		response.getOutputStream().println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getOutputStream().println("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
		response.getOutputStream().println("<channel>");
		response.getOutputStream().println("<atom:link href=\"" + request.getRequestURL() + "\" rel=\"self\" type=\"application/rss+xml\" />");
		
		response.getOutputStream().println("<title>" + (rssTitle != null ? rssTitle : "Untitled RSS Feed") + "</title>");
		response.getOutputStream().println("<link>" + rssLink + "</link>");
		response.getOutputStream().println("<description>" + packageName + "</description>");
		response.getOutputStream().println("<lastBuildDate>" + new SimpleDateFormat(dateFormatString).format(lastBuildDate) + "</lastBuildDate>");
		response.getOutputStream().println("<language>" + language + "</language>");

		
		//Write the RSS content out
		response.getOutputStream().println(sb.toString());
		
		//Write RSS footers
		response.getOutputStream().println("</channel>");
		response.getOutputStream().println("</rss>");
	}
}
