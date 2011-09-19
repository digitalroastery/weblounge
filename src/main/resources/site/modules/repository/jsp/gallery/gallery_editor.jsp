<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<script>
	$(document).bind('pageletEditorOpen', function(event, options) {
		$('#wbl-pageleteditor div#wbl-galleryEditor').editor_gallery(options);
	});
</script>

<div id="wbl-galleryEditor">
	<p>
	    <label for="wbl-galleryTitle"><webl:i18n key="module.repository.gallery.title"/></label> <input id="wbl-galleryTitle" type="text" name="element:title" />
	    <label for="wbl-galleryDesc"><webl:i18n key="module.repository.gallery.description"/></label> <input id="wbl-galleryDesc" type="text" name="element:description" /><br /><br />
		<button id="wbl-galleryFilesButton" type="button"><webl:i18n key="module.repository.gallery.button"/></button><br />
		<img src=""></img>
	    <input id="wbl-galleryFiles" type="hidden" name="property:resourceid" />
	</p>
</div>