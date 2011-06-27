steal.then('jsonix')
.then(function($) {
	
	$.Model('Site',
	/* @Static */
	{
		findOne: function(params, success, error) {
			$.ajax('/system/weblounge/sites/', {
				async: false,
				success: this.callback(['parseXML', 'wrap', success]),
			});
		},
		
		parseXML: function(xml) {
			var site = new Object();
			$(xml).find('#weblounge-test').each(function(index) {
				site.languages = [];
				
				$(this).find('language').each(function(index) {
					site.languages[index] = {};
					site.languages[index].language = $(this).text();
					site.languages[index].default = ($(this).attr('default') == "true");
				});
				
				site.domains = [];
				$(this).find('url').each(function(index) {
					site.domains[index] = {};
					site.domains[index].url = $(this).text();
					site.domains[index].default = ($(this).attr('default') == "true");
				});
				return false;
			});
			return site;
		}
		
	},
	/* @Prototype */
	{
	    getLanguages: function() {
	    	return this.languages;
	    },
	    
	    getDefaultLanguage: function() {
	    	var language; 
	    	$.each(this.languages, function(index, lang) { 
	    		if(lang.default == "true") {
	    			language = lang;
	    			return false;
	    		}
	    	});
	    	return language;
	    }
	    
	});

});
