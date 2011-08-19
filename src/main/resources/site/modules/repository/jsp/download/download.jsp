<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ page import="ch.entwine.weblounge.common.url.UrlUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="site, language">

	<webl:element define="title, description">
	<webl:property define="resourceid">
	
	<weblr:file uuid="<%= resourceid %>">
		<%	
	    /** Resource description */	
	    if (StringUtils.isNotBlank(description)) {
	    	description = file.getDescription(language);
	    }
		
			/** Create the download link */
			String link = UrlUtils.concat("/weblounge-files", file.getIdentifier(), filecontent.getFilename());
			
			/** Get the file size */
			String size = ResourceUtils.formatFileSize(filecontent.getSize());
			
			/** The mimetype */
			String mimetype = filecontent.getMimetype();
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
	</weblr:file>	

	</webl:property>
	</webl:element>

</webl:context>