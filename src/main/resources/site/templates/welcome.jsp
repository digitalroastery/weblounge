<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<html>
	<head>
		<title>Welcome to Weblounge</title>
		<webl:generator/>
		<webl:workbench/>
		<webl:headers/>
	</head>
	<body>
		<h1><%= "Welcome to the Weblounge 3.0 testpage!" %></h1>
		<div>
			i18n: <webl:i18n key="greeting.hello"/> <webl:i18n key="greeting.world"/>
		</div>
		<greeter:greeting/>
		<webl:composer name="main"/>
	</body>
</html>