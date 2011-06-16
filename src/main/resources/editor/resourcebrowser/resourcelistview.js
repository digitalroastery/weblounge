steal.plugins('jquery/view/tmpl', 'jqueryui/dialog')
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
		
		_openDialog: function(dialog, message) {
			this.options.selectedPages = $('div.listView table input:checked');
			if(this.options.selectedPages.length) {
				dialog.dialog('open');
				this._showMessage();
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"img.settings click": function(el, ev) {
			steal.dev.log('settings')
			var pageID = el.parents('tr').find('td:first-child input').attr('id');
			steal.dev.log('delete: ' + pageID);
		},
		
		"img.delete click": function(el, ev) {
			var pageID = el.parents('tr').find('td:first-child input').attr('id');
			steal.dev.log('delete: ' + pageID);
		},
		
		"img.favorite click": function(el, ev) {
			steal.dev.log('favorize')
		},
		
		"button.duplicate click": function(el, ev) {
			this._openDialog(this.duplicateDialog, 'Seite dupliziert');
		},
		
		"button.delete click": function(el, ev) {
			this._openDialog(this.deleteDialog, 'Seite gelöscht');
		},
		
		"button.favorize click": function(el, ev) {
			this.options.selectedPages = $('div.listView table input:checked');
			if(this.options.selectedPages.length) {
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
	});

});
