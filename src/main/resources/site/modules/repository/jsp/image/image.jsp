<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>
<%@ page import="ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ page import="ch.entwine.weblounge.common.url.UrlUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="site, language">

	<webl:element define="description">
	<webl:property define="resourceid, style, alignment, border, enlarge, description_preview, group, photographer, link">

	<weblr:image uuid="<%= resourceid %>" imagestyle="<%= style %>">
	  <%
	    // Resource description	
	    if (StringUtils.isBlank(description)) {
	    	description = image.getDescription(language);
	    }
			
			// Create the link to the enlarged version
			String linkToEnlarged = UrlUtils.concat("/weblounge-images", image.getIdentifier(), imagecontent.getFilename());
            linkToEnlarged += "?style=highresolution";
			
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
	
					<% if ("true".equals(enlarge)) { %>
						<img src="<%= imageUrl %>" alt="<%= description %>" title="<%= description %>" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" class="<%= "true".equals(border) ? "border" : "" %>" />
						<% //TODO: Disable enlage if user is authenticated tro prevent Firefox-Bug %>
						<br/>
					 	<a href="<%= linkToEnlarged %>" class="imgEnlarge"  title="<%= description %>">
					 		<img src="<%= "/weblounge-sites/rivellagames/modules/repository/jsp/image/lupe.png" %>" class="<%= "true".equals(border) ? "enlarge_border" : "enlarge" %>" alt="[+]" title="<%= site.getI18n().getAsHTML("module.repository.image.enlarge", language) %>" />
					 	</a>
					<% } else { %>
						<img src="<%= imageUrl %>" alt="<%= description %>" title="<%= description %>" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" class="<%= "true".equals(border) ? "border" : "" %>" />
					<% } %>
				 
			  	<webl:ifproperty name="link"></a></webl:ifproperty>
				</dt>
		  		
	  		<% if (StringUtils.isNotBlank(description)) { %>
				<dd>						
					<% if ("false".equals(description_preview)) { %>
						<webl:ifelement name="description">
							<p class="image_description"><webl:element name="description" /></p>
						</webl:ifelement>
					<% } else { %>
	
					<% } %>
		
					<webl:ifproperty name="photographer">
						<span class="image_photographer">&copy;&nbsp;<webl:property name="photographer" /></span>
					</webl:ifproperty>
				</dd>
				<% } %>
			</dl>
		</div>

	</weblr:image>	

	</webl:property>
	</webl:element>
</webl:context>