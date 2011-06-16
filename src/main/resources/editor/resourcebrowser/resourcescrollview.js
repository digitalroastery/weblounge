steal.plugins('jquery/view/tmpl','jqueryui/widget')
.views('//editor/resourcebrowser/views/resourcescrollview.tmpl')
.resources('jquery.smoothDivScroll-1.1')
.css('smoothDivScroll')
.then('resourceview', 'resourcescrollviewitem')
.then(function($) {
	
	Editor.Resourceview.extend('Editor.Resourcescrollview', 
	{
		init: function(el) {
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
				steal.dev.log(res.id);
				var scrollViewItem = $('div.scrollableArea').append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {page: res});
				scrollViewItem.find('div.scrollviewitem').editor_resourcescrollviewitem({page: res});
			});
		},
		
		_openDialog: function(dialog, message) {
			this.options.selectedPages = $('div.page.marked');
			if(this.options.selectedPages.length) {
				dialog.dialog('open');
				this._showMessage();
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.duplicate click": function(el, ev) {
			this._openDialog(this.duplicateDialog, 'Seite dupliziert');
		},
		
		"button.delete click": function(el, ev) {
			this._openDialog(this.deleteDialog, 'Seite gelöscht');
		},
		
		"button.favorize click": function(el, ev) {
			this.options.selectedPages = $('div.page.marked');
			if(this.options.selectedPages.length) {
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		}
		
	});

});
