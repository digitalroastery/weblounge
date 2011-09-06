<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<input name="property:year" type="hidden" />
<input name="property:login" type="hidden" />
<h2><webl:i18n key="module.text.author.source"/></h2>
<p>
	<input name="property:source" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.author.source.sample"/></span>
</p>		
<h2><webl:i18n key="module.text.author.author"/></h2>
<p>
	<input name="property:name" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.author.author.sample"/></span>
</p>
<h2><webl:i18n key="module.text.author.email"/></h2>
<p>
	<input name="property:email" class="email" type="text"><br />
	<span class="editor-sample"><webl:i18n key="module.text.author.email.sample"/></span>
</p>
<h2><webl:i18n key="module.text.author.position"/></h2>
<p>
	<input name="element:position" type="text"/><br />
	<span class="editor-sample"><webl:i18n key="module.text.author.position.sample"/></span>
</p>