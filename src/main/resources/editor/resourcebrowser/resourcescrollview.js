steal.plugins('jquery/view/tmpl','jqueryui/widget', 'jqueryui/dialog')
.views('//editor/resourcebrowser/views/resourcescrollview.tmpl')
.resources('jquery.smoothDivScroll-1.1')
.css('smoothDivScroll')
.then('resourceview', 'resourcescrollviewitem')
.then(function($) {
	
	Editor.Resourceview.extend('Editor.Resourcescrollview', 
	{
		defaults: {
			confirmDialog: null
		}
	},
	{		
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcescrollview.tmpl', {});
			this._initViewItems();
			
			$(el).find('#makeMeScrollable').smoothDivScroll({
			  	autoScroll: "onstart" , 
				autoScrollDirection: "backandforth", 
				autoScrollStep: 1, 
				autoScrollInterval: 15,	
				visibleHotSpots: "always"
		  	});
			
			this.options.confirmDialog = $('#dialog-confirm').dialog({
				width: 400,
				autoOpen: false,
				modal: true,
				resizable: false,
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
		},
		
		_initViewItems: function() {
			$.each(this.options.resources, function(i, res) {
				$('div.scrollableArea').append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {page: res});
			});
			
			$('div.scrollviewitem').editor_resourcescrollviewitem();
		},
		
		_message: function(messageText) {
			$('.message').removeClass('error').addClass('success').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
			});
			$('.message').text(messageText);
		},
		
		"button.duplicate click": function(el, ev) {
			if($('div.page.marked').length) {
//				$('#editor').dialog( "option", "title", 'Seite duplizieren' ).dialog('open').load('duplicate_page.html')
				// add code to delete the page from weblounge
				this._message('Seite dupliziert');
			} else {
				this._message('Es wurde keine Seite markiert.');
			}
		},
		
		"button.delete click": function(el, ev) {
			if($('div.page.marked').length) {
				this.options.confirmDialog.dialog('open');
			} else {
				this._message('Keine Seite ausgew&auml;hlt');
			}
		},
		
		"button.favorize click": function(el, ev) {
			if($('div.page.marked').length) {
				this._message('Zu Favoriten hinzugefügt');
			} else {
				this._message('Keine Seite ausgew&auml;hlt');
			}
		}
	});

});
