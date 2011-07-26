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
			this.find('tr.wbl-pageEntry').remove();
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
				"bJQueryUI": true
			});
		},
		
		_initViewItems: function() {
			$.each(this.options.resources,$.proxy(function(i, res) {
				var listViewItem = this.element.find('#wbl-listViewContent').append('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {page: res, runtime: this.options.runtime});
				listViewItem.find('tr.wbl-pageEntry').editor_resourcelistviewitem({page: res});
			}, this));
		},
		
		"img.wbl-settings click": function(el, ev) {
			this.options.selectedPages = el.parents('tr.wbl-pageEntry');
			steal.dev.log('open settings: ' + this.options.selectedPages);
		},
		
		"img.wbl-delete click": function(el, ev) {
			this.options.selectedPages = el.parents('tr.wbl-pageEntry');
			if(this.options.selectedPages.length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
			
		},
		
		"img.wbl-favorite click": function(el, ev) {
			this.element.trigger('favorizePages', [el.parents('tr.wbl-pageEntry')]);
			this._showMessage('Zu Favoriten hinzugefügt');
		},
		
		"button.wbl-duplicate click": function(el, ev) {
			this.options.selectedPages = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			if(this.options.selectedPages.length == 1) {
				this.duplicateDialog.dialog('open');
			} else if(this.options.selectedPages.length > 1) {
				this._showMessage('Es kann nur eine Seite markiert werden.')
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.wbl-delete click": function(el, ev) {
			this.options.selectedPages = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			if(this.options.selectedPages.length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.wbl-favorize click": function(el, ev) {
			this.options.selectedPages = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			if(this.options.selectedPages.length) {
				this.element.trigger('favorizePages', [this.options.selectedPages]);
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		}
		
	});

});
