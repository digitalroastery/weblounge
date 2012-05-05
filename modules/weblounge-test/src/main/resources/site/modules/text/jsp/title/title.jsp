<%@ page import="java.util.Date" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:context define="p=page, language">
	<%
	Locale l = new Locale(language.getIdentifier());
	String modifier = null;
	DateFormat df = new SimpleDateFormat("dd. MMMM yyyy, HH:mm", l);
	Date publishingStartDate = null;
	if (p != null) {
		if (publishingStartDate == null) {
			publishingStartDate = ResourceUtils.getModificationDate(p);
		}
		publishingStartDate = p.getPublishFrom();
		modifier = p.getModifier().getName();
	} else {
	  publishingStartDate = new Date();
	  modifier = "";
	}
	%>
	
	<h1><webl:element name="title" /></h1>
	<p class="keyword"><webl:ifelement name="keyword"><webl:element name="keyword" /></webl:ifelement>&nbsp;</p>
	<webl:ifproperty name="date">
		<span class="DateAndAuthor">
			<%= df.format(publishingStartDate) %>&nbsp;
			<webl:ifproperty name="author">/</webl:ifproperty>
		</span>
	</webl:ifproperty>
	<webl:ifproperty name="author">
		<span class="DateAndAuthor">
			<%= modifier %>
		</span>
	</webl:ifproperty>
	<webl:ifelement name="lead">
		<p class="lead"><webl:element name="lead" templates="true" /></p>
	</webl:ifelement>
</webl:context>