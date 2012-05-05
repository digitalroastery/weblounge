<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:property define="anchor, box, gradient, title, quote, paragraph">

<webl:ifproperty name="box">
	<div class="box <%=(gradient.equals("true")?"gradient":"") %>">
</webl:ifproperty>

	<webl:ifelement name="title">
		<h3><webl:element name="title" /></h3>
	</webl:ifelement>
		
		<div class="inner"<webl:ifproperty name="anchor"> id="<%= anchor %>"</webl:ifproperty>>
		
		<webl:ifproperty name="quote">
			<blockquote>
		</webl:ifproperty>	
	
				<p><webl:element name="paragraph" templates="true" /></p>
	
		<webl:ifproperty name="quote">
			</blockquote>
		</webl:ifproperty>
		
		</div>

<webl:ifproperty name="box">
	</div>
</webl:ifproperty>

</webl:property>