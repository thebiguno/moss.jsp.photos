package ca.digitalcave.moss.jsp.photos.model;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.FilterConfig;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConfigFactory {
	
	private final static Logger logger = Logger.getLogger(ConfigFactory.class.getName());

	public static Config loadConfig(FilterConfig filterConfig) {
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(Config.class);
		
		String config = filterConfig.getInitParameter("config");
		if (config == null || config.length() == 0)
			config = "photos.xml";
		InputStream is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/" + config);
		if (is == null){
			logger.config("No config file '/WEB-INF/" + config + "' found!  Config filter will not be configured.");
			return null;
		}
		
		Object o = xstream.fromXML(is);
		if (o instanceof Config)
			return (Config) o;
		
		logger.config("Could not load config file; resulting object of type " + o.getClass().getName());
		
		return null;
	}
}
