steal.plugins('jquery/view/tmpl')
.views('//editor/resourcebrowser/views/resourcelistview.tmpl')
.resources('jquery.tablesorter', 'jquery.tablesorter.pager')
.then('resourceview', 'resourcelistviewitem')
.css('resources/images/blue/style', 'resources/jquery.tablesorter.pager')
.then(function($) {

	Editor.Resourceview.extend('Editor.Resourcelistview', 
	{	
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcelistview.tmpl', {runtime: this.options.runtime});
			this._initViewItems();
			this._initButtons();
			this._initFilter();
			this._initDialogs();
			this._initDataTable(10);
		},
		
		update: function(options) {
			this.options.resources = options.resources;
			this.find('tr.wbl-pageEntry').remove();
			this.table.trigger("update");
			if($.isEmptyObject(this.options.resources)) return;
			this._initViewItems();
			this._initDataTable();
		},
		
		_initDataTable: function(pagingSize) {
			this.table = this.find('table').tablesorter({
				sortList: [[0,0]],
		        headers: { 
		            4: { sorter: false }
		        },
		        widgets: ['zebra']
			}).tablesorterPager({
				container: this.element.find("#wbl-pager"),
				positionFixed: false,
				size: pagingSize,
				cssNext: '.wbl-next',
				cssPrev: '.wbl-prev',
				cssFirst: '.wbl-first',
				cssLast: '.wbl-last',
				cssPageDisplay: '.wbl-pageDisplay',
				cssPageSize: '.wbl-pageSize'
			});
		},
		
		_initViewItems: function() {
			$.each(this.options.resources,$.proxy(function(i, res) {
				var listViewItem = this.element.find('#wbl-listViewContent').append('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {
					page: res, 
					runtime: this.options.runtime,
					language: this.options.language
				});
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
			this.element.trigger('favorizeResources', [el.parents('tr.wbl-pageEntry')]);
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
				this.element.trigger('favorizeResources', [this.options.selectedPages]);
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		}
		
	});

});
