steal.plugins('jquery/controller',
		'jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.then(function($) {

	/*
	 *
	 */
	$.Controller('Editor.Resourceview',
	{
		defaults: {
			resources: {},
			resourceType: 'pages',
			selectedResources: {}
		}
	},
	{	
		init: function(el) {

		},
		
		_initButtons: function() {
			$('nav.weblounge div.wbl-icons').buttonset();
			$('button.wbl-delete').button({icons: {primary: "ui-icon-trash"}, text: false});
			$('button.wbl-duplicate').button({icons: {primary: "ui-icon-copy"}, text: false});
			$('button.wbl-favorize').button({icons: {primary: "ui-icon-star"}, text: false });
		},
		
		_initFilter: function() {
			this.element.find("input#wbl-filter").keydown($.proxy(function(ev) {
				if(ev.key() == '\r') {
					ev.preventDefault();
					clearTimeout(this.timeout);
					$(ev.target).trigger('filterResources', ev.target.value);
				} else {
					clearTimeout(this.timeout);
					this.timeout = setTimeout(function() {
						$(ev.target).trigger('filterResources', ev.target.value);
					}, 700);
				}
			}, this));
			
			
		},
		
		_initDialogs: function() {
			this.deleteDialog = $('<div></div>')
			.load(this.options.runtime.getRootPath() + '/editor/resourcebrowser/views/delete-dialog.html')
			.dialog({
				modal: true,
				title: 'Seite(n) l&ouml;schen',
				autoOpen: false,
				resizable: true,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
						this.element.trigger('deleteResources', [this.options.selectedResources]);
						this.deleteDialog.dialog('close');
					},this)
				}
			});

			this.duplicateDialog = $('<div></div>')
			.load(this.options.runtime.getRootPath() + '/editor/resourcebrowser/views/duplicate-dialog.html')
			.dialog({
				modal: true,
				title: 'Seite duplizieren',
				autoOpen: false,
				resizable: true,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
						this.element.trigger('duplicateResources', [this.options.selectedResources]);
						this.duplicateDialog.dialog('close');
					},this)
				}
			});
		},
		
		"img deleteResource": function(el, ev, resource) {
			this.options.selectedResources = resource;
			this.deleteDialog.dialog('open');
		}
		
	});

});
