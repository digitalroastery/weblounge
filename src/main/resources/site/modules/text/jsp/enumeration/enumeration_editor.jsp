<%--
 *
 * enumeration
 * dialog to create an ordered/unordered list
 *
 * @dependency  -
 * @attributes  -
 * @parameters  -
 *
 * @version     weblounge 3
 * @author      Simon Betschmann
 *
--%>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<script type="text/javascript" src="http://scripts.swissunihockey.ch/js/jQuery/plugins/tablednd-0.5.js"></script>

<script>
	$(document).ready(function() {
		// make the table sortable
		$('#enumeration').tableDnD({
			dragHandle: "dragHandle"
		});
		// add a hover-efect
		$("#enumeration tr").hover(function() {
			$(this.cells[0]).addClass('showDragHandle');
		}, function() {
			$(this.cells[0]).removeClass('showDragHandle');
		});
	});
	
	//add a new element
	function AddLi() {
		$('tr:last').parent().append('<tr id="NEU"><td class="dragHandle">&nbsp;</td><td><wiz:textarea name="element:text" description="enumeration n" value=" " /></td></tr>');
		dojo.query("table#enumeration > tbody > tr", document).forEach(
			function(tr, i) {
				tr.id = (i+1);
			}
		);
		$('#enumeration').tableDnD({
			dragHandle: "dragHandle"
		});
		$("#enumeration tr").hover(function() {
			$(this.cells[0]).addClass('showDragHandle');
		}, function() {
			$(this.cells[0]).removeClass('showDragHandle');
		});
	}
	
	//remove an element
	function RemoveLi(element) {
		$('tr#' + element).remove();
	}
</script>

<h1><webl:i18n key="module.text.enumeration.enumeration"/></h1>
<webl:context define="uri, language, site">
	<h2><webl:i18n key="module.text.enumeration.title"/></p>
	<p><input type="text" name="element:zwischentitel" /></p>
	<h2><webl:i18n key="module.text.enumeration.type"/></p>	
	<p><select name="property:type" class="required" >
			<woption value="ul"><webl:i18n key="module.text.enumeration.unnumbered"/></option>
			<option value="ol"><webl:i18n key="module.text.enumeration.numbered"/></option>
		</wiz:select>
	</p>
			
	<table id="enumeration">
		<webl:element-iterator name="element:text" minOccurs="1">
			<% int i = index.intValue(); %>
			<tr id="<%= i %>">
				<td class="dragHandle">&nbsp;</td>
				<%--TODO: delete-button doesn't work before the wizard was saved --%>
				<td><wiz:textarea name="element:text" description="enumeration item" class="required" style="width:360px" /> <img src="delete.png" alt="del" title="<%= site.getI18n().getAsHTML("module.text.enumeration.delete", language) %>" onclick="RemoveLi(<%= i %>);" style="cursor:pointer" /></td>
			</tr>
		</webl:element-iterator>
	</table>
	<p class="add-icon"><a href="javascript:;" onclick="AddLi();">Element hinzuf&uuml;gen</a></p>
	<p class="info">Hinweis: Elemente m&uuml;ssen zuerst gespeichert werden, bevor sie gel&ouml;scht werden k&ouml;nnen. Die Reihenfolge der Eintr&auml;ge k&ouml;nnen per drag &amp; drop ver&auml;ndert werden.</p>

</webl:context>