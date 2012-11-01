steal.then('jsonix')
.then(function($) {
	
	$.Model('Editor.File',
	/* @Static */
	{
		/**
		 * Returns the file that is located at the given path, the given id or the given id and language
		 * @param {Object} params path, id or id and langauge
		 */
		findOne: function(params, success, error) {
			if ('path' in params) {
				$.ajax('/system/weblounge/files?path=' + params.path, {
					success: function(data, textStatus, jqXHR) {
						// TODO eTag
//						var json = Editor.File.parseXML(data);
//						var file = Editor.File.wrap(json);
//						file.setETag(jqXHR.getResponseHeader('Etag'));
//						success(file);
						this.callback(['parseXML', 'wrap', success])
					}
				});
			} 
			else if ('language' in params) {
				$.ajax('/system/weblounge/files/' + params.id + '/content/' + params.language, {
					success: this.callback(['parseXML', 'wrap', success])
				});
			}
			else if ('id' in params) {
				$.ajax('/system/weblounge/files/' + params.id, {
					success: this.callback(['parseXML', 'wrap', success])
				});
			}
		},
		
		/**
		 * Get all Files
		 */
		findAll: function(params, success, error) {
			var url = "/system/weblounge/files/?sort=created-asc&limit=50&offset=0";
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			if ('type' in params) {
				url += "&type=" + params.type;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Editor.File.parseXML(xml);
					success(Editor.File.concatFiles(json));
				}
			});
		},
		
		/**
		 * Get Recent Files
		 */
		findRecent: function(params, success, error) {
			var url = "/system/weblounge/files/?sort=modified-desc&limit=8&offset=0";
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			if ('type' in params) {
				url += "&type=" + params.type;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Editor.File.parseXML(xml);
					success(Editor.File.concatFiles(json));
				}
			});
		},
		
		/**
		 * Get Pending Files
		 */
		findPending: function(params, success, error) {
			var url = "/system/weblounge/files/pending?limit=8";
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			if ('type' in params) {
				url += "&type=" + params.type;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Editor.File.parseXML(xml);
					success(Editor.File.concatFiles(json));
				}
			});
		},
		
		/**
		 * Get files searched by string
		 */
		findBySearch: function(params, success, error) {
			var url = '/system/weblounge/files/?searchterms=' + params.search + '&sort=modified-desc&limit=8&offset=0';
			if ('filter' in params) {
				url += "&filter=" + params.filter;
			}
			if ('type' in params) {
				url += "&type=" + params.type;
			}
			$.ajax(url, {
				success: function(xml) {
					var json = Editor.File.parseXML(xml);
					success(Editor.File.concatFiles(json));
				}
			});
		},
		
		/**
		 * Returns pages containing references to the page with the given id
		 */
		findReferrer: function(params, success, error) {
			if ('id' in params) {
				var url = '/system/weblounge/files/' + params.id + '/referrer';
				$.ajax(url, {
					success: function(xml) {
						var json = Page.parseXML(xml);
						success(json.value.page);
					}
				});
			}
		},
		
		/**
		 * Updates the specified file or the specified file content.
		 * @param {Object} params The file identifier, language and eTag
		 * @param {File} file The file object
		 */
		update: function(params, file, success, error){
			var headers = {};
			var url = '';
			
			if('eTag' in params)
				headers = {"If-Match": params.eTag};
			if ('id' in params)
				url = '/system/weblounge/files/' + params.id;
			if ('language' in params)
				url += '/content/' + params.language;
			
			$.ajax({
				url: url,
				type: 'put',
				headers: headers,
				success: success,
				dataType: 'xml',
				data: {content : Editor.File.parseJSON(file)}
			});
		},
		
		/**
		 * Creates a new file, either at the given path or at a random location and returns the REST url of the created resource.
		 * @param {Object} params The target path
		 * @param {File} file The file data
		 */
		create: function(params, file, success, error){
			if ('path' in params) {
				$.ajax({
					url: '/system/weblounge/files/',
					type: 'post',
					dataType: 'xml',
					success: success,
					data: {path : params.path, content : Editor.File.parseJSON(file)}
				});
			}	
		},
		
		/**
		 * Creates a new file as well as content, either at the given path or at a random location and returns the REST url of the created resource.
		 * @param {Object} params The target path, mimeType and languageid
		 * @param {File} file The file data
		 */
		upload: function(params, file, success, error){
			if ('path' in params) {
				$.ajax({
					url: '/system/weblounge/files/uploads',
					type: 'post',
					dataType: 'xml',
					data: {path : params.path, mimeType: params.mimeType, languageid: params.languageid, content : Editor.File.parseJSON(file)}
				});
			}	
		},
		
		/**
		 * Deletes the specified file or the specified file content.
		 * @param {Object} params The file id or id and language
		 */
		destroy: function(params, success, error){
			if ('language' in params) {
				$.ajax({
					url: '/system/weblounge/files/' + params.id + '/content/' + params.language,
					type: 'delete',
					success: success,
					error: error
				});
			}
			else if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/files/' + params.id,
					type: 'delete',
					success: success,
					error: error
				});
			}
		},
		
		/**
		 * Concat file, image and video array
		 */
		concatFiles: function(json) {
			if($.isEmptyObject(json.value.movie)) {
				json.value.movie = [];
			}
			if($.isEmptyObject(json.value.image)) {
				json.value.image = [];
			}
			if($.isEmptyObject(json.value.file)) {
				json.value.file = [];
			}
			
			$.each(json.value.movie, function(index, movie) {
				movie.type = 'movie';
			});
			$.each(json.value.image, function(index, image) {
				image.type = 'image';
			});
			$.each(json.value.file, function(index, file) {
				file.type = 'file';
			});
			return new Array().concat(json.value.movie, json.value.image, json.value.file);
		},
		
		/**
		 * Converts XML to JSON
		 */
		parseXML: function(xml) {
			var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
			return unmarshaller.unmarshalDocument(xml);
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
	     * Return the file title
	     * @param {String} language
	     */
	    getTitle: function(language) {
	    	if($.isEmptyObject(this.value.head.metadata)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.title)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.title[language])) return '';
	    	return this.value.head.metadata.title[language];
	    },
	    
	    /**
	     * Return the file description
	     * @param {String} language
	     */
	    getDescription: function(language) {
	    	if($.isEmptyObject(this.value.head.metadata)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.description)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.description[language])) return '';
	    	return this.value.head.metadata.description[language];
	    },
	    
	    /**
	     * Return the file tags
	     */
	    getTags: function() {
	    	if($.isEmptyObject(this.value.head.metadata)) return '';
	    	if($.isEmptyObject(this.value.head.metadata.subject)) return '';
	    	return this.value.head.metadata.subject;
	    },
	    
	    /**
	     * Return the content author
	     * @param {String} language
	     */
	    getAuthor: function(language) {
	    	var content = this.getContent(language);
	    	if($.isEmptyObject(content)) return '';
	    	if($.isEmptyObject(content.author)) return '';
	    	return content.author;
	    },
	    
		/**
		 * Return the specified Content or the original if not found
		 * @param {String} language
		 */
	    getContent: function(language) {
	    	if($.isEmptyObject(this.value.body)) return null;
	    	if($.isEmptyObject(this.value.body.contents)) return null;
	    	var content = null;
	    	var originalContent = null;
	    	$.each(this.value.body.contents, function(i, cont) {
	    		if(cont.language == language) {
	    			content = cont;
	    		}
	    		if(originalContent == null || originalContent.created.date > cont.created.date) {
	    			originalContent = cont;
	    		}
    		});
	    	if(content == null)
	    		return originalContent;
	    	return content;
	    },
		
	    /**
	     * Return the editable file metadata
	     * @param {String} language
	     */
		getMetadata: function(language) {
			var metadata = new Object();
			metadata.title = '';
			metadata.description = '';
			metadata.tags = '';
			metadata.path = '';
			metadata.author = this.getAuthor(language);
			
			if(!$.isEmptyObject(this.value.path)) {
				metadata.path = this.value.path;
			}
			if($.isEmptyObject(this.value.head.metadata)) {
				this.value.head.metadata = {};
			}
			if(!$.isEmptyObject(this.value.head.metadata.title) && this.value.head.metadata.title[language] != undefined) {
				metadata.title = this.value.head.metadata.title[language];
			}
			if(!$.isEmptyObject(this.value.head.metadata.description) && this.value.head.metadata.description[language] != undefined) {
				metadata.description = this.value.head.metadata.description[language];
			}
			if(!$.isEmptyObject(this.value.head.metadata.subject)) {
				metadata.tags = this.value.head.metadata.subject.toString();
			}
			return metadata;
		},
		
		/**
		 * Update the editable file metadata
		 */
		saveMetadata: function(metadata, language, eTag, success) {
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
			
			if(!$.isEmptyObject(metadata.path)) {
				this.value.path = metadata.path.toLowerCase()
	            		  .replace(/^\s+|\s+$/g, "")
	            		  .replace(/[_|\s]+/g, "-")
	            		  .replace(/[^a-z/\u0400-\u04FF0-9-]+/g, "")
	            		  .replace(/[-]+/g, "-")
	            		  .replace(/^-+|-+$/g, "")
	            		  .replace(/[-]+/g, '-');
			}
			if(!$.isEmptyObject(metadata.title)) {
				this.value.head.metadata.title[language] = metadata.title;
			}
			if(!$.isEmptyObject(metadata.description)) {
				this.value.head.metadata.description[language] = metadata.description;
			}
			if(!$.isEmptyObject(metadata.author)) {
				var content = this.getContent(language);
				content.author = metadata.author;
			}
			
			// Filter out empty values
			this.value.head.metadata.subject = metadata.tags.split(/\s*,\s*/).filter(function(value) { 
				return value != ''; 
			});
			
			if(eTag == null) 
				Editor.File.update({id:this.value.id}, this, success);
			else {
				eTag = eTag.replace(/"/g, '');
				Editor.File.update({id:this.value.id, eTag: eTag}, this, success);
			}
		}
		
	});

});
