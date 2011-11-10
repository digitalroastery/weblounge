steal.then('jsonix')
.then(function($) {
	
	$.Model('Runtime',
	/* @Static */
	{
		findOne: function(params, success, error) {
			$.ajax('/system/weblounge/runtime/', {
				async: false,
				success: this.callback(['parseXML', 'wrap', success])
			});
		},
		
		parseXML: function(xml) {
			var runtime = new Object();
			runtime.path = $(xml).find('ui path:first').text();
			runtime.user = new Object();
			var userElement = $(xml).find('security user:first');
			runtime.user.login = userElement.attr('id');
			runtime.user.realm = userElement.attr('realm');
			runtime.user.name = userElement.find('name').text();
			runtime.user.email = userElement.find('email').text();
			runtime.site = new Object();
			runtime.roles = new Array();
			$(xml).find('security roles').each(function(index) {
				var role = $(this).find('role');
				runtime.roles.push({
					id: role.attr('id'),
					context: role.attr('context'),
					name: role.find('name').text()
				});
			});
			$(xml).find('site').each(function(index) {
				runtime.site.id = $(this).find('id:first').text();
				
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
				
				runtime.site.templates = [];
				$(this).find('template').each(function(index) {
					runtime.site.templates[index] = {};
					runtime.site.templates[index].id = $(this).attr('id');
					runtime.site.templates[index].composeable = ($(this).attr('composeable') == "true");
					runtime.site.templates[index]._default = ($(this).attr('default') == "true");
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
	    
	    isSystemAdmin: function() {
	    	var isAdmin = false;
	    	$.each(this.roles, function(index, role) {
	    		if(role.id == 'systemadmin' && role.context == 'weblounge') {
	    			isAdmin = true;
	    			return false;
	    		}
	    	});
	    	return isAdmin;
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
	    
	    getSiteLayouts: function() {
	    	var layouts = new Array();
	    	$.each(this.site.templates, function(index, template){
    		  if(template.composeable) layouts.push(template);
    		});
	    	return layouts;
	    },
	    
		getSiteModules: function(success) {
			Site.getModules({id: this.getId()}, success);
		},
		
		getModulePagelets: function(module, success) {
			Site.getModule({id: this.getId(), module: module, composeable: true}, success);
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
