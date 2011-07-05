steal.then('jsonix')
.then(function($) {
	
	$.Model('Runtime',
	/* @Static */
	{
		findOne: function(params, success, error) {
			$.ajax('/system/weblounge/runtime/', {
				async: false,
				success: this.callback(['parseXML', 'wrap', success]),
			});
		},
		
		parseXML: function(xml) {
			var site = new Object();
			$(xml).find('site').each(function(index) {
				site.id = $(this).attr('id');
				site.languages = [];
				
				$(this).find('language').each(function(index) {
					site.languages[index] = {};
					site.languages[index].language = $(this).text();
					site.languages[index]._default = ($(this).attr('default') == "true");
				});
				
				site.domains = [];
				$(this).find('url').each(function(index) {
					site.domains[index] = {};
					site.domains[index].url = $(this).text();
					site.domains[index]._default = ($(this).attr('default') == "true");
				});
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
