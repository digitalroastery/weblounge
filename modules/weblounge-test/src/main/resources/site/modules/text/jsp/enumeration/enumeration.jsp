<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:element define="zwischentitel">
	<webl:ifelement name="zwischentitel">
		<h3><webl:element name="zwischentitel" /></h3>
	</webl:ifelement>
</webl:element>

<%-- open the correct enumeration tag --%>
<<webl:property name="type"/>>

<%-- show all enumeration-items --%>
<webl:element-iterator elements="text[\d*]">
	<li><%= elementValue %></li>
</webl:element-iterator>

<%-- close the correct enumeration tag --%>
<<webl:property name="type"/>>