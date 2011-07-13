steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/tabs')
.views('//editor/pageletcreator/views/init.tmpl')
.css('pageletcreator')
.models('../../models/site')
.then(function($) {
	
	$.Controller("Editor.Pageletcreator",	
	/* @static */
	{
		defaults: {
			max_file_size: '10mb'
	    }
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new Pageletcreator controller.
		 */
		init: function(el) {
			$("body").css("margin-top","200px");
			Site.getModules({}, $.proxy(function(modules) {
				$(el).html('//editor/pageletcreator/views/init.tmpl', {modules: modules});
				
				$("#module_tabs").tabs({
					// TODO
				});
				
				this._loadContent(modules[0].id);
			}, this));
	    },
	    
	    _loadContent: function(module) {
			Site.getModule({module: module}, function(pagelets){
				$("#tabcontent").empty().append(pagelets[0].id);
			});
	    },
	    
	    "li a click": function(el, ev) {
	    	this._loadContent(el.attr('id'));
	    }
	    
  	});
});
