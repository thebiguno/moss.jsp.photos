<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="p" uri="/WEB-INF/tlds/photo-tags.tld" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

<p:header rssPackages="/img"/>
</head>
<body style="width: 100%; background-color: #000;">

<p:gallery packageName="/img" fullSize="800" showTitle="true" showCaption="true" showDate="true" showRssLink="true"/>

</body>
</html>