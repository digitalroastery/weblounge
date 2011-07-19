steal.then('jsonix')
.then(function($) {
	
	$.Model('Site',
	/* @Static */
	{
		findOne: function(params, success, error) {
			$.ajax('/system/weblounge/sites/' + params.id, {
				success: this.callback(['parseXML', 'wrap', success])
			});
		},
		
		getModules: function(params, success, error) {
			$.ajax('/system/weblounge/sites/' + params.id + '/modules', {
				success: function(xml) {
					success(Site.parseModules(xml));
				}
			});
		},
		
		getModule: function(params, success, error) {
			$.ajax('/system/weblounge/sites/' + params.id + '/modules/' + params.module, {
				success: function(xml) {
					var pagelets = Site.parseModule(xml);
					if(params.composeable == true) {
						pagelets = $.grep(pagelets, function(pagelet, index){
							return pagelet.composeable;
						});
					}
					success(pagelets);
				}
			});
		},
		
		parseModules: function(xml) {
			var modules = new Array();
			$(xml).find('module').each(function(index) {
				modules[index] = {};
				modules[index].id = $(this).attr('id');
				modules[index].enable =($(this).find('enable:first').text() == "true");
				modules[index].name = $(this).find('name:first').text();
			});
			return modules;
		},
		
		parseModule: function(xml) {
			var pagelets = new Array();
			$(xml).find('pagelet').each(function(index) {
				pagelets[index] = {};
				pagelets[index].id = $(this).attr('id');
				pagelets[index].composeable = ($(this).attr('composeable') == "true");
				
				pagelets[index].renderer = new Array();
				var renderer = pagelets[index].renderer;
				$(this).find('renderer').each(function(index) {
					renderer[index] = {};
					renderer[index].type = $(this).attr('type');
					renderer[index].value = $(this).text();
				});
				
				pagelets[index].editor = $(this).find('editor:first').text();
				pagelets[index].recheck = $(this).find('recheck:first').text();
				pagelets[index].valid = $(this).find('valid:first').text();
				pagelets[index].preview = $(this).find('preview:first').text();
			});
			return pagelets;
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
