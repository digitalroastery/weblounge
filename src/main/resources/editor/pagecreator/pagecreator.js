steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl')
.views('//editor/pageletcreator/views/init.tmpl')
.css('pagecreator')
.models()
.then(function($) {

	/**
	 * @class Editor.Pagecreator
	 */
	$.Controller('Editor.Pagecreator',
	/* @Static */
	{
		defaults : {
		
		}
	},
	/* @Prototype */
	{
		init : function(){
			this.element.html("Hello World!");
		}
	})

});