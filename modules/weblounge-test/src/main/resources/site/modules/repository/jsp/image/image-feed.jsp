<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/resources" prefix="weblr" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="site, language">

	<webl:element define="description">
	<webl:property define="resourceid, style">

	<weblr:image uuid="<%= resourceid %>" imagestyle="<%= style %>">
	  <%
	    // Get the resource description	if the pagelet does not define its own
	    if (StringUtils.isBlank(description)) {
	    	description = image.getDescription(language);
	    }
		%>	
    <p>
			<img src="<%= imageUrl %>" alt="<%= description %>" title="<%= description %>" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" />
		</p>
	</weblr:image>	

	</webl:property>
	</webl:element>
</webl:context>