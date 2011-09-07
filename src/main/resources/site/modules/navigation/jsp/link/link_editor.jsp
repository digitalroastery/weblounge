<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<div id="wbl-linkEditor">
<p>
    <label for="wbl-linkTitle"><webl:i18n key="module.navigation.linkanchor.title"/></label> <input id="wbl-linkTitle" type="text" name="element:title" />
    <label for="wbl-linkDesc">Beschreibung:</label> <input id="wbl-linkDesc" type="text" name="element:description" />
    <label for="wbl-linkAnchor">Anker:</label> <input id="wbl-linkAnchor" type="text" name="property:anchor" />
</p>
<p>
    <label for="wbl-linkType">Typ:</label>
    <select id="wbl-linkType" name="property:type">
        <option selected value="internal">interne Seite</option>
        <option value="external">externe URL</option>
    </select>
</p>

<!-- external -->
<p class="wbl-linkExternal">
    <label for="wbl-linkExternal">URL:</label> <input id="wbl-linkExternal" type="text" name="property:link" /><br />
    <label for="wbl-linkWindow">In neuem Fenster:</label> <input id="wbl-linkWindow" type="radio" name="property:window" value="Ja" checked="checked"> Ja <input type="radio" name="property:window" value="Nein"> Nein<br />
</p>

<!-- internal -->
<div class="wbl-linkInternal">
    <input id="wbl-linkInternal" type="text" name="property:pageid" readonly="readonly" /><br /><br />
	<button id="wbl-linkInternalButton" type="button">Select Page</button>
</div>

</div>