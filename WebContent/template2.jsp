<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="p" uri="/WEB-INF/tlds/photo-tags.tld" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

</head>
<body style="width: 100%; color: #fff; background-color: red;">

<h1><%= request.getAttribute("title") %></h1>
<p><%= request.getAttribute("caption") %></p>

<p><img src="<%= request.getAttribute("imageSource") %>" alt=""/></p>

<p><%= request.getAttribute("date") %></p>
</body>
</html>