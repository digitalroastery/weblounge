<%@ page import="java.util.Locale" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:context define="p=page, language">
	<%
	Locale l = new Locale(language.getIdentifier());
	DateFormat df = new SimpleDateFormat("dd. MMMM yyyy, HH:mm", l);
	%>
	<h1><webl:element name="title" /></h1>
	<p>
		<webl:ifelement name="keyword"><webl:element name="keyword" /></webl:ifelement>
	</p>
	<webl:ifelement name="lead">
		<p>
			<webl:element name="lead" templates="true" />
		</p>
	</webl:ifelement>
</webl:context>