steal.then(function($) {
	
	$.Model('Workbench',
	/* @Static */
	{
		findOne: function(params, success, error) {
			if ('id' in params) {
				$.get('/system/weblounge/workbench/edit/' + params.id + '/' + params.composer + '/' + params.pagelet, function(xml) {
					success(xml);
				}, 'xml');
			}
		}
	},
	/* @Prototype */
	{});

});
