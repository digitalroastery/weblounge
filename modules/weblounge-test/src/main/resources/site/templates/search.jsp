<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.common.content.SearchResult" %>
<%@ page import="ch.entwine.weblounge.common.request.WebloungeRequest" %>
<% SearchResult result = (SearchResult)request.getAttribute(WebloungeRequest.SEARCH); %>
<!DOCTYPE HTML>
<html>
	<head>
		<title>Weblounge Test Site</title>
		<meta http-equiv="Content-type" content="text/html;charset=UTF-8" /> 
		<webl:generator/>
		<webl:workbench/>
		<webl:headers/>
	</head>
	<body>
		<form action="/weblounge-search">
			<input type="hidden" name="limit" value="15"/>
			<input type="hidden" name="target-template" value="search"/>
			<% if (result != null) { %>
			<input type="text" name="query" value="<%= result.getQuery().getQueryString() %>"/><input type="submit" name="Search"/>
			<% } else { %>
			<input type="text" name="query"/><input type="submit" name="Search"/>
			<% } %>
		</form>
		
		<!-- Search summary -->
		<% if (result != null) { %>
		<p>
			About <%= result.getHitCount() %> results (<%= result.getSearchTime() %> miliseconds)
		</p>
		<% } %>
		
		<!-- Search results -->
		<webl:composer id="main"/>
		
		<!-- Pager -->
		<% if (result != null && result.getOffset() + result.getPageSize() > result.getDocumentCount()) { %>
		<p>
			Pager (TODO)			
		</p>
		<% } %>

	</body>
</html>