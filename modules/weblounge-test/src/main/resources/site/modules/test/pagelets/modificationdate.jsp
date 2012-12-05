<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ page import="java.util.Date" %>
<% 
	Date modificationDate = new Date(); 
%>
<div>
	Modificationdate <%= modificationDate %>
	<webl:modified date="<%= modificationDate %>"/>
</div>