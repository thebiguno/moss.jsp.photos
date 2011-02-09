package ca.digitalcave.moss.jsp.photos.services;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GalleryService {
	public void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
