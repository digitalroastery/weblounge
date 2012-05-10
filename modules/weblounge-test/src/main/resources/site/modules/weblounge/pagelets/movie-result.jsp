<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ page import="ch.entwine.weblounge.common.content.movie.MovieResource" %>
<webl:context define="pagelet, site, language">
  <% if (pagelet == null || pagelet.getContent() == null) return; %>
  <% MovieResource pageResult = (MovieResource)pagelet.getContent(); %>
  <% String path = pageResult.getURI().getPath(); %>
  <div class="movie-result">
	  <h1><%= pageResult.getTitle(language) %></h1>
	  <p>
	  	<a href="<%= path %>"><%= path %></a>
	  </p>
  </div>
</webl:context>