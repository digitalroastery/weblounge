<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<div id="wbl-galleryEditor">
	<p>
	    <label for="wbl-galleryTitle">Titel:</label> <input id="wbl-galleryTitle" type="text" name="element:gallerytitle" />
	    <label for="wbl-galleryDesc">Beschreibung:</label> <input id="wbl-galleryDesc" type="text" name="element:description" /><br />
	    <input id="wbl-galleryFiles" type="text" name="property:resourceid" readonly="readonly" /><br /><br />
		<button id="wbl-galleryFilesButton" type="button">Select Files</button>
	</p>
</div>