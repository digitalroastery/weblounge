steal.plugins('jquery/controller/view', 'jquery/view/tmpl')
.views('//editor/pageheadeditor/views/init.tmpl')
.models('language', 'template')
.then(function($) {

$.Controller('Editor.Pageheadeditor', 
	/* @static */
	{},
	/* @prototype */
	{
		init: function(el) {
			$(el).html('//editor/pageheadeditor/views/init.tmpl', {languages: Language.findAll(), templates: Template.findAll()});
		}
	}
);
});