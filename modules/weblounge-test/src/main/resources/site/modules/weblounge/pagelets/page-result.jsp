<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.content.page.Page" %>
<webl:context define="pagelet, site, language">
  <% Page pageResult = (Page)pagelet.getContent(); %>
  <% String path = pageResult.getURI().getPath(); %>
  <div class="page-result">
	  <h1><%= pageResult.getTitle(language) %></h1>
	  <p>
	  	<a href="<%= path %>"><%= path %></a>
	  </p>
  </div>
</webl:context>