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
			this.enabled = true;
			$("body").css("margin-top","185px");
			this.options.runtime.getSiteModules(this.callback('_initViews', el));
	    },
	    
	    _initViews: function(el, modules) {
	    	var siteId = this.options.runtime.getId();
	    	var favorites = new Array();
	    	var favoritesLength = localStorage['weblounge.editor.' + siteId + '.favorites.size'];
	        for (var i = 0; i < favoritesLength; i++) {
		    	var pagelet = {};
		    	pagelet.name = localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'name'];
		    	pagelet.module = localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'module'];
	        	favorites[i] = pagelet;
	        }
	    	
			$(el).html('//editor/pageletcreator/views/init.tmpl', {modules: modules, favorites: favorites});
			
			var tabs = $("#wbl-moduleTabs").tabs();
			
			var tab_items = $("ul:first li:first", tabs).droppable({
				accept: ".wbl-draggable",
				hoverClass: "wbl-tabHover",
				tolerance: 'pointer',
				drop: function(event, ui) {
					var item = $(this);
					var list = $(item.find("a").attr("href"));
					if(list.has('div#'+ui.draggable.attr('id')).length) return;
					ui.draggable.hide("slow", function() {
						tabs.tabs("select", tab_items.index(item));
						$(this).appendTo(list).show("slow");
					});
					
			    	var pagelet = {};
			    	pagelet.module = ui.draggable.attr('module');
			    	pagelet.name = ui.draggable.attr('id');
			    	favorites.push(pagelet);
			    	
			    	localStorage['weblounge.editor.' + siteId + '.favorites.size'] = favorites.length;
			        for (var i = 0; i < favorites.length; i++) {
			        	localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'name'] = favorites[i].name;
			        	localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'module'] = favorites[i].module;
			        }
				}
			});
			
			this._updateDraggable();
	    },
	    
	    update: function() {
	    	if(!this.enabled) return;
	    	$("body").css("margin-top","185px");
	    	this.element.show();
	    },
	    
	    enable: function() {
	    	this.enabled = true;
	    },
	    
	    disable: function() {
	    	this.enabled = false;
	    	this._hide();
	    },
	    
		_sleep: function(ms) {
			var dt = new Date();
			dt.setTime(dt.getTime() + ms);
			while (new Date().getTime() < dt.getTime());
		},
	    
	    _hide: function() {
	    	this.element.hide();
	    	$("body").css("margin-top","45px");
	    },
	    
	    _updateDraggable: function() {
			this.element.find('.wbl-draggable').draggable({
				connectToSortable: ".composer",
				helper: "clone",
				revert: "invalid",
				cursor: 'move',
				cursorAt: { top: -8, left: -10 },
				start: function(e, ui) {
					$(ui.helper).removeClass('wbl-draggable');
					$(ui.helper).addClass('wbl-draggableHelper');
				},
				stop: function(e, ui) {
					$('.composer').editor_composer('enable');
				}
			}).disableSelection();
	    },
	    
	    _loadContent: function(module) {
	    	this.options.runtime.getModulePagelets(module, $.proxy(function(pagelets) {
	    		var tabContent = $("#wbl-tabContent").empty();
	    		$.each(pagelets, function(key, pagelet) {
	    			tabContent.append('<div id="' + pagelet.id + '" class="wbl-draggable ui-widget-content" module="' + module + '">' + pagelet.id + '</div>');
	    		});
	    		this._updateDraggable();
	    	}, this));
	    },
	    
	    "li a click": function(el, ev) {
	    	if(el.attr('id') == 'favorites') return;
	    	this._loadContent(el.attr('id'));
	    },
	    
	    "a.wbl-tabClose click": function(el, ev) {
	    	this._hide();
	    }
	    
  	});
});
