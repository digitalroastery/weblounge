<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<div id="wbl-linkEditor">
<p>
    <label for="wbl-linkTitle"><webl:i18n key="module.navigation.linkanchor.title"/></label> <input id="wbl-linkTitle" type="text" name="element:linktitle" />
    <label for="wbl-linkDesc">Beschreibung:</label> <input id="wbl-linkDesc" type="text" name="element:description" />
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
    <label for="wbl-linkExternal">URL:</label> <input id="wbl-linkExternal" type="text" name="property:external" /><br />
    <label for="wbl-linkWindow">In neuem Fenster:</label> <input id="wbl-linkWindow" type="radio" name="property:window" value="Ja" checked="checked"> Ja <input type="radio" name="property:window" value="Nein"> Nein<br />
    <label for="wbl-linkAnchor">Anker:</label> <input id="wbl-linkAnchor" type="text" name="property:anchor" />
</p>

<!-- internal -->
<div class="wbl-linkInternal">
    <label for="wbl-linkInternal">Interne Seite suchen:</label> <input id="wbl-linkInternalSearch" type="text" name="property:internalsearch" />
    <input id="wbl-linkInternal" type="hidden" name="property:internal" /><br /><br />
     <table border="1" cellspacing="0" cellpadding="0">
         <thead>
            <tr>
                <th>Seite</th>
                <!--  
                <th>Seitentitel</th>
                <th>Erstellt</th>
                <th>Zuletzt editiert</th>
                -->
            </tr>
        </thead>
        <tbody id="wbl-linkInternalSearchResult" class="wbl-linkSearchResult"></tbody>
    </table>
</div>

</div>