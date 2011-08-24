$('div#wbl-linkEditor div.wbl-linkInternal, div#wbl-linkEditor p.wbl-linkExternal').hide();

$('div#wbl-linkEditor select#wbl-linkType').change(function() {
	var type = $(this).val();
	$('div.wbl-linkInternal, p.wbl-linkExternal').hide();
	if(type == 'external') {
		$('p.wbl-linkExternal').show();
	} else if(type == 'internal') {
		$('div.wbl-linkInternal').show();
	}
});

var inputLinkId = $('input#wbl-linkInternal');

$('div#wbl-linkEditor input#wbl-linkInternalSearch').keypress(function(ev) {
	if(ev.key() == '\r') {
		ev.preventDefault();
		$('tbody.wbl-linkSearchResult').empty();
		Page.findBySearch({search: $(this).val()}, function(pages) {
			if(pages == undefined || pages.length < 1) {
				$('tbody.wbl-linkSearchResult').html('No pages found!');
			} else {
				$.each(pages, function(index, page) {
					$('tbody.wbl-linkSearchResult').append('<tr id="' + page.id + '"><td>' + page.path + '</td></tr>');
//							'<td>' + page.getTitle('de') + '</td>' +
//							'<td>' + page. + '</td>' + 
//							'<td>' + page. + '</td></tr>');
				});
				
				$('tbody.wbl-linkSearchResult tr').click(function() {
				    $('tbody.wbl-linkSearchResult tr.wbl-linkMarked').removeClass();
				    $(this).addClass('wbl-linkMarked');
				    inputLinkId.val($(this).attr('id'));
				});
				
				if(inputLinkId.val() != '') {
					$('tbody.wbl-linkSearchResult tr#' + inputLinkId.val()).click();
				}
			}
		});
	}
});

//$('div#wbl-linkEditor input#wbl-linkExternal').change(function() {
//// URL mit Linkchecker Rot Grün
//});

$('div#wbl-linkEditor select#wbl-linkType').change();

var search = $('div#wbl-linkEditor input#wbl-linkInternalSearch').val();
if(search != '') {
	var keyEnterEvent = jQuery.Event("keypress");
	keyEnterEvent.which = 13; //choose the one you want
	keyEnterEvent.keyCode = 13;
	$('div#wbl-linkEditor input#wbl-linkInternalSearch').trigger(keyEnterEvent);
}