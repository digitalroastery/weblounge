<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<script>
	$(document).bind('pageletEditorOpen', function(event, options) {
		$('#wbl-pageleteditor div#wbl-galleryEditor').editor_gallery(options);
	});
</script>

<div id="wbl-galleryEditor">
	<p>
	    <label for="wbl-galleryTitle">Titel:</label> <input id="wbl-galleryTitle" type="text" name="element:title" />
	    <label for="wbl-galleryDesc">Beschreibung:</label> <input id="wbl-galleryDesc" type="text" name="element:description" /><br />
		<button id="wbl-galleryFilesButton" type="button">Select Files</button><br />
		<img src=""></img>
	    <input id="wbl-galleryFiles" type="hidden" name="property:resourceid" />
	</p>
</div>