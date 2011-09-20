<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<script>
	$(document).bind('pageletEditorOpen', function(event, options) {
		$('#wbl-pageleteditor div#wbl-linkEditor').editor_link(options);
	});
</script>

<div id="wbl-linkEditor">
<p>
    <label for="wbl-linkTitle"><webl:i18n key="module.navigation.linkanchor.title"/></label> <input id="wbl-linkTitle" type="text" name="element:title" />
    <label for="wbl-linkDesc"><webl:i18n key="module.navigation.linkanchor.description"/></label> <input id="wbl-linkDesc" type="text" name="element:description" />
    <label for="wbl-linkAnchor"><webl:i18n key="module.navigation.linkanchor.anchor"/></label> <input id="wbl-linkAnchor" type="text" name="property:anchor" />
</p>
<p>
    <label for="wbl-linkType"><webl:i18n key="module.navigation.link.type"/></label>
    <select id="wbl-linkType" name="property:type">
        <option selected value="internal"><webl:i18n key="module.navigation.link.type.internal.name"/></option>
        <option value="external"><webl:i18n key="module.navigation.link.type.external.name"/></option>
    </select>
</p>

<!-- external -->
<p class="wbl-linkExternal">
    <label for="wbl-linkExternal"><webl:i18n key="module.navigation.link.external.url"/></label> <input id="wbl-linkExternal" type="text" name="property:link" /><br />
    <label for="wbl-linkWindow"><webl:i18n key="module.navigation.link.external.window"/></label> <input id="wbl-linkWindow" type="radio" name="property:window" value="Ja" checked="checked" /><webl:i18n key="module.navigation.link.external.window.yes"/> <input type="radio" name="property:window" value="Nein" /><webl:i18n key="module.navigation.link.external.window.no"/>
</p>

<!-- internal -->
<div class="wbl-linkInternal">
	<button id="wbl-linkInternalButton" type="button"><webl:i18n key="module.navigation.link.internal.button"/></button><br /><br />
    <input id="wbl-linkInternal" type="hidden" name="property:pageid" />
 	<img src=""></img>
</div>

</div>