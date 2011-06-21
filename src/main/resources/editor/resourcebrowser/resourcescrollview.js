steal.plugins('jquery/view/tmpl')
.views('//editor/resourcebrowser/views/resourcescrollview.tmpl')
.resources('jquery.smoothDivScroll-1.1')
.css('smoothDivScroll')
.then('resourceview', 'resourcescrollviewitem')
.then(function($) {
	
	Editor.Resourceview.extend('Editor.Resourcescrollview', 
	{
		init: function(el) {
			steal.dev.log('neue scrollview');
			$(el).html('//editor/resourcebrowser/views/resourcescrollview.tmpl', {});
			this._initViewItems();
			this._initButtons();
			this._initDialogs();
			this._initSmoothDivScroll();
		},
		
		update: function(options) {
			this.options.resources = options.resources;
			this.find('div.scrollviewitem').remove();
			this._initViewItems();
		},
		
		_initSmoothDivScroll: function() {
			this.find('#makeMeScrollable').smoothDivScroll({
			  	autoScroll: "onstart" , 
				autoScrollDirection: "backandforth", 
				autoScrollStep: 1, 
				autoScrollInterval: 15,	
				visibleHotSpots: "always"
		  	});
		},
		
		_initViewItems: function() {
			$.each(this.options.resources, function(i, res) {
				var scrollViewItem = $('div.scrollableArea').append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {page: res});
				scrollViewItem.find('div.scrollviewitem').editor_resourcescrollviewitem({page: res});
			});
		},
		
		"button.duplicate click": function(el, ev) {
			this.options.selectedPages = this.find('div.editor_resourcescrollviewitem.marked');
			if(this.options.selectedPages.length == 1) {
				this.duplicateDialog.dialog('open');
			} else if(this.options.selectedPages.length > 1) {
				this._showMessage('Es kann nur eine Seite markiert werden.')
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.delete click": function(el, ev) {
			this.options.selectedPages = this.find('div.editor_resourcescrollviewitem.marked');
			if(this.options.selectedPages.length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.favorize click": function(el, ev) {
			this.options.selectedPages = $('div.editor_resourcescrollviewitem.marked');
			if(this.options.selectedPages.length) {
				this.options.selectedPages.trigger('favorizePages', [this.options.selectedPages]);
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		}
		
	});

});
