<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:context define="language, site">
<script>
	$(document).bind('pageletEditorOpen', function() {
		$('#wbl-pageleteditor form').editor_enumeration({
			siteId: '<%= site.getIdentifier() %>',
			title: '<%= site.getI18n().getAsHTML("module.text.enumeration.delete", language) %>'
		});
	});
</script>

<h1><webl:i18n key="module.text.enumeration.enumeration"/></h1>
	<div id="mainTabContainerEnumeration">
		<p class="editor-title"><webl:i18n key="module.text.enumeration.title"/></p>
		<p><input type="text" name="element:zwischentitel" /></p>
		<p class="editor-title"><webl:i18n key="module.text.enumeration.type"/></p>
		<p>
			<select name="property:type" size="1">
				<option value="ul"><webl:i18n key="module.text.enumeration.unnumbered"/></option>
				<option value="ol"><webl:i18n key="module.text.enumeration.numbered"/></option>
			</select>
		</p>
		<table id="enumeration">
			<webl:element-iterator elements="text[\d*]" minOccurs="1">
				<tr id="<%= index %>">
					<td class="dragHandle">&nbsp;</td>
					<%--TODO: delete-button doesn't work before the wizard was saved --%>
					<td><textarea name="element:text<%= index %>"></textarea> <img id="<%= index %>" src="/weblounge-sites/<%= site.getIdentifier() %>/modules/text/jsp/enumeration/delete.png" alt="del" title="<%= site.getI18n().getAsHTML("module.text.enumeration.delete", language) %>" /></td>
				</tr>
			</webl:element-iterator>
		</table>
		<p class="add-icon"><a href="#new">Element hinzuf&uuml;gen</a></p>
		<p class="info">Hinweis: Elemente m&uuml;ssen zuerst gespeichert werden, bevor sie gel&ouml;scht werden k&ouml;nnen. Die Reihenfolge der Eintr&auml;ge k&ouml;nnen per drag &amp; drop ver&auml;ndert werden.</p>
	</div>
</webl:context>