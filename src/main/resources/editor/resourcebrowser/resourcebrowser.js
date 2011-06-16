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
		
		_update: function(pages) {
			this.scrollView.editor_resourcescrollview({resources: pages});
			this.listView.editor_resourcelistview({resources: pages});
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
        
        // Delete Pages
        "div deletePages": function(el, ev, pages) {
        	steal.dev.log('deletePages');
//        	Page.remove(pages);
        	this._update(pages);
        },
        
		"button.recent click": function(el, ev) {
			steal.dev.log('recent')
			this._update();
		},
		"button.favorites click": function(el, ev) {
			steal.dev.log('favorites')
		},
		"button.pending click": function(el, ev) {
			steal.dev.log('show pending')
		},
		"button.all click": function(el, ev) {
			steal.dev.log('show all')
		},
		"button.list click": function(el, ev) {
			this._showResourceListView(this.options.resources);
		},
		"button.thumbnails click": function(el, ev) {
			this._showResourceScrollView(this.options.resources)
		}
	});
});
