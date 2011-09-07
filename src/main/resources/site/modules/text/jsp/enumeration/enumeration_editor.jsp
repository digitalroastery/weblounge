<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<h1><webl:i18n key="module.text.enumeration.enumeration"/></h1>
<webl:context define="language, site">
	<div id="mainTabContainerEnumeration" style="width:100%; height:400px;">
		<div id="Default" title="<%= site.getI18n().getAsHTML("module.text.enumeration.tab.default", language) %>" selected="true" style="margin: 10px;">	
		<form>
			<p class="editor-title"><webl:i18n key="module.text.enumeration.title"/></p>
			<p><input type="text" name="element:zwischentitel" /></p>
			<p class="editor-title"><webl:i18n key="module.text.enumeration.type"/></p>
			<p>
				<select name="property:type" i18n="module.text.enumeration.type" required="true" size="1">
					<option value="ul"><webl:i18n key="module.text.enumeration.unnumbered"/></option>
					<option value="ol"><webl:i18n key="module.text.enumeration.numbered"/></option>
				</select>
			</p>
			
			<table id="enumeration">
				<webl:element-iterator element="text" minOccurs="1">
					<% int i = index.intValue(); %>
					<tr id="<%= i %>">
						<td class="dragHandle">&nbsp;</td>
						<%--TODO: delete-button doesn't work before the wizard was saved --%>
						<td><textarea name="element:text" description="enumeration item" required="true" style="width:360px" /> <img id="<%= i %>" src="delete.png" alt="del" title="<%= site.getI18n().getAsHTML("module.text.enumeration.delete", language) %>" style="cursor:pointer" /></td>
					</tr>
				</webl:element-iterator>
			</table>
			<p class="add-icon"><a href="">Element hinzuf&uuml;gen</a></p>
			<p class="info">Hinweis: Elemente m&uuml;ssen zuerst gespeichert werden, bevor sie gel&ouml;scht werden k&ouml;nnen. Die Reihenfolge der Eintr&auml;ge k&ouml;nnen per drag &amp; drop ver&auml;ndert werden.</p>
		</form>
		</div>
	</div>
</webl:context>