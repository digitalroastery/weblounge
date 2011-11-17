steal.plugins('jquery/view/tmpl')
.views('//editor/resourcebrowser/views/resourcelistview.tmpl')
.resources('jquery.tablesorter', 'jquery.tablesorter.pager')
.then('resourceview', 'resourcelistviewitem')
.css('resources/images/blue/style', 'resources/jquery.tablesorter.pager')
.then(function($) {

	Editor.Resourceview.extend('Editor.Resourcelistview', 
	{	
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcelistview.tmpl', {runtime: this.options.runtime, resourceType: this.options.resourceType});
			this._initViewItems();
			this._initButtons();
			this._initFilter();
			this._initDialogs();
			this.table = this.element.find('table.wbl-tablesorter');
			this.pager = this.element.find("#wbl-pager");
			this._initDataTable(10);
		},
		
		update: function(options) {
			this._super(options);
			this.find('tr.wbl-pageEntry').remove();
			this._initViewItems();
			this._initDataTable(this.element.find(".wbl-pageSize").val());
		},
		
		_initDataTable: function(pagingSize) {
			if($.isEmptyObject(this.options.resources)) {
				this.pager.find('*').unbind();
				return;
			}
			this.table.tablesorter({
				sortList: [[0,0]],
		        headers: { 
		            4: { sorter: false }
		        },
		        widgets: ['zebra']
			}).tablesorterPager({
				container: this.pager,
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
			if($.isEmptyObject(this.options.resources)) return;
			$.each(this.options.resources,$.proxy(function(i, res) {
				var listViewItem = this.element.find('#wbl-listViewContent').append('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {
					page: res, 
					runtime: this.options.runtime,
					language: this.options.language,
					resourceType: this.options.resourceType
				});
				listViewItem.find('tr.wbl-pageEntry:last').editor_resourcelistviewitem({
					page: res,
					runtime: this.options.runtime, 
					language: this.options.language,
					resourceType: this.options.resourceType,
					mode: this.options.mode
				});
			}, this));
		},
		
		_selectResources: function(selection) {
			// Find selected resources by id
			var resources = $.grep(this.element.find('#wbl-listViewContent tr.wbl-pageEntry'), $.proxy(function(elem, index) {
				return jQuery.inArray($(elem).attr('id'), selection) != -1;
			}, this));
			
			// Mark selected resources
			$(resources).find('input').attr('checked', 'checked');
		},
		
		"img.wbl-itemFavorize click": function(el, ev) {
			this.element.trigger('favorizeResources', [el.parents('tr.wbl-pageEntry')]);
		},
		
		"button.wbl-duplicate click": function(el, ev) {
			this.options.selectedResources = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			if(this.options.selectedResources.length == 1) {
				this.duplicateDialog.dialog('open');
			} else if(this.options.selectedResources.length > 1) {
				this.element.trigger('showMessage', 'Es kann nur eine Seite markiert werden.');
			} else {
				this.element.trigger('showMessage', 'Es wurde keine Seite markiert.');
			}
		},
		
		"button.wbl-delete click": function(el, ev) {
			this.options.selectedResources = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			if(this.options.selectedResources.length) {
				this.deleteDialog.dialog('open');
			} else {
				this.element.trigger('showMessage', 'Es wurde keine Seite markiert.');
			}
		},
		
		"button.wbl-favorize click": function(el, ev) {
			this.options.selectedResources = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			this.element.trigger('favorizeResources', this.options.selectedResources);
		}
		
	});

});
