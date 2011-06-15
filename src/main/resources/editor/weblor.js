$(document).ready(function() {

	// trigger media-view (empty or not)
	if($('p.switch_view input').is(':checked')) {
		$('#empty_view').show();
	} else {
		$('#empty_view').hide();
	}
	$('p.switch_view input').click(function() {
		if($(this).is(':checked')) {
			$('#empty_view').show();
		} else {
			$('#empty_view').hide();
		}
	});
	
	// hide all menus
	$('body').click(function() {
		$('.menu').hide();
	});
	
	// trigger editmode
	$('input#editmode').click( function() {
		// enable editmode
		if ($(this).is(':checked')) {
			log('editmode is enabled')
		// disable editmode
		} else {
			log('editmode is disabled')
			$('#editor').dialog({title: 'Seite publizieren', buttons: {
				Ja: function() {
					$(this).dialog('close');
					log('weiter');
				},
				Nein: function() {
					$(this).dialog('close');
					log('abbrechen');
				}
				
			}} ).dialog('open').load('publish_page.html')
		}
	});

	$.datepicker.regional['de'] = {clearText: 'löschen', clearStatus: 'aktuelles Datum löschen',
	    closeText: 'schließen', closeStatus: 'ohne Änderungen schließen',
	    prevText: '&#x3c;zurück', prevStatus: 'letzten Monat zeigen',
	    nextText: 'Vor&#x3e;', nextStatus: 'nächsten Monat zeigen',
	    currentText: 'heute', currentStatus: '',
	    monthNames: ['Januar','Februar','März','April','Mai','Juni',
	    'Juli','August','September','Oktober','November','Dezember'],
	    monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
	    'Jul','Aug','Sep','Okt','Nov','Dez'],
	    monthStatus: 'anderen Monat anzeigen', yearStatus: 'anderes Jahr anzeigen',
	    weekHeader: 'Wo', weekStatus: 'Woche des Monats',
	    dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
	    dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
	    dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
	    dayStatus: 'Setze DD als ersten Wochentag', dateStatus: 'Wähle D, M d',
	    dateFormat: 'dd.mm.yy', firstDay: 1, 
	    initStatus: 'Wähle ein Datum', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['de']);
	$.timepicker.regional['de'] = {
	  timeOnlyTitle: 'Uhrzeit auswählen',
	  timeText: 'Zeit',
	  hourText: 'Stunde',
	  minuteText: 'Minute',
	  secondText: 'Sekunde',
	  currentText: 'Jetzt',
	  closeText: 'Auswählen',
	  ampm: false
	};
	$.timepicker.setDefaults($.timepicker.regional['de']);
	$('input.datepicker').datetimepicker();
	
	// autocomplete keywords
	$('input.keywords').tokenInput([
		{id:1,name:"HNLA"},
		{id:2,name:"Nati"}
		], {
	    hintText: "Type in the keyword(s)",
	    noResultsText: "No results",
	    searchingText: "Searching...",
	    theme: "facebook"
	});
	
	// autocomplete user-access
	$('input.user_access').tokenInput([
		{id:1,name:"Simon"},
		{id:2,name:"Basil"}
		], {
	    hintText: "Type in the names of the user(s)",
	    noResultsText: "No results",
	    searchingText: "Searching...",
	    theme: "facebook"
	});
	  	  	
	// Buttons
	$('nav.weblounge div.view').buttonset();
	$('nav.weblounge div.filter').buttonset();
	$('nav.weblounge div.icons').buttonset();
	$('nav.weblounge button').button('option', 'disabled', false);
	$('nav.weblounge button.list').button({
		icons: {primary: "icon-list"},
		text: false }).click(function() { 
			log('switch to list view');
			$('div.listview').show();
			$('div.treeview').hide();
			$('div.thumbnailview').hide();
	});
	$('button.tree').button({
		icons: {
			primary: "icon-tree"
		},
		disabled: true,
		text: false }).click(function() { 
			log('switch to tree view') 
			$('div.treeview').show();
			$('div.listview').hide();
			$('div.thumbnailview').hide();
	});
	$('nav.weblounge button.thumbnails').button({
		disabled: false,
		icons: {primary: "icon-thumbnails"},
		text: false }).click(function() { 
			log('switch to thumbnail view');
			$('div.treeview').hide();
			$('div.listview').hide();
			$('div.thumbnailview').show();
	});
	
// ------------
// page actions
// ------------

// add page
function addPage() {
	log('Neue Seite hinzufügen');
	$('#editor').dialog( "option", "title", 'Neue Seite erstellen' ).dialog('open').load('add_page.html');
	// add code to add a new page
	$('.message').text('Neue Seite erstellt')
}

// edit page setting
function pageSetting(pageID) {
	$('#editor').dialog( "option", "title", 'Seiteneingeschaften' ).dialog('open').load('pageinfo.html');

}


// delete page
function deletePage(pageID) {
	if($('div.listview table input:checked').length) {
		// onlyif page is linked
		$('#editor').dialog( "option", "title", 'Seite löschen' ).dialog('open').load('delete_page.html')
		// in every case after the dialog is closed and not escaped
		$('div.listview table input:checked').parentsUntil('tbody').fadeOut(1500).delay(1500).queue(function() {
			$(this).remove();
		});
		$('.message').text('Seite(n) gelöscht').removeClass('error').addClass('success').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
		});
		// add code to delete the page from weblounge
		
	} else if(pageID != "") {
		// onlyif page is linked
		$('#editor').dialog( "option", "title", 'Seite löschen' ).dialog('open').load('delete_page.html')
		// in every case after the dialog is closed and not escaped
		
		// remove the deleted page from the list: search the with the given pageID and remove it!
		
		$('.message').text('Seite(n) gelöscht').removeClass('error').addClass('success').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
		});
	
	} else {
		log('Keine Seite markiert')
		$('.message').text('Es wurde keine Seite markiert.').removeClass('success').addClass('error').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
		});
	}
}

// duplicate page
function duplicatePage() {
	if($('div.listview table input:checked').length) {
		$('#editor').dialog( "option", "title", 'Seite duplizieren' ).dialog('open').load('duplicate_page.html')
		// add code to delete the page from weblounge
	} else {
		log('Keine Seite markiert')
		$('.message').text('Es wurde keine Seite markiert.');
	}
	$('.message').text('Seite dupliziert');
}
