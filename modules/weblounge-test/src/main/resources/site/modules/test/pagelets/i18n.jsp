<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<div id="i18n-module" class="pagelet">
  <webl:property define="i18n_module_key">
  	<webl:i18n key="<%= i18n_module_key %>"/>
  </webl:property>
</div>
<div id="i18n-page" class="pagelet">
  <webl:property define="i18n_page_key">
  	<webl:i18n key="<%= i18n_page_key %>"/>
  </webl:property>
</div>