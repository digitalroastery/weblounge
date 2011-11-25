<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<style type="text/css">
<!--
	ul.editor li label {
		width: 110px;
		float: left;
		}	
	ul.editor li label.checkbox {
		padding-left: 110px;
		
		}			
-->
</style>

<webl:context define="language, site">
<form>
	<div id="mainTabContainer" style="width:100%; height:435px;">
		<div id="Standard" style="margin: 10px;">
			<ul class="editor">
				<li>
					<label for="element:title"><webl:i18n key="module.navigation.linklist.title" /> <span class="required">*</span></label>
					<input name="element:title" type="text" />
				</li>
				<li>
					<label for="property:categories"><webl:i18n key="module.navigation.linklist.keyword" /> <span class="required">*</span></label>
					<input name="property:categories" type="text" />
				</li>
				<li>
					<label for="property:show"><webl:i18n key="module.navigation.linklist.size" />&nbsp;<span class="required">*</span></label>
					<input name="property:show" class="required" type="text" />
				</li>
				<li>
					<label for="property:archiv">Archiv-Link</label>
					<input name="property:archiv" type="text" />
				</li>
				<li>
					<label for="lead" class="checkbox"><input name="property:lead" type="checkbox" /></label>
					<webl:i18n key="module.navigation.linklist.showlead" />
				</li>
				<li>
					<label for="date" class="checkbox"><input name="property:date" type="checkbox" /></label>
					<webl:i18n key="module.text.title.showdate"/>
				</li>
				<li>
					<label for="showkeyword" class="checkbox"><input name="property:showkeyword" type="checkbox" /></label>
					<webl:i18n key="module.navigation.linklist.showkeyword"/>
				</li>
				<li>
					<label for="rss" class="checkbox"><input name="property:RSS" type="checkbox" id="rss" /></label>
					<webl:i18n key="module.navigation.linklist.showrss"/>
				</li>
			</ul>
		</div>
	</div>
</form>
</webl:context>