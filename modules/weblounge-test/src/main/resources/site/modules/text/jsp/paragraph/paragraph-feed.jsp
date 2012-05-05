<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:ifelement name="title">
	<h3>
		<webl:element name="title" />
	</h3>
</webl:ifelement>
<webl:ifproperty name="quote">
	<blockquote>
		<webl:element name="paragraph" templates="true" />
	</blockquote>
</webl:ifproperty>
<webl:ifnotproperty name="quote">
	<p>
		<webl:element name="paragraph" templates="true" />
	</p>
</webl:ifnotproperty>