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
			selectedPages: {}
		}
	},
	{	
		init: function(el) {
			
		},
		
		_initButtons: function() {
			$('nav.weblounge div.icons').buttonset();
			$('button.delete').button({icons: {primary: "ui-icon-trash"}, text: false});
			$('button.duplicate').button({icons: {primary: "ui-icon-copy"}, text: false});
			$('button.favorize').button({icons: {primary: "ui-icon-star"}, text: false });
		},
		
		_initDialogs: function() {
			this.deleteDialog = $('<div></div>')
			.load('weblounge/editor/resourcebrowser/views/delete-dialog.html')
			.dialog({
				modal: true,
				title: 'Seite(n) löschen',
				autoOpen: false,
				resizable: true,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
						this.element.trigger('deletePages', [this.options.selectedPages]);
						this._showMessage('Seite(n) gelöscht!');
						this.deleteDialog.dialog('close');
					},this)
				},
			});

			this.duplicateDialog = $('<div></div>')
			.load('weblounge/editor/resourcebrowser/views/duplicate-dialog.html')
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
						this.element.trigger('duplicatePages', [this.options.selectedPages]);
						this._showMessage('Seite dupliziert!');
						this.duplicateDialog.dialog('close');
					},this)
				}
			});
		},
		
		_showMessage: function(messageText) {
			$('.message').removeClass('error').addClass('success').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
			});
			$('.message').text(messageText);
		}
		
	});

});
