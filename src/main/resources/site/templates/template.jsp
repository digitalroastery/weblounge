<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<html>
	<head>
		<title>Test Template</title>
		<webl:generator/>
		<webl:headers/>
	</head>
	<body>
		<h1><%= "Hello world!" %></h1>
		<greeter:greeting/>
	</body>
</html>