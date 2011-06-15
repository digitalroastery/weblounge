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
				$('div.scrollableArea').append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {page: res});
			});
			
			$('div.scrollviewitem').editor_resourcescrollviewitem();
		},
		
		"button.duplicate click": function(el, ev) {
			if($('div.page.marked').length) {
				this.duplicateDialog.dialog('open');
				this._showMessage('Seite dupliziert');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.delete click": function(el, ev) {
			if($('div.page.marked').length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.favorize click": function(el, ev) {
			if($('div.page.marked').length) {
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
	});

});
