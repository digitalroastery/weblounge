<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<h2><webl:i18n key="module.text.email" /></h2>
<p>
	<input name="element:title" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.email.sample" /></span>
</p>
<h2><webl:i18n key="module.text.email.description" /></h2>
<p>
	<textarea name="element:description"></textarea><br />
	<span class="editor-sample"><webl:i18n key="module.text.email.description.sample" /></span>
</p>
<h2><webl:i18n key="module.text.email.link" /><span class="required">*</span></h2>
<p>
	<input name="property:email" class="required email" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.email.link.sample" /></span>
</p>