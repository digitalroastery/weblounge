steal.plugins(
	'jquery/controller/view',
	'jquery/view/tmpl')
.views('//editor/resourcebrowser/views/init.tmpl')
.css('resourcebrowser')
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
//			this._loadResources();
		},
		
		update: function() {
			if($.isEmptyObject(this.options.resources)) return;
			this.searchBox.hide();
			this.scrollView.editor_resourcescrollview({resources: this.options.resources, language: this.options.language});
			this.listView.editor_resourcelistview({resources: this.options.resources, language: this.options.language});
		},
		
		_initViewItems: function() {
			this.searchBox = this.find('div.searchBox').editor_resourcesearch({resourceType: this.options.resourceType});
			this.scrollView = this.find('div.thumbnailView');
			this.listView = this.find('div.listView');
			this.activeElement = this.scrollView;
			$('nav.weblounge div.view').buttonset();
			$('nav.weblounge div.filter').buttonset();
			
			$('nav.weblounge button.list').button({
				icons: {primary: "icon-list"},
				text: false });
			$('button.tree').button({
				icons: {primary: "icon-tree"},
				disabled: true,
				text: false });
			$('nav.weblounge button.thumbnails').button({
				disabled: false,
				icons: {primary: "icon-thumbnails"},
				text: false });
		},
		
		_showResourceScrollView: function(resources) {
			this.options.resources = resources;
			var element = this.find('div.thumbnailView');
			this._toggleElement(element)
			element.editor_resourcescrollview({resources: this.options.resources, language: this.options.language});
		},
		
		_showResourceListView: function(resources) {
			this.options.resources = resources;
			var element = this.find('div.listView');
			this._toggleElement(element)
			element.editor_resourcelistview({resources: this.options.resources, language: this.options.language});
		},
		
//		_loadResources: function() {
//			if(this.options.resourceType == 'pages') {
//				Page.findAll({}, this.callback('_showResourceScrollView'));
//			} else if(this.options.resourceType == 'media') {
//				Editor.File.findAll({}, this.callback('_showResourceScrollView'));
//			}
//		},
		
		_toggleElement: function(el) {
        	this.activeElement.hide();
        	this.activeElement = el;
        	el.show();
        },
        
		"input searchMedia": function(el, ev, serachValue) {
			steal.dev.log(serachValue);
		},
		
		"input searchPages": function(el, ev, serachValue) {
			steal.dev.log(serachValue);
		},
		
		"button.list click": function(el, ev) {
			if($.isEmptyObject(this.options.resources)) return;
			this._showResourceListView(this.options.resources);
		},
		
		"button.thumbnails click": function(el, ev) {
			if($.isEmptyObject(this.options.resources)) return;
			this._showResourceScrollView(this.options.resources);
		},
        
        // Delete Pages
        "div#mainContainer deletePages": function(el, ev, pages) {
//        	Page.remove(pages);
        	this.update();
        },
        
        // Duplicate Pages
        "div#mainContainer duplicatePages": function(el, ev, pages) {
//        	Page.duplicate(pages);
        	this.update();
        },
        
        // Favorize Pages
        "div#mainContainer favorizePages": function(el, ev, pages) {
//        	Page.duplicate(pages);
        	this.update();
        },
        
		"button.recent click": function(el, ev) {
			if(this.options.resourceType == 'pages') {
				Page.findRecent({}, $.proxy(function(pages) {
					this.options.resources = pages;
					this.update();
				}, this));
			}
		},
		
		"button.favorites click": function(el, ev) {
			steal.dev.log('favorites')
			this.update();
		},
		
		"button.pending click": function(el, ev) {
			steal.dev.log('show pending')
			this.update();
		},
		
		"button.all click": function(el, ev) {
			if(this.options.resourceType == 'pages') {
				Page.findAll({}, $.proxy(function(pages) {
					this.options.resources = pages;
					this.update();
				}, this));
			}
		}
	});
});
