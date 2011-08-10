<%--
 * weblounge
 *
 * Enumeration
 * Ordered or unorderd list
 *
 * @dependency  -
 * @attributes  
 * @parameters  
 *
 * @version     weblounge 2.2
 * @author      Tobias Wunden
 * @link        http://www.o2it.ch
 * @copyright   Copyright 2008, o2it
 *
--%>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:element define="zwischentitel">
	<webl:ifelement name="zwischentitel">
		<h3><webl:element name="zwischentitel" /></h3>
	</webl:ifelement>
</webl:element>

<webl:property define="type">
	<%= "<" + type + ">" %>

	<%-- show all enumaration-items --%>
	<webl:element-iterator element="text">
		<li><webl:element name="text" templates="true" />&nbsp;</li>
	</webl:element-iterator>

	<%= "</" + type + ">" %>
</webl:property>