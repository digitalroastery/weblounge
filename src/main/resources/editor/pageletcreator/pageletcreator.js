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
				
				var tabs = $("#module_tabs").tabs();
				
				var tab_items = $("ul:first li:first", tabs).droppable({
					accept: ".draggable",
	    			hoverClass: "tab_hover",
					drop: function(event, ui) {
						var item = $(this);
						var list = $(item.find("a").attr("href"));
						if(list.has('li#'+ui.draggable.attr('id')).length) return;
						ui.draggable.hide("slow", function() {
							tabs.tabs("select", tab_items.index(item));
							$(this).appendTo(list).show("slow");
						});
					}
				});
				
				this.element.find('#module_favorites .draggable').draggable({
					connectToSortable: ".composer",
					helper: "clone",
					revert: "invalid"
				}).disableSelection();
				
				this._loadContent(modules[0].id);
			}, this));
	    },
	    
	    _loadContent: function(module) {
			Site.getModule({module: module}, function(pagelets){
				var tabContent = $("#tabcontent").empty();
				$.each(pagelets, function(key, pagelet) {
					tabContent.append('<div id="' + pagelet.id + '" class="draggable ui-widget-content">' + pagelet.id + '</div>');
				});
				tabContent.find('.draggable').draggable({
					connectToSortable: ".composer",
					helper: "clone",
					revert: "invalid"
				}).disableSelection();
			});
	    },
	    
	    "li a click": function(el, ev) {
	    	if(el.attr('id') == 'favorites') return;
	    	this._loadContent(el.attr('id'));
	    }
	    
  	});
});
