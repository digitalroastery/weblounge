<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:context define="language, user">
		<webl:property define="email">
				<webl:element define="title, description">
					<div class="email">
					<a href="mailto:<%= email %>" class="email">
						<% if (!title.equals("")) { %>
								<%= title %>
						<% } else { %>
								<%= email %>
						<% } %>
					</a>
					<% if (!description.equals("")) { %>
							<span class="text"><%= description %></span>
					<% } %>
					</div>
				</webl:element>
		</webl:property>
</webl:context>
