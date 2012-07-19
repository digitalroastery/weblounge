steal.then('jsonix')
.then(function($) {
	
	$.Model('Page',
	/* @Static */
	{
		/**
		 * Get Page from Path or Id
		 * @param {Object} params path or id
		 */
		findOne: function(params, success, error) {
			// work version is default
			var version = 1;
			if ('version' in params) {
				version = params.version;
			}
			
			if ('path' in params) {
				$.ajax('/system/weblounge/pages?details=true&path=' + params.path + '&version=' + version, {
					success: $.proxy(function(pageXML) {
						var liveJson = Page.parseXMLPage(pageXML);
						if(liveJson == null) {
							Page.findOne({path: params.path, version: 0}, success);
						} else {
							var livePage = this.wrap(liveJson);
							success(livePage);
						}
					}, this),
					error: function() {
						Page.findOne({path: params.path, version: 0}, success);
					}
				});
			} 
			else if ('id' in params) {
				$.ajax('/system/weblounge/pages/' + params.id + '?version=' + version, {
					success: this.callback(['parseXMLPage','wrap', success]),
					error: function() {
						Page.findOne({id: params.id, version: 0}, success);
					}
				});
			}
		},
		
		/**
		 * Get all Pages
		 */
		findAll: function(params, success, error) {
			var url = "/system/weblounge/pages/?sort=created-asc&limit=0&offset=0";
			if ('version' in params) {
				url += "&version=" + params.version;
			}
			if ('preferredversion' in params) {
				url += "&preferredversion=" + params.preferredversion;
			}
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Page.parseXML(xml);
					success(json.value.page);
				},
				error: error
			});
		},
		
		/**
		 * Get Recent Pages
		 */
		findRecent: function(params, success, error) {
			var url = "/system/weblounge/pages/?sort=modified-desc&limit=8&offset=0";
			if ('version' in params) {
				url += "&version=" + params.version;
			}
			if ('preferredversion' in params) {
				url += "&preferredversion=" + params.preferredversion;
			}
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Page.parseXML(xml);
					success(json.value.page);
				},
				error: error
			});
		},
		
		/**
		 * Get Pending Pages
		 */
		findPending: function(params, success, error) {
			var url = "/system/weblounge/pages/pending?limit=0";
			if ('filter' in params) {
				url += "?filter=" + params.filter;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Page.parseXML(xml);
					success(json.value.page);
				},
				error: error
			});
		},
		
		/**
		 * Get pages searched by string
		 */
		findBySearch: function(params, success, error) {
			var url = '/system/weblounge/pages/?searchterms=' + params.search + '&sort=modified-desc&limit=8&offset=0';
			if ('version' in params) {
				url += "&version=" + params.version;
			}
			if ('preferredversion' in params) {
				url += "&preferredversion=" + params.preferredversion;
			}
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Page.parseXML(xml);
					success(json.value.page);
				},
				error: error
			});
		},
		
		/**
		 * Returns pages containing references to the page with the given id
		 */
		findReferrer: function(params, success, error) {
			if ('id' in params) {
				var url = '/system/weblounge/pages/' + params.id + '/referrer';
				$.ajax(url, {
					success: function(xml) {
						var json = Page.parseXML(xml);
						success(json.value.page);
					},
					error: error
				});
			}
		},
		
		/**
		 * Updates the specified page.
		 * @param {Object} params The page identifier and eTag
		 * @param {Page} page The page object
		 */
		update: function(params, page, success, error){
			var headers = {};
			if('eTag' in params) 
				headers = {"If-Match": params.eTag};
			
			if ('id' in params) {
				var asynchron = false;
				if('async' in params)
					asynchron = params.async;
				$.ajax({
					url: '/system/weblounge/pages/' + params.id,
					async: asynchron,
					type: 'put',
					success: success,
					error: error,
					headers: headers,
					dataType: 'xml',
					data: {content : Page.parseJSON(page)}
				});
			}	
		},
		
		/**
		 * Creates a new page, either at the given path or at a random location and returns the REST url of the created resource.
		 * @param {Object} params The target path and optionally the page content
		 */
		create: function(params, success, error){
			if ('path' in params) {
				var data = {path : params.path};
				if('content' in params)
					data = {path : params.path, content : Page.parseJSON(params.content)}
				var asynchron = false;
				if('async' in params)
					asynchron = params.async;
				$.ajax({
					url: '/system/weblounge/pages/',
					async: asynchron,
					type: 'post',
					dataType: 'xml',
					data: data,
					success: function(data, status, xhr){
						var url = xhr.getResponseHeader('Location');
						Page.findOne({id : url.substring(url.lastIndexOf('/') + 1)}, success);
					},
					error: error
				});
			}	
		},
		
		/**
		 * Deletes the specified page.
		 * @param {Object} params The page identifier 
		 */
		destroy: function(params, success, error){
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id,
					type: 'delete',
					success: success,
					error: error
				});
			}
		},
		
		/**
		 * Locks the specified page.
		 *  @param {Object} params The page identifier, user (optional) and eTag (optional)
		 */
		lock: function(params, success, error) {
			var headers = {};
			var data = {user: ''};
			if('eTag' in params) 
				headers = {"If-Match": params.eTag};
			if('user' in params)
				data.user = params.user;
			
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id + '/lock',
					type: 'put',
					success: success,
					error: error,
					headers: headers,
					dataType: 'xml',
					data: data
				});
			}
		},
		
		/**
		 * Unlocks the specified page.
		 *  @param {Object} params The page identifier
		 */
		unlock: function(params, success, error) {
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id + '/lock',
					type: 'delete',
					success: success,
					error: error
				});
			}
		},
		
		/**
		 * Publish the specified page.
		 */
		publish: function(params, success, error) {
			var headers = {};
			var data = {startdate: '', enddate: ''};
			if('eTag' in params) 
				headers = {"If-Match": params.eTag};
			if('startdate' in params)
				data.startdate = params.startdate;
			if('enddate' in params)
				data.enddate = params.enddate;
			
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id + '/publish',
					type: 'put',
					success: success,
					error: error,
					headers: headers,
					dataType: 'xml',
					data: data
				});
			}
		},
		
		/**
		 * Unpublish the specified page.
		 */
		unpublish: function(params, success, error) {
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id + '/publish',
					type: 'delete',
					success: success,
					error: error
				});
			}
		},
		
		/**
		 * Converts XML to JSON
		 */
		parseXML: function(xml) {
			var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
			return unmarshaller.unmarshalDocument(xml);
		},
		
		/**
		 * Converts XML to JSON
		 */
		parseXMLPage: function(xml) {
			var page = $(xml).find('page')[0];
			if(page == undefined) return null;
			var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
			return unmarshaller.unmarshalDocument(page);
		},
		
		/**
		 * Converts JSON to XML
		 */
		parseJSON: function(json) {
			var marshaller = Editor.Jsonix.context().createMarshaller();
			return marshaller.marshalString(json);
		}

	},
	/* @Prototype */
	{
		/**
		 * Return the specified Composer
		 * @param {String} composerId
		 */
	    getComposer: function(id) {
	    	var composer;
	    	$.each(this.value.body.composers, function(i, comp) {
	    		if(comp.id == id) {
	    			composer = comp;
	    			return false;
	    		}
    		});
	    	return composer;
	    },
	    
	    /**
	     * Check if the page is locked
	     */
	    isLocked: function() {
	    	if($.isEmptyObject(this.value.head.locked)) return false;
	    	return true;
	    },
	    
	    /**
	     * Publish this page
	     */
	    publish: function(success, error) {
	    	Page.publish({id:this.value.id}, success, error);
	    },
	    
	    /**
	     * Publish this page
	     */
	    unpublish: function(success, error) {
	    	Page.unpublish({id:this.value.id}, success, error);
	    },
	    
	    /**
	     * Check if the user has locked the page
	     * @param {String} userId
	     */
	    isLockedUser: function(userId) {
	    	if($.isEmptyObject(this.value.head.locked)) return false;
	    	if(this.value.head.locked.user.id == userId) return true;
	    	return false;
	    },
	    
	    /**
	     * Lock this page
	     */
	    lock: function(user, success, error) {
	    	Page.lock({id:this.value.id, user: user}, $.proxy(function() {
	    		Page.findOne({id: this.value.id}, $.proxy(function(page) {
					this.value = page.value;
					success();
				}, this));
	    	}, this), error);
	    },
	    
	    /**
	     * Unlock this page
	     */
	    unlock: function(success) {
	    	Page.unlock({id:this.value.id}, $.proxy(function() {
	    		delete this.value.head.locked;
	    		success();
	    	}, this));
	    },
	    
	    /**
	     * Return the lock owner's user name
	     */
	    getLockOwner: function() {
	    	if($.isEmptyObject(this.value.head.locked)) return;
	    	if(!$.isEmptyObject(this.value.head.locked.user.name))
	    		return this.value.head.locked.user.name;
    		return this.value.head.locked.user.id;
	    },
	    
	    /**
	     * Return the page path
	     */
	    getPath: function() {
	    	return this.value.path;
	    },
	    
	    /**
	     * Return the page template
	     */
	    getTemplate: function() {
	    	return this.value.head.template;
	    },
	    
	    /**
	     * Return the page version
	     */
	    getVersion: function() {
	    	return this.value.version;
	    },
	    
	    /**
	     * Return true if this page is the work version
	     */
	    isWorkVersion: function() {
	    	return this.value.version == 'work';
	    },
	    
	    /**
	     * Return the page title
	     */
	    getTitle: function(language) {
	    	if($.isEmptyObject(this.value.head.metadata)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.title)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.title[language])) return '';
	    	return this.value.head.metadata.title[language];
	    },
	    
	    /**
	     * Return the page description
	     */
	    getDescription: function(language) {
	    	if($.isEmptyObject(this.value.head.metadata)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.description)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.description[language])) return '';
	    	return this.value.head.metadata.description[language];
	    },
	    
	    /**
	     * Return the page tags
	     */
	    getTags: function() {
	    	if($.isEmptyObject(this.value.head.metadata)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.subject)) return '';
	    	return this.value.head.metadata.subject;
	    },
	    
	    /**
	     * Return the specified Composer Index
	     * @param {String} composerId
	     */
	    getComposerIndex: function(id) {
	    	var index = -1;
	    	$.each(this.value.body.composers, function(i, composer) {
	    		if(composer.id == id) {
	    			index = i;
	    			return false;
	    		};
    		});
	    	return index;
	    },
	    
	    /**
	     * Delete a pagelet
	     */
	    deletePagelet: function(composerId, index){
	    	var composer = this.getComposer(composerId);
	    	composer.pagelets.splice(index, 1);
	    	Page.update({id:this.value.id}, this);
	    },
 	    
	    /**
	     * Return the specified Pagelet
	     * @param {String} composerId
	     * @param {int} index
	     */
	    getPagelet: function(composerId, index) {
	    	var composer = this.getComposer(composerId);
	    	return composer.pagelets[index];
	    },
	    
	    /**
	     * Create an empty composer element
	     * @param {String} composerId Composer to create
	     */
	    createComposer: function(composerId) {
	    	if($.isEmptyObject(this.value.body.composers)) {
	    		this.value.body.composers = new Array();
	    	}
	    	if($.isEmptyObject(this.value.body.composers.pagelets)) {
	    		this.value.body.composers.pagelets = new Array();
	    	}
	    	
	    	if(this.getComposerIndex(composerId) != -1) return;
	    	this.value.body.composers.push({id: composerId});
	    },
	    
	    /**
	     * Changes current Composer with new one and Update in Repository
	     * @param {String} composerId Composer to change
	     * @param {Object} newComposer New Composer Object
	     */
	    updateComposer: function(composerId, newComposer) {
	    	var index = this.getComposerIndex(composerId);
	    	this.value.body.composers[index].pagelets = newComposer;
	    	Page.update({id:this.value.id}, this);
	    },
	    
	    /**
	     * Return Pagelet for Pagelet-Editor with current and original Language
	     * @param {String} composerId
	     * @param {int} index
	     * @param {String} language
	     */
	    getEditorPagelet: function(composerId, index, language) {
	    	var pagelet = this.getPagelet(composerId, index);
	    	var copyPagelet = jQuery.extend(true, {}, pagelet);

	    	if($.isEmptyObject(copyPagelet.locale)) return copyPagelet;
			$.each(copyPagelet.locale, function(i, locale) {
				if(locale.language == language) {
					copyPagelet.locale.current = locale;
				}
				if(locale.original == true) {
					copyPagelet.locale.original = locale;
				}
			});
			return copyPagelet;
	    },
	    
	    /**
	     * Insert Pagelet to the specified position in the composer, this removes the current pagelet at that position
	     * @param {Object} pagelet The pagelet to insert
	     * @param {String} composerId The parent composer id 
	     * @param {int} index The pagelet index to insert
	     */
	    insertPagelet: function(pagelet, composerId, index, success) {
	    	delete pagelet.locale.current;
	    	delete pagelet.locale.original;
	    	this.value.body.composers[this.getComposerIndex(composerId)].pagelets[index] = pagelet;
	    	Page.update({id:this.value.id, async:false}, this, success);
	    },
	    
	    /**
	     * Update the page with the new data from the creation dialog.
	     * @param {Object} creationData The data from the creation dialog
	     * @param {String} language The languageId
	     */
	    saveMetadata: function(creationData, language, success) {
			if($.isEmptyObject(this.value.head.metadata)) {
				this.value.head.metadata = {};
			}
			if($.isEmptyObject(this.value.head.metadata.title)) {
				this.value.head.metadata.title = {};
			}
			if($.isEmptyObject(this.value.head.metadata.description)) {
				this.value.head.metadata.description = {};
			}
			if($.isEmptyObject(this.value.head.metadata.subject)) {
				this.value.head.metadata.subject = [];
			}
			
			this.value.path = creationData.url;
			this.value.head.template = creationData.layout;
			
			this.value.head.metadata.title[language] = creationData.title;
			this.value.head.metadata.description[language] = creationData.description;
			
			// Filter out empty values
			this.value.head.metadata.subject = creationData.tags.split(/\s*,\s*/).filter(function(value) { 
				return value != ''; 
			});
			
			Page.update({id:this.value.id}, this, success);
		}
	    
	});

});
