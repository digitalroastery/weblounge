<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<webl:property define="hello, world">

  <webl:ifproperty name="hello">
    <webl:property name="hello" />
  </webl:ifproperty>

  <webl:ifproperty name="world">
    <webl:property name="world" />
  </webl:ifproperty>

  <c:if test="${(not empty hello) or (not empty world)}">
    Attributes have successfully been restored
  </c:if>

  <c:if test="${(empty hello) or (empty world)}">
    Attributes have been messed up!
  </c:if>

</webl:property>