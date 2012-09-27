<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:context define="uri, site, language">
  <ul id="tabs" class="nav nav-tabs">
    <li><a href="#tab-1" data-toggle="tab">Basic</a></li>
    <li><a href="#tab-2" data-toggle="tab">Advanced</a></li>
  </ul>

<div class="tab-content">

  <div id="tab-1" class="tab-pane active">
  
    <div class="control-group select">  
      <label for="method"><webl:i18n key="module.integration.timeline.height"/></label>
      <div class="controls input-append"><input name="property:timeline" id="timeline" class="required" type="string" /></div>
      <div class="controls input-append"><input name="property:height" id="height" class="required" type="number" /><span class="add-on">px</span></div>
    </div>

  </div>
  
  <div id="tab-2" class="tab-pane">
  
  </div> 
</webl:context>