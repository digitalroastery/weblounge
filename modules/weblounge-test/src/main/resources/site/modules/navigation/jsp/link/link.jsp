<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/resources" prefix="weblr" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<webl:context define="language, user">
<webl:property define="type">

<%-- internal --%>
<% if (type.equals("internal")) { %>
	<webl:property define="resourceid, anchor">
		<webl:element define="title, description">
			<weblr:page uuid="<%= resourceid %>">
				<%
				  String link = pagecontent.getURI().getPath();
				%>
      			<% anchor = StringUtils.isNotBlank(anchor) ? "#" + anchor : ""; %>
      			<% title = StringUtils.isNotBlank(title) ? title : pagecontent.getTitle(language); %>
      			<p class="link">
      			<a href="<%= link %><%= anchor %>"><%= title %></a>
        		<% if (StringUtils.isNotBlank(description)) { %>
        			<br /><span class="text"><%= description %></span>
        		<% } %>
				</p>
			</weblr:page>
		</webl:element>
	</webl:property>		

<%-- external --%>
<% } else { %>
	<webl:property define="link, anchor, window">
		<webl:element define="title, description">
			<p class="link"><a href="<%= link %><% if (anchor.length() >0) out.print("#" + anchor); %>" 
				<% if (window.equals("Ja")) { %>
					<% out.print("target=\"_blank\""); %>
				<% } else { %>
					<% out.print("target=\"_self\""); %>
				<% } %>>
				<% if (!title.equals("")) { %>
						<%= title %>
				<% } else { %>
						<%= link %>
				<% } %>
			</a>
			<% if (!description.equals("")) { %>
					<br /><span class="text"><%= description %></span>
			<% } %>
			</p>
		</webl:element>
	</webl:property>
<% } %>

</webl:property>		
</webl:context>