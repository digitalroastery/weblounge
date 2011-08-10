<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:ifproperty name="anchor">
	<a name="<%= anchor %>"></a>
</webl:ifproperty>

<h2><webl:element name="subtitle" /></h2>