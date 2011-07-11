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
			var runtime = new Object();
			runtime.path = $(xml).find('ui path')[0].textContent;
			runtime.user = new Object();
			runtime.user.login = $(xml).find('user login')[0].textContent;
			runtime.user.name = $(xml).find('user name')[0].textContent;
			runtime.user.email = $(xml).find('user email')[0].textContent;
			runtime.site = new Object();
			$(xml).find('site').each(function(index) {
				runtime.site.id = $(this).find('id')[0].textContent;
				runtime.site.languages = [];
				
				$(this).find('language').each(function(index) {
					runtime.site.languages[index] = {};
					runtime.site.languages[index].language = $(this).text();
					runtime.site.languages[index]._default = ($(this).attr('default') == "true");
				});
				
				runtime.site.domains = [];
				$(this).find('url').each(function(index) {
					runtime.site.domains[index] = {};
					runtime.site.domains[index].url = $(this).text();
					runtime.site.domains[index]._default = ($(this).attr('default') == "true");
				});
			});
			return runtime;
		}
		
	},
	/* @Prototype */
	{
	    getLanguages: function() {
	    	return this.site.languages;
	    },
	    
	    getUserName: function() {
	    	return this.user.name;
	    },
	    
	    getUserLogin: function() {
	    	return this.user.login;
	    },
	    
	    getUserEmail: function() {
	    	return this.user.email;
	    },
	    
	    getId: function() {
	    	return this.site.id;
	    },
	    
	    getRootPath: function() {
	    	return this.path;
	    },
	    
	    getDefaultLanguage: function() {
	    	var language; 
	    	$.each(this.site.languages, function(index, lang) { 
	    		if(lang._default) {
	    			language = lang.language;
	    			return false;
	    		}
	    	});
	    	return language;
	    }
	    
	});

});
