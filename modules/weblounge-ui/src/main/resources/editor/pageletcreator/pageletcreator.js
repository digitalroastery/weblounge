steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl')
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
			this._hide();
			this.options.runtime.getSiteModules(this.callback('_initViews', el));
			var designerTab = $('div#weblounge-editor a.wbl-tab.wbl-designer');
			
			$(window).resize($.proxy(function() {
				if(!this.element.is(":visible") || !designerTab.hasClass('wbl-active')) return;
				this._calculateHeight();
			}, this));

	    },
	    
	    _initViews: function(el, modules) {
	    	var siteId = this.options.runtime.getId();
//	    	var favorites = new Array();
//	    	var favoritesLength = localStorage['weblounge.editor.' + siteId + '.favorites.size'];
//	        for (var i = 0; i < favoritesLength; i++) {
//		    	var pagelet = {};
//		    	pagelet.name = localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'name'];
//		    	pagelet.module = localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'module'];
//	        	favorites[i] = pagelet;
//	        }
	    	
			$.each(modules, $.proxy(function(index, module) {
		    	this.options.runtime.getModulePagelets(module.id, function(pagelets) {
		    		modules[index].pagelets = pagelets;
		    	});
			}, this));
	    	
			$(el).html('//editor/pageletcreator/views/init.tmpl', {modules: modules, favorites: {}});
			
			var tabs = this.element.tabs('div.wbl-panes > div');
			
			this._loadContent(modules);
			
//			var tab_items = $("ul:first li:first", tabs).droppable({
//				accept: ".wbl-draggable",
//				hoverClass: "wbl-tabHover",
//				tolerance: 'pointer',
//				drop: function(event, ui) {
//					var item = $(this);
//					var list = $(item.find("a").attr("href"));
//					if(list.has('div#'+ui.draggable.attr('id')).length) return;
//					ui.draggable.hide("slow", function() {
//						tabs.tabs("select", tab_items.index(item));
//						$(this).appendTo(list).show("slow");
//					});
//					
//			    	var pagelet = {};
//			    	pagelet.module = ui.draggable.attr('module');
//			    	pagelet.name = ui.draggable.attr('id');
////			    	favorites.push(pagelet);
////			    	
////			    	localStorage['weblounge.editor.' + siteId + '.favorites.size'] = favorites.length;
////			        for (var i = 0; i < favorites.length; i++) {
////			        	localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'name'] = favorites[i].name;
////			        	localStorage['weblounge.editor.' + siteId + '.favorites' + i + 'module'] = favorites[i].module;
////			        }
//				}
//			});
	    },
	    
	    _initDraggable: function(contentPane) {
	    	contentPane.find('.wbl-draggable').draggable({
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
					$('.composer:not(.locked)').editor_composer('enable');
				}
			}).disableSelection();
	    },
	    
	    _bodyAbsoluteTop: function(pixel) {
			$('body > *').each(function(index, elem) {
				if($(elem).css('position') == 'absolute') {
					var cssTop = $(elem).css('top');
					if(cssTop == '') return;
					var top = parseFloat(cssTop);
					$(elem).css('top', pixel + 'px');
				}
			});
	    },
	    
	    update: function() {
	    	if(!this.enabled) return;
	    	$("div#weblounge-editor").css("height","185px");
	    	this._bodyAbsoluteTop(185);
	    	this.element.show();
	    	this._calculateHeight();
	    },
	    
	    enable: function() {
	    	this.enabled = true;
	    },
	    
	    disable: function() {
	    	this.enabled = false;
	    	this._hide();
	    },
	    
	    _hide: function() {
	    	this.element.hide();
	    	$("div#weblounge-editor").css("height","45px");
	    	this._bodyAbsoluteTop(45);
	    },
	    
	    _loadContent: function(modules) {
			$.each(modules, $.proxy(function(index, module) {
	    		var contentPane = this.element.find('div.wbl-pane' + module.id);
	    		if(contentPane.length < 1) return;
	    		$.each(module.pagelets, function(key, pagelet) {
	    			contentPane.append('<div id="' + pagelet.id + '" class="wbl-draggable ui-widget-content" module="' + module.id + '">' + pagelet.id + '</div>');
	    		});
	    		this._calculateHeight();
	    		this._initDraggable(contentPane);
			}, this));
	    },
	    
	    _calculateHeight: function() {
	    	var visiblePane = this.element.find('div.wbl-panes > div:visible');
	    	if(!this.enabled) return;
	    	var elements = visiblePane.children();
	    	if(elements.length < 2) return;
	    	var firstTop = elements.first().position().top;
	    	var lastTop = elements.last().position().top;
	    	if(firstTop == lastTop) {
	    		this.element.find('div.wbl-panes').css('height', '80px');
	    		$("div#weblounge-editor").css("height","185px");
	    		this._bodyAbsoluteTop(185);
	    	} else {
	    		this.element.find('div.wbl-panes').css('height', '120px');
	    		$("div#weblounge-editor").css("height","225px");
	    		this._bodyAbsoluteTop(225);
	    	}
	    }
	    
  	});
});
