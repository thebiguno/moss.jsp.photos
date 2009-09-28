<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="p" uri="/WEB-INF/tlds/photo-tags.tld" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<p:header/>
</head>
<body>
<h1>Foo</h1>


<p>
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam mollis rutrum turpis, non sagittis magna vestibulum quis. Cras lacinia lacus in nulla molestie posuere. Nam et neque massa, at tincidunt tellus. Donec a volutpat leo. Pellentesque ac metus eu diam tincidunt ultricies quis in elit. Fusce ornare nunc sed massa facilisis quis venenatis diam pharetra. Vivamus tincidunt varius quam et pretium. Donec ornare, turpis quis dictum elementum, risus orci tempus arcu, suscipit euismod elit metus convallis lectus. Integer ut odio pretium mi ornare vestibulum eget id velit. In vitae augue in ante vestibulum accumsan aliquet non lacus. Proin tincidunt posuere metus, a sollicitudin purus ornare eu. Nam eget tempor sapien. Pellentesque eget sapien nunc. Aenean in arcu tortor, at sollicitudin turpis. Sed ullamcorper, libero quis pellentesque laoreet, lorem quam consectetur nisi, id aliquam magna massa non neque. In eu est elit. 
</p>
<p>
<p:gallery packageName="/img" thumbSize="350" fullSize="800" showFullQualityDownload="true"/>
</p>
<p>
<p:slideshow packageName="/img"/>
</p>

</body>
</html>