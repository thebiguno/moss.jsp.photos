<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/WEB-INF/tlds/GalleryTags.tld" prefix="gallery" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<gallery:header/>
</head>
<body>
<h1>Foo</h1>

<gallery:gallery packageName="/img" thumbSize="256" fullSize="800"/>

</body>
</html>