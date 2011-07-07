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
					success: this.callback(['parseXML', 'wrap', success]),
				});
			} 
			else if ('language' in params) {
				$.ajax('/system/weblounge/files/' + params.id + '/content/' + params.language, {
					success: this.callback(['parseXML', 'wrap', success]),
				});
			}
			else if ('id' in params) {
				$.ajax('/system/weblounge/files/' + params.id, {
					success: this.callback(['parseXML', 'wrap', success]),
				});
			}
		},
		
		/**
		 * Updates the specified file or the specified file content.
		 * @param {Object} params The file identifier
		 * @param {File} file The file object
		 */
		update: function(params, file, success, error){
			if ('language' in params) {
				$.ajax({
					url: '/system/weblounge/files/' + params.id + '/content/' + params.language,
					type: 'put',
					dataType: 'xml',
					data: {content : File.parseJSON(file)}
				});
			}	
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/files/' + params.id,
					type: 'put',
					dataType: 'xml',
					data: {content : File.parseJSON(file)}
				});
			}	
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
					data: {path : params.path, content : File.parseJSON(file)}
				});
			}	
		},
		
		/**
		 * Creates a new file as well as content, either at the given path or at a random location and returns the REST url of the created resource.
		 * @param {Object} params The target path, mimeType and languageid
		 * @param {File} file The file data
		 */
		create: function(params, file, success, error){
			if ('path' in params) {
				$.ajax({
					url: '/system/weblounge/files/uploads',
					type: 'post',
					dataType: 'xml',
					data: {path : params.path, mimeType: params.mimeType, languageid: params.languageid, content : File.parseJSON(file)}
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
					type: 'delete'
				});
			}
			else if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/files/' + params.id,
					type: 'delete'
				});
			}
		},
		
		/**
		 * Converts XML to JSON
		 */
		parseXML: function(xml) {
			return null;
		},
		
		createNewFile: function() {
			return '<file id="6bc19990-8f99-4873-a813-71b6dfac22ad" path="/test/document/" version="live">'
		},
		
		/**
		 * Converts JSON to XML
		 */
		parseJSON: function(json) {
			return null
		}

	},
	/* @Prototype */
	{
	});

});
