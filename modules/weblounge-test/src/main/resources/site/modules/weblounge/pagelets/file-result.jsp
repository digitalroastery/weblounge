<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.common.content.file.FileResource" %>
<webl:context define="pagelet, site, language">
  <% FileResource fileResult = (FileResource)pagelet.getContent(); %>
  <% String path = fileResult.getURI().getPath(); %>
  <div class="file-result">
	  <h1><%= fileResult.getTitle(language) %></h1>
	  <p>
	  	<a href="<%= path %>"><%= path %></a>
	  </p>
  </div>
</webl:context>