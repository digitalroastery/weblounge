steal.plugins(
	'jquery/controller/view',
	'jquery/view/tmpl')
	.models('../../models/page')
.views('//editor/resourcebrowser/views/init.tmpl')
.css('resourcebrowser')
.then('resourcescrollview', 'resourcelistview')
.then(function($) {

  $.Controller('Editor.Resourcebrowser',
	{
		defaults: {
			resources: {},
			resourceType: 'pages',
		}
	},
	{
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/init.tmpl', {});
			this._initViewItems();
			this._loadResources();					
		},
		
		update: function() {
			this.scrollView.editor_resourcescrollview({resources: this.options.resources});
			this.listView.editor_resourcelistview({resources: this.options.resources});
		},
		
		_initViewItems: function() {
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
		
		_showResourceScrollView: function(pages) {
			this.options.resources = pages;
			var element = this.find('div.thumbnailView');
			this._toggleElement(element)
			element.editor_resourcescrollview({resources: this.options.resources});
		},
		
		_showResourceListView: function(pages) {
			this.options.resources = pages;
			var element = this.find('div.listView');
			this._toggleElement(element)
			element.editor_resourcelistview({resources: this.options.resources});
		},
		
		_loadResources: function() {
			if(this.options.resourceType == 'pages') {
				Page.findAll({}, this.callback('_showResourceScrollView'));
			} else if(this.options.resourceType == 'media') {
				steal.dev.log('load Media');
			}
		},
		
		_toggleElement: function(el) {
        	this.activeElement.hide();
        	this.activeElement = el;
        	el.show();
        },
        
		"button.list click": function(el, ev) {
			this._showResourceListView(this.options.resources);
		},
		
		"button.thumbnails click": function(el, ev) {
			this._showResourceScrollView(this.options.resources)
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
			steal.dev.log('recent')
			this.update();
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
			steal.dev.log('show all')
			this.update();
		}
	});
});
