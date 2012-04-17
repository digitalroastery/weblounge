<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<!DOCTYPE HTML>
<html>
	<head>
		<title>Welcome to Weblounge</title>
		<meta http-equiv="Content-type" content="text/html;charset=UTF-8" /> 
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
		<form action="/weblounge-search">
			<input type="hidden" name="limit" value="15"/>
			<input type="hidden" name="target-template" value="search"/>
			<input type="text" name="query"/><input type="submit" name="Search"/>
		</form>
		<webl:composer id="main"/>
		<webl:composer id="bottom"/>
	</body>
</html>