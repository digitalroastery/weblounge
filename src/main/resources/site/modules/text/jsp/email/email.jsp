<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:property define="email">
		<webl:element define="title, description">
			<div class="email">
				<p>
					<a href="mailto:<%= email %>" class="email">
						<% if (!"".equals(title)) { %>
								<%= title %>
						<% } else { %>
								<%= email %>
						<% } %>
					</a>
					<% if (!"".equals(description)) { %>
							<span class="text"><%= description %></span>
					<% } %>
				</p>
			</div>
		</webl:element>
</webl:property>