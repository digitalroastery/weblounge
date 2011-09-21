function AppleOverlay(imageId){
	$(window).load(function() {
		$('img[rel="#photo' + imageId + '"]').overlay({
			effect: 'default',
			top: 50
		});
	});
}