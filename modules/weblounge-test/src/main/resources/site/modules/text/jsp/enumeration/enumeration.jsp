<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<webl:ifelement name="zwischentitel">
  <h3><webl:element name="zwischentitel" /></h3>
</webl:ifelement>

<%-- open the correct enumeration tag --%>
<webl:property define="type">
  <c:if test="${type eq 'ol'}"><ol></c:if>
  <c:if test="${type eq 'ul'}"><ul></c:if>
</webl:property>

<%-- show all enumeration-items --%>
<webl:content-iterator elements="text">
  <li><%= text %></li>
</webl:content-iterator>

<%-- close the correct enumeration tag --%>
<webl:property define="type">
  <c:if test="${type eq 'ol'}"></ol></c:if>
  <c:if test="${type eq 'ul'}"></ul></c:if>
</webl:property>