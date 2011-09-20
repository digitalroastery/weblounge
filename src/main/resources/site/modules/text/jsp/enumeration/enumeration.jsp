<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:element define="zwischentitel">
	<webl:ifelement name="zwischentitel">
		<h3><webl:element name="zwischentitel" /></h3>
	</webl:ifelement>
</webl:element>

<webl:property define="type">
	<%= "<" + type + ">" %>

	<%-- show all enumeration-items --%>
	<webl:element-iterator elements="text[\d*]">
		<li><webl:element name="text<%= index %>"/>&nbsp;</li>
	</webl:element-iterator>

	<%= "</" + type + ">" %>
</webl:property>