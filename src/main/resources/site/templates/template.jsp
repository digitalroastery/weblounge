<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<html>
	<head>
		<title>Test Template</title>
		<webl:generator/>
		<webl:headers/>
	</head>
	<body>
		<h1><%= "Welcome to the Weblounge 3.0 testpage!" %></h1>
		<greeter:greeting/>
		<webl:composer name="main"/>
	</body>
</html>