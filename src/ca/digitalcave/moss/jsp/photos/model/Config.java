package ca.digitalcave.moss.jsp.photos.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("photos")
public class Config {

	@XStreamAsAttribute
	@XStreamAlias("log-level")
	private String logLevel;

	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
}
