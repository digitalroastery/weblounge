<%@page import="ch.entwine.weblounge.common.content.file.FileResource"%>
<%@page import="ch.entwine.weblounge.common.impl.content.ResourceURIImpl"%>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ page import="ch.entwine.weblounge.common.url.UrlUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="site, language">

	<webl:element define="title, description">
	<webl:property define="resourceid">
	
	<%	
		new ResourceURIImpl(FileResource.TYPE, site, null, resourceid);
	%>
	
	<weblr:resource uuid="<%= resourceid %>">
	<%	
	    /** Resource description */	
	    if (StringUtils.isNotBlank(description)) {
	    	description = resource.getDescription(language);
	    }
	
		/** Create the download link */
		String link = UrlUtils.concat("/weblounge-files", resource.getIdentifier(), resourcecontent.getFilename());
		
		/** Get the resource size */
		String size = ResourceUtils.formatFileSize(resourcecontent.getSize());
		
		/** The mimetype */
		String mimetype = resourcecontent.getMimetype();
	%>
					
		<table class="download">
			<tr>
				<td class="title"><a href="<%= link %>"><%= title %></a></td>
				<td class="link">&nbsp;<a href="<%= link %>"></a>&nbsp;(<%= size %>&nbsp;/&nbsp;<%= mimetype %>)</td>
			</tr>
			
			<webl:ifelement name="description">
			<tr>
				<td colspan="2"><%= description %></td>
			</tr>
			</webl:ifelement>
		</table>
	</weblr:resource>

	</webl:property>
	</webl:element>

</webl:context>