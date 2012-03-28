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
			selectedResources: {},
			mode: 'normal'
		}
	},
	{	
		update: function(options) {
			if(!$.isEmptyObject(options.mode)) {
				this.options.mode = options.mode
			}
			this.options.resources = options.resources;
			if(this.options.mode == 'normal') {
				this.element.find('nav.wbl-icons button').show();
			} else {
				this.element.find('nav.wbl-icons button').hide();
			}
		},
		
		_initButtons: function() {
			$('nav.weblounge div.wbl-icons').buttonset();
			$('button.wbl-delete').button({icons: {primary: "ui-icon-trash"}, text: false});
			$('button.wbl-duplicate').button({icons: {primary: "ui-icon-copy"}, text: false});
			$('button.wbl-favorize').button({icons: {primary: "ui-icon-star"}, text: false });
			$('button.wbl-delete').button("disable");
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
			var deleteDialogTitle;
			var deleteDialogText;
			if(this.options.resourceType == 'pages') {
				deleteDialogTitle = 'Seite(n) l&ouml;schen';
				deleteDialogText = '<p>M&ouml;chten Sie diese Seite(n) wirklich l&ouml;schen?</p>';
			} else {
				deleteDialogTitle = 'Media l&ouml;schen';
				deleteDialogText = '<p>M&ouml;chten Sie diese Media wirklich l&ouml;schen?</p>';
			}
			
			this.deleteDialog = $('<div></div>')
			.html(deleteDialogText)
			.dialog({
				modal: true,
				title: deleteDialogTitle,
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
		},
		
		"div,tr updateResource": function(el, ev, resource) {
			ev.stopPropagation();
			var index = -1;
			$.each(this.options.resources, function(i, elem) {
				if($(this).attr('id') == resource.id) {
					index = i;
					return false;
				}
			})
			if(index == -1) return;
			this.options.resources.splice(index, 1, resource)
		}
		
	});

});
