<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>
<%@ page import="ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ page import="ch.entwine.weblounge.common.url.UrlUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="site, language">

	<webl:element define="description">
	<webl:property define="resourceid, style, alignment, border, enlarge, description_preview, group, photographer">

	<weblr:image uuid="<%= resourceid %>" imagestyle="<%= style %>">
	  <%
	    // Resource description	
	    if (StringUtils.isBlank(description)) {
	    	description = image.getDescription(language);
	    }
			
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
					<img src="<%= imageUrl %>" alt="<%= description %>" title="<%= description %>" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" class="<%= "true".equals(border) ? "border" : "" %>" /><br/>
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