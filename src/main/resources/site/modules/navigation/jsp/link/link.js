$(window).load(function(){
	$('p.internal, p.external, p.file').hide();
	
	$('select#type').change(function() {
		var type = $('#type option:selected').val();
		$('p.internal, p.external, p.file').hide();
		if(type=='external') {
			$('p.external').show();
		} else if(type=='internal') {
			$('p.internal').show();
		} else if(type=='file') {
			$('p.file').show();
		}
	});
});


