<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<html>
	<head>
		<title>Welcome to Weblounge</title>
		<webl:generator/>
		<script src="http://127.0.0.1:8080/weblounge-sites/weblounge-test/web/jquery-1.6.3.min.js" type="text/javascript"></script>
		<webl:headers/>
		<webl:workbench/>
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