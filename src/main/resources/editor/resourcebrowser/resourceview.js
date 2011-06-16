steal.plugins('jquery/controller').then(function($) {

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
			this.deleteDialog = $('#dialog-delete')
			.dialog({
				modal: true,
				autoOpen: false,
				resizable: false,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: function() {
						$(this).trigger('deletePages', this.options.selectedPages);
						$(this).dialog('close');
					}
				},
			});
			
			this.duplicateDialog = $('#dialog-duplicate')
			.dialog({
				modal: true,
				autoOpen: false,
				resizable: false,
				title: 'Seite(n) duplizieren',
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: function() {
						$(this).trigger('duplicatePages', this.options.selectedPages);
						$(this).dialog('close');
					},
				}
			});
		},
		
		_showMessage: function(messageText) {
			$('.message').removeClass('error').addClass('success').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
			});
			$('.message').text(messageText);
		},
		
		'drawer.open subscribe': function() {
			steal.dev.log('hallo Welt');
		}
		
	});

});
