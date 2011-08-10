<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<h2><webl:i18n key="module.text.paragraph.title"/></h2>
<p>
	<input name="element:title" type="text" /><br />
	<span class="editor-sample"><webl:i18n key="module.text.paragraph.title.sample"/></span>
</p>
<h2><webl:i18n key="module.text.paragraph.paragraph"/><span class="required">*</span></h2>
<p>
	<textarea name="element:paragraph" class="required"></textarea><br />
	<span class="editor-sample"><webl:i18n key="module.text.paragraph.paragraph.sample"/></span>
</p>
<h2><webl:i18n key="module.repository.image.alignment"/><span class="required">*</span></h2>
<p>
   <select name="property:alignment" class="required">
	   <option value="right"><webl:i18n key="module.repository.image.right_floated"/></option>
	   <option value="left"><webl:i18n key="module.repository.image.left_floated"/></option>
   </select>
</p>