<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/security" prefix="webls" %>
<!DOCTYPE HTML>
<html>
	<head>
		<title>Welcome to Weblounge</title>
		<meta http-equiv="Content-type" content="text/html;charset=UTF-8" /> 
		<webl:generator/>
		<webl:workbench/>
		<webl:headers/>
	</head>
    <webl:context>
	<body>
		<h1>Welcome to the Weblounge 3.0 testpage!</h1>
	    <webls:ifauthenticated>
			<h1>Hi <%= user.getName() %></h1>
		</webls:ifauthenticated>
		<webls:ifnotauthenticated>
			<h1>Hello, stranger</h1>
		</webls:ifnotauthenticated>
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
	</webl:context>
</html>