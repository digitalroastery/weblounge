<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<p>
    <label for="linktitle"><webl:i18n key="module.navigation.linkanchor.title"/></label> <input id="linktitle" type="text" name="element:linktitle" />
</p>
<p>
    <label for="desc">Beschreibung:</label> <input id="desc" type="text" name="element:description" />
</p>
<p>
    <label for="type">Typ:</label>
    <select id="type" type="text" name="element:type">
        <option value="">Bitte wählen...</option>
        <option value="internal">interne Seite</option>
        <option value="external">externe URL</option>
        <option value="file">Download</option>
    </select>
</p>

<!-- external -->
<p class="external">
    <label for="external">URL:</label> <input id="external" type="text" name="element:external" />
</p>

<!-- internal -->
<p class="internal">
    <label for="internal">Interne Seite suchen:</label> <input id="internal" type="text" name="element:internal" />
    <span class="search_result">Suchergebnisse</span>
</p>

<!-- file-->
<p class="file">
    <label for="file">Datei suchen:</label> <input id="file" type="text" name="element:file" />
    <span class="search_result">Suchergebnisse</span>
</p>