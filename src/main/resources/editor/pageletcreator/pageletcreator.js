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

		}
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new Pageletcreator controller.
		 */
		init: function(el) {
			$("body").css("margin-top","185px");
			Site.getModules({}, $.proxy(function(modules) {
				// LOAD FAVORITES
				$(el).html('//editor/pageletcreator/views/init.tmpl', {modules: modules});
				
				var tabs = $("#module_tabs").tabs();
				
				var tab_items = $("ul:first li:first", tabs).droppable({
					accept: ".draggable",
	    			hoverClass: "tab_hover",
	    			tolerance: 'pointer',
					drop: function(event, ui) {
						var item = $(this);
						var list = $(item.find("a").attr("href"));
						if(list.has('div#'+ui.draggable.attr('id')).length) return;
						ui.draggable.hide("slow", function() {
							tabs.tabs("select", tab_items.index(item));
							$(this).appendTo(list).show("slow");
						});
						// TODO SAVE Favorites
					}
				});
				
				this.element.find('#module_favorites .draggable').draggable({
					connectToSortable: ".composer",
					helper: "clone",
					revert: "invalid",
					cursor: 'move',
					cursorAt: { top: -8, left: -10 },
					start: function(e, ui) {
						$(ui.helper).removeClass('draggable');
						$(ui.helper).addClass('draggable_helper');
					}
				}).disableSelection();
				
			}, this));
	    },
	    
	    update: function() {
	    	$("body").css("margin-top","185px");
	    	this.element.show();
	    },
	    
	    _loadContent: function(module) {
			Site.getModule({module: module}, function(pagelets){
				var tabContent = $("#tabcontent").empty();
				$.each(pagelets, function(key, pagelet) {
					tabContent.append('<div id="' + pagelet.id + '" class="draggable ui-widget-content" module="' + module + '">' + pagelet.id + '</div>');
				});
				tabContent.find('.draggable').draggable({
					connectToSortable: ".composer",
					helper: "clone",
					revert: "invalid",
					cursor: 'move',
					cursorAt: { top: -8, left: -10 },
					start: function(e, ui) {
						$(ui.helper).removeClass('draggable');
						$(ui.helper).addClass('draggable_helper');
					}
				}).disableSelection();
			});
	    },
	    
	    "li a click": function(el, ev) {
	    	if(el.attr('id') == 'favorites') return;
	    	this._loadContent(el.attr('id'));
	    },
	    
	    "a.tab_close click": function(el, ev) {
	    	this.element.hide();
	    	$("body").css("margin-top","45px");
	    }
	    
  	});
});
