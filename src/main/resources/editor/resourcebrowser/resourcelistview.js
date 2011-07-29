steal.plugins('jquery/view/tmpl')
.views('//editor/resourcebrowser/views/resourcelistview.tmpl')
.resources('jquery.dataTables.min', 'jquery.tablesorter.min', 'jquery.tablesorter.pager')
.then('resourceview', 'resourcelistviewitem')
.css('resources/images/blue/style', 'resources/jquery.tablesorter.page')
.then(function($) {

	Editor.Resourceview.extend('Editor.Resourcelistview', 
	{	
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcelistview.tmpl', {runtime: this.options.runtime});
			this._initViewItems();
			this._initButtons();
			this._initDialogs();
			this._initDataTable();
		},
		
		update: function(options) {
			this.options.resources = options.resources;
////			this.dataTable.fnClearTable();
			this.find('tr.wbl-pageEntry').remove();
			this._initViewItems();
////			this.dataTable = this.dataTable.dataTable({
//				"bPaginate": true,
//				"bLengthChange": true,
//				"bRetrieve": true,
//				"bFilter": false,
//				"bDestroy": true,
//				"bSort": true,
//				"bInfo": true,
//				"bAutoWidth": true,
//				"bJQueryUI": true
//			});
//			this.dataTable.fnStandingRedraw();
//			this.dataTable.fnDataUpdate();
//			this._initDataTable();
			
//			var page_size = 20;
//			var curr_page = config.page;
//			this.table.tablesorterPager({size: config.totalRows});
			this.table.trigger("update");
//			this.table.trigger("appendCache");
//			this.table.trigger("sorton", [[0,0]]);
//			this.table.tablesorterPager({size: page_size, page: curr_page}); 
			
//			var page_size = 20;
//			var curr_page = config.page;
//			this.table.tablesorterPager({size: page_size, page: curr_page}); 
		},
		
		_initDataTable: function() {
			this.table = this.find('table').tablesorter({
				sortList: [[0,0]],
		        headers: { 
		            4: { sorter: false }
		        },
		        widgets: ['zebra']
			}).tablesorterPager({
				container: this.element.find("#wbl-pager")
			});
			
			// init jquery dataTable Plugin over Table
//			this.dataTable = this.find('table').dataTable({
//				"bPaginate": true,
//				"bLengthChange": true,
//				"bRetrieve": true,
//				"bFilter": false,
//				"bSort": true,
//				"bInfo": true,
//				"bAutoWidth": true,
//				"bJQueryUI": true
//			});
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
