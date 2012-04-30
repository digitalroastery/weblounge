<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.content.file.FileResource" %>
<webl:context define="filelet, site, language">
  <% FileResource fileResult = (FileResource)filelet.getContent(); %>
  <% String path = fileResult.getURI().getPath(); %>
  <div class="file-result">
	  <h1><%= fileResult.getTitle(language) %></h1>
	  <p>
	  	<a href="<%= path %>"><%= path %></a>
	  </p>
  </div>
</webl:context>