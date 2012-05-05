<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.common.content.image.ImageResource" %>
<webl:context define="pagelet, site, language">
  <% ImageResource imageResult = (ImageResource)pagelet.getContent(); %>
  <% String path = imageResult.getURI().getPath(); %>
  <div class="image-result">
	  <h1><%= imageResult.getTitle(language) %></h1>
	  <p>
	  	<a href="<%= path %>"><%= path %></a>
	  </p>
  </div>
</webl:context>