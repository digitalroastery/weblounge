steal.plugins('jquery/controller').then(function($) {

	/*
	 *
	 */
	$.Controller('Editor.Resourceview',
	{
		defaults: {
			resources: {},
			resourceType: 'pages',
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
			.html('Seite(n) löschen?')
			.dialog({
				modal: true,
				autoOpen: false,
				resizable: false,
				title: 'Seite(n) löschen',
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: function() {
						$(this).dialog('close');
					}
				},
				close: function(event, ui) {
					// Delete Pages
				}
			});
			this.duplicateDialog = $('<div></div>')
			.html('Seite(n) duplizieren!')
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
						$(this).dialog('close');
					}
				},
				close: function(event, ui) {
					// Duplicate Pages
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
