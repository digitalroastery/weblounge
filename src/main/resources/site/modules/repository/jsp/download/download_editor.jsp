<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<script>
	$(document).bind('pageletEditorOpen', function() {
		$('#wbl-pageleteditor div#wbl-downloadEditor').editor_download();
	});
</script>

<div id="wbl-downloadEditor">
	<p>
	    <label for="wbl-downloadTitle"><webl:i18n key="module.repository.download.title"/></label> <input id="wbl-downloadTitle" type="text" name="element:title" />
	    <label for="wbl-downloadDesc"><webl:i18n key="module.repository.download.description"/></label> <input id="wbl-downloadDesc" type="text" name="element:description" /><br />
	    <input id="wbl-downloadFile" type="text" name="property:resourceid" readonly="readonly" /><br /><br />
		<button id="wbl-downloadFileButton" type="button"><webl:i18n key="module.repository.download.button"/></button>
	</p>
</div>