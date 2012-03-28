<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<p class="author">
	<webl:ifproperty name="source">
		<webl:i18n key="module.text.author.source"/>: <webl:property name="source"/><br />
	</webl:ifproperty>
	
	<webl:ifproperty name="name">
		<webl:ifproperty name="email">
			<a href="mailto:<%= email %>">
		</webl:ifproperty>
		
		<webl:property name="name" />
		
		<webl:ifproperty name="email">
			</a>
		</webl:ifproperty>
		<br/>

		<webl:ifelement name="position">
			<webl:element name="position" /><br />
		</webl:ifelement>
	</webl:ifproperty>
	
	<webl:ifproperty name="copyrightholder">
    &copy;&nbsp;<webl:property name="year" />&nbsp;<webl:property name="copyrightholder" />
  </webl:ifproperty>
</p>