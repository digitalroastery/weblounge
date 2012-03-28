function AppleOverlay(imageId) {
	$(window).load(function() {
		$('img[rel="#photo' + imageId + '"]').overlay({
			effect: 'default',
			top: 50,
			onBeforeLoad: function() {
				var maxWidth = $(window).width() * 0.8;
				var maxHeight = $(window).height() * 0.8;
				
				var image = this.getOverlay().find('img');
				image.css('max-width', maxWidth + 'px');
				image.css('max-height', (maxHeight - 50) + 'px');
			}
		});
	});
}