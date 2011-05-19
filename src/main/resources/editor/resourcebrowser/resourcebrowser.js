steal.plugins().then(function($) {

  $.Controller('Editor.Resourcebrowser'), 
	{
		defaults: {
			view: 'scrollview'
		}
	},
	
	{
		
		init: function(el) {
			$(el).html(this.view());
		},
		
		changeView: function() {
			
		},
		
		loadRecent: function() {
			
		},
		
		loadFavorites: function() {
			
		},
		
		loadPending: function() {
			
		},
		
		loadAll: function() {
			
		}
		
	});

});
