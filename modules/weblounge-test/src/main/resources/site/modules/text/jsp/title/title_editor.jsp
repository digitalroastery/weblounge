<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<h2><webl:i18n key="module.text.title.keyword"/></h2>
<p>
	<input name="element:keyword" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.title.keyword.sample"/></span>
</p>
<h2><webl:i18n key="module.text.title.title"/><span class="required">*</span></h2>
<p>
	<input name="element:title" class="required" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.title.title.sample"/></span>
</p>
<h2><webl:i18n key="module.text.title.lead"/></h2>
<p>
	<textarea name="element:lead"></textarea><br />
	<span class="editor-sample"><webl:i18n key="module.text.title.lead.sample"/></span>
</p>
<p>
	<input name="property:date" type="checkbox" /><webl:i18n key="module.text.title.showdate"/><br />
	<input name="property:author" type="checkbox" /><webl:i18n key="module.text.title.showauthor"/>
</p>