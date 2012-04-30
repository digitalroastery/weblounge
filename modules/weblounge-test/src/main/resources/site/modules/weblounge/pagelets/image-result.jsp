<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.content.image.ImageResource" %>
<webl:context define="imagelet, site, language">
  <% ImageResource imageResult = (ImageResource)imagelet.getContent(); %>
  <% String path = imageResult.getURI().getPath(); %>
  <div class="image-result">
	  <h1><%= imageResult.getTitle(language) %></h1>
	  <p>
	  	<a href="<%= path %>"><%= path %></a>
	  </p>
  </div>
</webl:context>