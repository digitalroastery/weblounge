steal.plugins('jquery/model')
.then(function($) {
	
	$.Model('Workbench',
	/* @Static */
	{
		/**
		 * Returns the editor for the given pagelet
		 */
		getPageletEditor: function(params, success, error) {
			var url = '/system/weblounge/workbench/edit/' + params.id + '/' + params.composer + '/' + params.pagelet;
			if ('language' in params) {
				url += "?language=" + params.language;
			}
			if ('id' in params) {
				$.ajax(url, {
					success: success
				});
			}
		},
		
		/**
		 * Returns the renderer for the given pagelet
		 */
		getRenderer: function(params, success, error) {
			var url = '/system/weblounge/workbench/renderer/' + params.id + '/' + params.composer + '/' + params.pageletId;
				
			if ('language' in params) {
				url += "?language=" + params.language;
			}
			if ('id' in params) {
				if(params.pagelet != undefined) {
					$.ajax(url, {
						type: 'GET',
						data: params.pagelet,
						success: success
					});
				} else {
					$.ajax(url, {
						type: 'GET',
						success: success
					});
				}
			}
		},
		
		/**
		 * Returns a list of suggested tags based on an initial hint. The number of
		 * suggestions returned can be specified using the <code>limit</code>
		 * parameter.
		 */
		suggestTags: function(params, success, error) {
			if ('seed' in params) {
				$.get('/system/weblounge/workbench/suggest/tags/' + params.seed, function(tags) {
					success(tags);
				}, 'xml');
			}
		}
	},
	/* @Prototype */
	{}
	);
});