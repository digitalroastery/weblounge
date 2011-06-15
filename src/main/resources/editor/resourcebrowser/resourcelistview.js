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
				steal.dev.log('res list: ' + res);
				$('#listViewContent').append('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {page: res});
			});
			
			this.find('tr.pageEntry').editor_resourcelistviewitem();
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
			if($('div.listView table input:checked').length) {
				this._showMessage('Seite dupliziert');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.delete click": function(el, ev) {
			if($('div.listView table input:checked').length) {
				this.confirmDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.favorize click": function(el, ev) {
			if($('div.listView table input:checked').length) {
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
	});

});
