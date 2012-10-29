<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<ul id="tabs" class="nav nav-tabs">
	<li><a href="#tab-1" data-toggle="tab">Basic</a></li>
	<li><a href="#tab-2" data-toggle="tab">Advanced</a></li>
</ul>

<div class="tab-content">
	<div id="tab-1" class="tab-pane active">

		<div class="control-group"> 
			<label for="keyword"><webl:i18n key="module.text.title.keyword"/>:</label>
			<div class="controls">  
				<input name="element:keyword" type="text" id="keyword" />
			</div>
		</div>

		<div class="control-group"> 
			<label for="title"><webl:i18n key="module.text.title.title"/>:</label>
			<div class="controls">  
				<input name="element:title" class="required" type="text" id="title" />
			</div>
		</div>

		<div class="control-group"> 
			<label for="lead"><webl:i18n key="module.text.title.lead"/>:</label>
			<div class="controls">  
				<textarea name="element:lead" id="lead"></textarea>
			</div>
		</div>

	</div>

	<div id="tab-2" class="tab-pane">

		<div class="control-group"> 
			<label for="date"><webl:i18n key="module.text.title.showdate"/></label>
			<div class="controls">  
				<input name="property:date" type="checkbox" id="date" />
			</div>
		</div>

		<div class="control-group"> 
			<label for="author"><webl:i18n key="module.text.title.showauthor"/></label>
			<div class="controls">  
				<input name="property:author" type="checkbox" id="author" />
			</div>
		</div>

	</div>
</div>