<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<script>
	$(document).bind('pageletEditorOpen', function(event, options) {
		$('#wbl-pageleteditor div#wbl-downloadEditor').editor_download(options);
	});
</script>

<div id="wbl-downloadEditor">
	<p>
	    <label for="wbl-downloadTitle"><webl:i18n key="module.repository.download.title"/></label> <input id="wbl-downloadTitle" type="text" name="element:title" />
	    <label for="wbl-downloadDesc"><webl:i18n key="module.repository.download.description"/></label> <input id="wbl-downloadDesc" type="text" name="element:description" /><br /><br />
		<button id="wbl-downloadFileButton" type="button"><webl:i18n key="module.repository.download.button"/></button><br /><br />
	    <input id="wbl-downloadFile" type="hidden" name="property:resourceid" />
	    <img src=""></img>
	</p>
</div>