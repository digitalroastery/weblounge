steal.plugins(
	'jquery/controller/view',
	'jquery/view/tmpl')
.views('//editor/resourcebrowser/views/init.tmpl')
.resources('jquery.treeview')
.css('resourcebrowser', 'resources/jquery.treeview')
.then('resourcescrollview', 'resourcelistview', 'resourcesearch')
.then(function($) {

  $.Controller('Editor.Resourcebrowser',
	{
		defaults: {
			resources: {},
			resourceType: 'pages'
		}
	},
	{
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/init.tmpl', {});
			this._initViewItems();
			this.searchFlag = true;
//			this._loadResources();
		},
		
		update: function(showSearchBox) {
			if($.isEmptyObject(this.options.resources)) return;
			if(showSearchBox == true) {
				this.searchFlag = true;
				this.searchBox.show();
				this.scrollView.hide();
				this.listView.hide();
			} 
			else if(this.searchFlag == false) {
				this.activeElement.show();
				this.searchBox.hide();
			}
			this.scrollView.editor_resourcescrollview({resources: this.options.resources, language: this.options.language, runtime: this.options.runtime});
			this.listView.editor_resourcelistview({resources: this.options.resources, language: this.options.language, runtime: this.options.runtime});
		},
		
		_initViewItems: function() {
			this.searchBox = this.find('div.wbl-searchBox').editor_resourcesearch({
				resourceType: this.options.resourceType,
				language: this.options.language, 
				runtime: this.options.runtime
			});
			this.scrollView = this.find('div.wbl-thumbnailView');
			this.listView = this.find('div.wbl-listView');
			this.activeElement = this.scrollView;
			$('nav.weblounge div.wbl-view').buttonset();
			$('nav.weblounge div.wbl-filter').buttonset();
			
			$('nav.weblounge button.wbl-list').button({
				icons: {primary: "wbl-iconList"},
				text: false });
			$('nav.weblounge button.wbl-tree').button({
				icons: {primary: "wbl-iconTree"},
				disabled: true,
				text: false });
			$('nav.weblounge button.wbl-thumbnails').button({
				disabled: false,
				icons: {primary: "wbl-iconThumbnails"},
				text: false });
		},
		
		_showResourceScrollView: function(resources) {
			this.options.resources = resources;
			var element = this.find('div.wbl-thumbnailView');
			this._toggleElement(element)
			element.editor_resourcescrollview({resources: this.options.resources, language: this.options.language, runtime: this.options.runtime});
		},
		
		_showResourceListView: function(resources) {
			this.options.resources = resources;
			var element = this.find('div.wbl-listView');
			this._toggleElement(element)
			element.editor_resourcelistview({resources: this.options.resources, language: this.options.language, runtime: this.options.runtime});
		},
		
//		_loadResources: function() {
//			if(this.options.resourceType == 'pages') {
//				Page.findAll({}, this.callback('_showResourceScrollView'));
//			} else if(this.options.resourceType == 'media') {
//				Editor.File.findAll({}, this.callback('_showResourceScrollView'));
//			}
//		},
		
		/**
		 * Unmark the scrollViewItems if you click outside of a item
		 */
		"div click": function(el, ev) {
			ev.stopPropagation();
			if(!el.is(this.element.find('div.wbl-scrollViewItem'))) {
				this.element.find('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
			}
		},
		
        _removeResource: function(id) {
	    	var index = -1;
	    	$.each(this.options.resources, function(i, resources) {
	    		if(resources.id == id) {
	    			index = i;
	    			return false;
	    		};
    		});
	    	
	    	if(index == -1) return;
	    	
			this.options.resources.splice(index, 1);
			this.update();
        },
		
		_toggleElement: function(el) {
        	this.activeElement.hide();
        	this.activeElement = el;
        	el.show();
        },
        
		"input searchMedia": function(el, ev, searchValue) {
			Page.findBySearch({search: searchValue}, $.proxy(function(pages) {
				this.options.resources = pages;
				this.searchFlag = false;
				this.update();
			}, this));
		},
		
		"input searchPages": function(el, ev, searchValue) {
			Page.findBySearch({search: searchValue}, $.proxy(function(pages) {
				this.options.resources = pages;
				this.searchFlag = false;
				this.update();
			}, this));
		},
		
		"button.wbl-list click": function(el, ev) {
			if($.isEmptyObject(this.options.resources)) return;
			this._showResourceListView(this.options.resources);
		},
		
		"button.wbl-thumbnails click": function(el, ev) {
			if($.isEmptyObject(this.options.resources)) return;
			this._showResourceScrollView(this.options.resources);
		},
        
        // Delete Pages
        "div#wbl-mainContainer deletePages": function(el, ev, pages) {
        	pages.each($.proxy(function(index, element) {
        		Page.destroy({id: element.id}, this.callback('_removeResource', element.id));
        	}, this))
        },
        
        // Duplicate Pages
        "div#wbl-mainContainer duplicatePages": function(el, ev, pages) {
//        	Page.duplicate(pages);
        	this.update();
        },
        
        // Favorize Pages
        "div#wbl-mainContainer favorizePages": function(el, ev, pages) {
//        	Page.duplicate(pages);
        	this.update();
        },
        
		"button.wbl-recent click": function(el, ev) {
			switch(this.options.resourceType) {
				case 'pages':
					Page.findRecent({}, $.proxy(function(pages) {
						this.options.resources = pages;
						this.searchFlag = false;
						this.update();
					}, this));
					break;
				case 'media':
					// TODO Replace with Files Model
					Page.findRecent({}, $.proxy(function(media) {
						this.options.resources = media;
						this.searchFlag = false;
						this.update();
					}, this));
					break;
			}
		},
		
		"button.wbl-favorites click": function(el, ev) {
			steal.dev.log('favorites')
			this.searchFlag = false;
			this.update();
		},
		
		"button.wbl-pending click": function(el, ev) {
			steal.dev.log('show pending')
			this.searchFlag = false;
			this.update();
		},
		
		"button.wbl-all click": function(el, ev) {
			switch(this.options.resourceType) {
			case 'pages':
				Page.findAll({}, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
				break;
			case 'media':
				// TODO Replace with Files Model
				Page.findAll({}, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
				break;
			}
		}
	});
});
