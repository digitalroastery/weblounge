<%@ taglib uri="/WEB-INF/greeter.tld" prefix="greeter" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML>
<html>
	<head>
		<title>Weblounge Test Site</title>
		<meta http-equiv="Content-type" content="text/html;charset=UTF-8" /> 
		<webl:generator/>
		<webl:workbench/>
		<webl:headers/>
	</head>
	<body>
		<form action="/weblounge-search">
			<input type="hidden" name="limit" value="15"/>
			<input type="hidden" name="target-template" value="search"/>
			<input type="text" name="query"/><input type="submit" name="Search"/>
		</form>
		<webl:composer id="main"/>
		<webl:composer id="bottom"/>
		
		<webl:property define="hello, world">

  <webl:ifproperty name="hello">
    <webl:property name="hello" />
  </webl:ifproperty>

  <webl:ifproperty name="world">
    <webl:property name="world" />
  </webl:ifproperty>

  <c:if test="${(not empty hello) or (not empty world)}">
    Never evaluated, because hello and empty are not set anymore
  </c:if>

</webl:property>

	</body>
</html>