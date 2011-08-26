<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<div id="wbl-downloadEditor">
	<p>
	    <label for="wbl-downloadTitle">Titel:</label> <input id="wbl-downloadTitle" type="text" name="element:downloadtitle" />
	    <label for="wbl-downloadDesc">Beschreibung:</label> <input id="wbl-downloadDesc" type="text" name="element:description" /><br />
	    <input id="wbl-downloadFile" type="text" name="property:file" readonly="readonly" /><br /><br />
		<button id="wbl-downloadFileButton" type="button">Select File</button>
	</p>
</div>