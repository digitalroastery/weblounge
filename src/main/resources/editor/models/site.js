steal.then('jsonix')
.then(function($) {
	
	$.Model('Site',
	/* @Static */
	{
		findOne: function(params, success, error) {
			$.ajax('/system/weblounge/sites/weblounge-test', {
				async: false,
				success: this.callback(['parseXML', 'wrap', success]),
			});
		},
		
		parseXML: function(xml) {
			var site = new Object();
			site.id = $(xml).attr('id');
			site.languages = [];
			
			$(xml).find('language').each(function(index) {
				site.languages[index] = {};
				site.languages[index].language = $(this).text();
				site.languages[index]._default = ($(this).attr('default') == "true");
			});
			
			site.domains = [];
			$(xml).find('url').each(function(index) {
				site.domains[index] = {};
				site.domains[index].url = $(this).text();
				site.domains[index]._default = ($(this).attr('default') == "true");
			});
			return site;
		}
		
	},
	/* @Prototype */
	{
	    getLanguages: function() {
	    	return this.languages;
	    },
	    
	    getId: function() {
	    	return this.id;
	    },
	    
	    getDefaultLanguage: function() {
	    	var language; 
	    	$.each(this.languages, function(index, lang) { 
	    		if(lang._default) {
	    			language = lang.language;
	    			return false;
	    		}
	    	});
	    	return language;
	    }
	    
	});

});
