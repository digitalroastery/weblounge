steal.plugins('jquery/view/tmpl')
.views('//editor/resourcebrowser/views/resourcelistview.tmpl')
.resources('jquery.dataTables.min')
.then('resourceview', 'resourcelistviewitem')
.then(function($) {

	Editor.Resourceview.extend('Editor.Resourcelistview', 
	{	
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcelistview.tmpl', {});
			this._initViewItems();
			this._initButtons();
			this._initDialogs();
			this._initDataTable();
		},
		
		update: function(options) {
			this.options.resources = options.resources;
			this.find('tr.pageEntry').remove();
			this._initViewItems();
		},
		
		_initDataTable: function() {
			// init jquery dataTable Plugin over Table
			this.find('table').dataTable({
				"bPaginate": true,
				"bLengthChange": true,
				"bFilter": true,
				"bSort": true,
				"bInfo": true,
				"bAutoWidth": true,
				"bJQueryUI": true,
			});
		},
		
		_initViewItems: function() {
			$.each(this.options.resources, function(i, res) {
				var listViewItem = $('#listViewContent').append('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {page: res});
				listViewItem.find('tr.pageEntry').editor_resourcelistviewitem({page: res});
			});
		},
		
		"img.settings click": function(el, ev) {
			var pageId = this.options.selectedPages = el.parents('tr.pageEntry');
			steal.dev.log('open settings: ' + pageId);
		},
		
		"img.delete click": function(el, ev) {
			this.options.selectedPages = el.parents('tr.pageEntry');
			if(this.options.selectedPages.length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
			
		},
		
		"img.favorite click": function(el, ev) {
			this.element.trigger('favorizePages', [el.parents('tr.pageEntry')]);
			this._showMessage('Zu Favoriten hinzugefügt');
		},
		
		"button.duplicate click": function(el, ev) {
			this.options.selectedPages = this.find('tr.pageEntry input:checked').parents('tr.pageEntry');
			if(this.options.selectedPages.length == 1) {
				this.duplicateDialog.dialog('open');
			} else if(this.options.selectedPages.length > 1) {
				this._showMessage('Es kann nur eine Seite markiert werden.')
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.delete click": function(el, ev) {
			this.options.selectedPages = this.find('tr.pageEntry input:checked').parents('tr.pageEntry');
			if(this.options.selectedPages.length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.favorize click": function(el, ev) {
			this.options.selectedPages = this.find('tr.pageEntry input:checked').parents('tr.pageEntry');
			if(this.options.selectedPages.length) {
				this.element.trigger('favorizePages', [this.options.selectedPages]);
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
	});

});
