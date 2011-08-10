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
			this._showPendingNotification();
			this.searchFlag = true;
		},
		
		update: function(showSearchBox) {
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
			
			if(this.searchFlag == true) return;
			this._showPendingNotification();
			this.scrollView.editor_resourcescrollview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime
			});
			this.listView.editor_resourcelistview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime
			});
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
		
		_showPendingNotification: function() {
			var query = {page: $.proxy(function(params) {
				Page.findPending(params, $.proxy(function(pages) {
					if($.isEmptyObject(pages)) {
						this.element.find("div#wbl-pendingCircle").html('0').fadeOut();
					} else {
						this.element.find("div#wbl-pendingCircle").html(pages.length).fadeIn();
					}
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findPending(params, $.proxy(function(media) {
					if($.isEmptyObject(media)) {
						this.element.find("div#wbl-pendingCircle").html('0').fadeOut();
					} else {
						this.element.find("div#wbl-pendingCircle").html(media.length).fadeIn();
					}
				}, this));
			}, this)};
			this._loadResources({}, query);
		},
		
		_showResourceScrollView: function(resources) {
			if(this.searchFlag == true) return;
			this.options.resources = resources;
			var element = this.find('div.wbl-thumbnailView');
			this._toggleElement(element);
			element.editor_resourcescrollview({
				resources: this.options.resources, 
				resourceType: this.options.resourceType,
				language: this.options.language, 
				runtime: this.options.runtime
			});
		},
		
		_showResourceListView: function(resources) {
			if(this.searchFlag == true) return;
			this.options.resources = resources;
			var element = this.find('div.wbl-listView');
			this._toggleElement(element);
			element.editor_resourcelistview({
				resources: this.options.resources,
				resourceType: this.options.resourceType,
				language: this.options.language, 
				runtime: this.options.runtime
			});
		},
		
		/**
		 * Remove deleted resource from resource array
		 */
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
        
        /**
         * Get Page from the resourceId
         */
	    _getPage: function(id) {
	    	var page = null;
	    	$.each(this.options.resources, function(i, resource) {
	    		if(resource.id == id) {
	    			page = resource;
	    			return false;
	    		}
    		});
	    	if(page == null) return null;
	    	return new Page({value: page});
	    },
		
		_toggleElement: function(el) {
        	this.activeElement.hide();
        	this.activeElement = el;
        	el.show();
        },
        
        _updateLast: function() {
        	this._loadResources(this.lastParams, this.lastQuery);
        },
        
        _loadResources: function(params, functions) {
			switch(this.options.resourceType) {
			case 'pages':
				functions.page(params);
				break;
			case 'media':
				functions.media(params);
				break;
			}
        },
        
        /**
         * Unmark the scrollViewItems if you click outside of a item
         */
        "div click": function(el, ev) {
        	ev.stopPropagation();
        	if(!el.is(this.element.find('div.wbl-scrollViewItem'))) {
        		this.element.find('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
        	}
        },
        
		"input searchResources": function(el, ev, searchValue) {
			if(searchValue == undefined) searchValue = '';
        	this.lastQuery = {page: $.proxy(function(params) {
        		Page.findBySearch(params, $.proxy(function(pages) {
        			this.options.resources = pages;
        			this.searchFlag = false;
        			this.update();
        		}, this));
        	}, this), media: $.proxy(function(params) {
    			Editor.File.findBySearch(params, $.proxy(function(pages) {
    				this.options.resources = pages;
    				this.searchFlag = false;
    				this.update();
    			}, this));
        	}, this)};
        	this.lastParams = {search: searchValue};
        	this._loadResources(this.lastParams, this.lastQuery);
		},
		
		"input filterResources": function(el, ev, filterValue) {
			if(filterValue == undefined) filterValue = '';
			this.lastParams.filter = filterValue;
			this._loadResources(this.lastParams, this.lastQuery)
		},
		
		"button.wbl-list click": function(el, ev) {
			this._showResourceListView(this.options.resources);
		},
		
		"button.wbl-thumbnails click": function(el, ev) {
			this._showResourceScrollView(this.options.resources);
		},
        
        // Delete Pages
        "div#wbl-mainContainer deleteResources": function(el, ev, resources) {
        	switch(this.options.resourceType) {
        	case 'pages':
        		$(resources).each($.proxy(function(index, element) {
        			var page = this._getPage(element.id);
        			if(page == null) return;
        			var locked = page.isLocked();
                	var userLocked = page.isLockedUser(this.options.runtime.getUserLogin());
                	var currentPath = location.pathname.substring(0, location.pathname.lastIndexOf('/') + 1);
                	
                	if(currentPath == '/') {
                		alert("Can't delete root page");
                	} else if(!locked || (locked && userLocked)) {
                		Page.destroy({id: element.id}, this.callback('_removeResource', element.id));
                		// Relocate to Root if current page was deleted
                		if(currentPath == page.getPath()) {
                			location.href = '/?edit';
                		}
                	} else {
                		alert("Can't delete " + page.getPath() + ": Page is locked!");
                	}
        		}, this))
        		break;
        	case 'media':
        		$(resources).each($.proxy(function(index, element) {
        			Editor.File.destroy({id: element.id}, this.callback('_removeResource', element.id));
        		}, this))
        		break;
        	}
        },
        
        // Duplicate Pages
        "div#wbl-mainContainer duplicateResources": function(el, ev, resources) {
        	// TODO
        	this.update();
        },
        
        // Favorize Pages
        "div#wbl-mainContainer favorizeResources": function(el, ev, resources) {
        	// TODO
        	this.update();
        },
        
		"button.wbl-recent click": function(el, ev) {
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findRecent(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findRecent(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this)};
			this.lastParams = {};
			this._loadResources(this.lastParams, this.lastQuery);
		},
		
		"button.wbl-favorites click": function(el, ev) {
			// TODO
//			this.searchFlag = false;
			this.update();
		},
		
		"button.wbl-pending click": function(el, ev) {
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findPending(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findPending(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this)};
			this.lastParams = {};
			this._loadResources(this.lastParams, this.lastQuery);
		},
		
		"button.wbl-all click": function(el, ev) {
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findAll(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findAll(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this)};
			this.lastParams = {};
			this._loadResources(this.lastParams, this.lastQuery);
		}
	});
});
