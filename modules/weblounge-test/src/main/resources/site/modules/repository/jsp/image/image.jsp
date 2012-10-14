<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/resources" prefix="weblr" %>
<%@ page import="ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ page import="ch.entwine.weblounge.common.url.UrlUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="site, language">

	<webl:element define="title, description">
	<webl:property define="resourceid, style, alignment, border, enlarge, description_preview, group, photographer, link">

	<weblr:image uuid="<%= resourceid %>" imagestyle="<%= style %>">
  	<%
	    // Resource description	
	    if (StringUtils.isBlank(description)) {
	    	description = imageDescription;
	    }
	    if (StringUtils.isBlank(title)) {
	    	title = imageTitle;
	    }
			
		// Create the link to the enlarged version
		String linkToEnlarged = UrlUtils.concat("/weblounge-images", image.getIdentifier(), language.getIdentifier());
		
		// Get the file size
		String size = ResourceUtils.formatFileSize(imagecontent.getSize());
		
		// The mimetype
		String mimetype = imagecontent.getMimetype();
	%>	
	
		<div class="image_<%= alignment %>"> 	
			<% if ("false".equals(border)) { %>
				<dl class="image <%= alignment %>" style="width: <%= imageWidth %>px;">
			<% } else { %>
				<dl class="image <%= alignment %>" style="width: <%= imageWidth + 2 %>px;">
			<% } %>
				<dt>
			  	<webl:ifproperty name="link"><a href="<%= link %>"></webl:ifproperty>
					<img src="<%= imageUrl %>" rel="#photo<%= image.getIdentifier() %>" alt="<%= description %>" title="<%= description %>" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" class="<%= "true".equals(border) ? "border" : "" %>" />
			  	<webl:ifproperty name="link"></a></webl:ifproperty>
				</dt>
		  		
	  		<% if (StringUtils.isNotBlank(description)) { %>
				<dd>						
					<% if ("false".equals(description_preview)) { %>
					<webl:ifproperty name="description">
						<p class="image_description"><%= description %></p>
					</webl:ifproperty>
					<% } %>
		
					<webl:ifproperty name="photographer">
						<span class="image_photographer">&copy;&nbsp;<webl:property name="photographer" /></span>
					</webl:ifproperty>
				</dd>
				<% } %>
			</dl>
		</div>
		<% if (enlarge.equals("true")) { %>
			<div class="apple_overlay" id="photo<%= image.getIdentifier() %>">
				<img src="<%= linkToEnlarged %>" />
				<div class="details">
					<h2><%= title %></h2>
					<p><%= description %></p>
				</div>
			</div>
			<script type="text/javascript">$(function(){new AppleOverlay('<%= image.getIdentifier() %>');});</script>
		<% } %>

	</weblr:image>	

	</webl:property>
	</webl:element>
</webl:context>