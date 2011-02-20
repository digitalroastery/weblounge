<%@ page import="java.util.Locale" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:context define="p=page, language">
	<%
	Locale l = new Locale(language.getIdentifier());
	DateFormat df = new SimpleDateFormat("dd. MMMM yyyy, HH:mm", l);
	%>
	
	<h1><webl:element name="title" /></h1>
	<p class="keyword"><webl:ifelement name="keyword"><webl:element name="keyword" /></webl:ifelement>&nbsp;</p>
	<webl:ifproperty name="date">
		<span class="DateAndAuthor">
			<%= df.format(p.getPublishFrom()) %>&nbsp;
			<webl:ifproperty name="author">/</webl:ifproperty>
		</span>
	</webl:ifproperty>
	<webl:ifproperty name="author">
		<span class="DateAndAuthor">
			<%= p.getModifier().getName() %>
		</span>
	</webl:ifproperty>
	<webl:ifelement name="lead">
		<p class="lead"><webl:element name="lead" templates="true" /></p>
	</webl:ifelement>
</webl:context>