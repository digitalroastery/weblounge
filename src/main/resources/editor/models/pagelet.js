/**
 * @tag models, home
 * Wraps backend pagelet services.  Enables 
 * [Editor.Models.Pagelet.static.findAll retrieving],
 * [Editor.Models.Pagelet.static.update updating],
 * [Editor.Models.Pagelet.static.destroy destroying], and
 * [Editor.Models.Pagelet.static.create creating] pagelets.
 */
$.Model.extend('Editor.Models.Pagelet',
/* @Static */
{
	/**
 	 * Retrieves pagelets data from your backend services.
 	 * @param {Object} params params that might refine your results.
 	 * @param {Function} success a callback function that returns wrapped pagelet objects.
 	 * @param {Function} error a callback function for an error in the ajax request.
 	 */
	findAll: function( params, success, error ){
		$.ajax({
			url: '/pagelet',
			type: 'get',
			dataType: 'json',
			data: params,
			success: this.callback(['wrapMany',success]),
			error: error,
			fixture: "//editor/fixtures/pagelets.json.get" //calculates the fixture path from the url and type.
		});
	},
	/**
	 * Updates a pagelet's data.
	 * @param {String} id A unique id representing your pagelet.
	 * @param {Object} attrs Data to update your pagelet with.
	 * @param {Function} success a callback function that indicates a successful update.
 	 * @param {Function} error a callback that should be called with an object of errors.
     */
	update: function( id, attrs, success, error ){
		$.ajax({
			url: '/pagelets/'+id,
			type: 'put',
			dataType: 'json',
			data: attrs,
			success: success,
			error: error,
			fixture: "-restUpdate" //uses $.fixture.restUpdate for response.
		});
	},
	/**
 	 * Destroys a pagelet's data.
 	 * @param {String} id A unique id representing your pagelet.
	 * @param {Function} success a callback function that indicates a successful destroy.
 	 * @param {Function} error a callback that should be called with an object of errors.
	 */
	destroy: function( id, success, error ){
		$.ajax({
			url: '/pagelets/'+id,
			type: 'delete',
			dataType: 'json',
			success: success,
			error: error,
			fixture: "-restDestroy" // uses $.fixture.restDestroy for response.
		});
	},
	/**
	 * Creates a pagelet.
	 * @param {Object} attrs A pagelet's attributes.
	 * @param {Function} success a callback function that indicates a successful create.  The data that comes back must have an ID property.
	 * @param {Function} error a callback that should be called with an object of errors.
	 */
	create: function( attrs, success, error ){
		$.ajax({
			url: '/pagelets',
			type: 'post',
			dataType: 'json',
			success: success,
			error: error,
			data: attrs,
			fixture: "-restCreate" //uses $.fixture.restCreate for response.
		});
	}
},
/* @Prototype */
{});