/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

var Docs = (function($) {
	
	var docs = {};
	
	/**
	 * Initialize the docs page, mainly by registering event listeners on the
	 * relevant elements.
	 */
	docs.init = function() {

	    /**
	     * Add a click handler to the link that shows the test form.
	     */
	    $("a.link_show_testform").click(
	        function() {
	           var link = $(this);
	           var form = link.parent().find("div.hidden_form");
	           var link_hide_testform = link.parent().find("a.link_hide_testform");
	           form.fadeIn(400);
	           link_hide_testform.show();
	           link.hide();
	           return false;
	        }
	    );

	    /**
	     * Add a click handler to the link that hides the test form.
	     */
	    $("a.link_hide_testform").click(
	        function() {
	           var link = $(this);
	           var form = link.parent().find("div.hidden_form");
	           var link_show_testform = link.parent().find("a.link_show_testform");
	           form.fadeOut(200);
	           link_show_testform.show();
	           link.hide();
	           return false;
	        }
	     );

	    /**
	     * Loops through all the test forms and wires the input fields with the
	     * checkXYZ() - methods.
	     */
	    $("form.form_test_form").each(function() {
	        var form = $(this);
	        if (form.find("input.form_action_holder").length >= 1) {

	            // wire path parameters
	            var formParams = form.find(".form_param_path");
	            if (formParams.length > 0) {
			        formParams.change(function() {
			        	checkPath(form);
			        });
			        formParams.keyup(function() {
			        	checkPath(form);
			        });
	            }
	            checkPath(form);

	            // wire required parameters
	            var reqParameters = form.find(".form_param_required");
	            if (reqParameters.length > 0) {
	                reqParameters.change(function() {
	                	checkRequired(form);
	                });
	                reqParameters.keyup(function() {
	                	checkRequired(form);
	                });
	            }
	            checkRequired(form);

	            // handle the ajax submissions
	            if (form.find("input.form_ajax_submit").length >= 1) {
	              // add an event handler to the form submit
	              form.bind('submit', function() {
	                  if (checkRequired(form)) {
	                      var submitParameters = {};
	                      form.find(".form_param_submit").each(function() {
	                          var param = $(this);
	                          submitParameters[param.attr('name')] = param.val();
	                      });

	                      var method = form.find(".form_method").val();
	                      var url = form.attr('action');
	                      form.parent().find(".test_form_working").show();
	                      form.parent().find(".test_form_response input").click(function() {
	                          $(this).parent.hide();
	                      });

	                      // clear previous responses
	                      var responseBody = form.parent().find(".test_form_response");
	                      responseBody.hide();
	                      responseBody.find("div.response_status").text("");
	                      responseBody.find("div.response_headers").text("");
	                      responseBody.find("pre.response_body").text("");

	                      // make the request
	                      $.ajax({
	                          type: method,
	                          url: url,
	                          processData: true,
	                          dataType: 'text',
	                          data: submitParameters,
	                          success: function(data, textStatus, request) {
	                              form.parent().find(".test_form_working").hide();
	                              
	                              // response status
	                              var statusText = "<b>Status:</b> <tt>" + request.status + " (" + request.statusText + ")</tt>";
	                              responseBody.find("div.response_status").html(statusText);

	                              // response headers
	                              var headers = request.getAllResponseHeaders();
	                              var contentType = "";
	                              if (headers !== undefined) {
	                                var headersText = "";
	                                var headerLines = headers.split("\n");
	                                for (var i = 0; i < headerLines.length; i++) {
	                                  if (headerLines[i] !== "") {
	                                    var header = headerLines[i].replace("\r", "").split("\: ");
	                                    headersText += "<b>" + header[0] + "</b>: " + header[1] + "<br/>";
	                                    if (header[0] == "Content-Type") {
	                                        contentType = header[1];
	                                    }
	                                  }
	                                }
	                                responseBody.find("div.response_headers").html(headersText);
	                              }
	                              
	                              // response body
	                              if (data !== undefined && data != "" && contentType.indexOf("text") == 0) {
	                                responseBody.find("pre.response_body").text(prettify(data, contentType));
	                                responseBody.find("pre.response_body").show();
	                              } else {
	                                responseBody.find("pre.response_body").hide();
	                              }
	                              responseBody.show();
	                          },
	                          error: function(request, textStatus, errorThrown) {
	                              form.parent().find(".test_form_working").hide();
	                              
	                              // response status
	                              var statusText = "<b>Status:</b> <tt>" + request.status + " (" + request.statusText + ")</tt>";
	                              responseBody.find("div.response_status").html(statusText);

	                              // response headers
	                              var headers = request.getAllResponseHeaders();
	                              if (headers !== undefined) {
	                                var headersText = "";
	                                var headerLines = headers.split("\n");
	                                for (var i = 0; i < headerLines.length; i++) {
	                                  if (headerLines[i] !== "") {
	                                    var header = headerLines[i].split("\: ");
	                                    headersText += "<b>" + header[0] + "</b>: " + header[1] + "<br/>";
	                                  }
	                                }
	                                responseBody.find("div.response_headers").html(headersText);
	                              }
	                              
	                              // response body
	                              responseBody.find("pre.response_body").hide();
	                              responseBody.show();
	                          }
	                      });
	                  } else {
	                      alert("Fill out all required fields first");
	                  }
	                  return false;
	              });
	            }
	        }
	    });
	        
	};

    /**
     * Takes a path and integrates the pathParameters values into it.
     *
     * @param path the path with keys (e.g. /my/{thing}/{stuff})
     * @param parameters the parameters to put into the path (e.g. {'thing':'apple'})
     */
    var updatePath = function(path, parameters) {
        var newPath = path;
        for (var key in parameters) {
            if (parameters.hasOwnProperty(key)) {
                var value = parameters[key];
                if (value !== undefined && value !== null && value !== '') {
                    newPath = newPath.replace('{' + key + '}', value);
                }
            }
        }
        return newPath;
    };

    /**
     * Checks the path parameters.
     */
    var checkPath = function(form) {
        var parameters = [];
        form.find(".form_param_path").each(function() {
            var param = $(this);
            parameters[param.attr('name')] = param.val();
        });
        var form_path = form.find("input.form_action_holder").val();
        var path = updatePath(form_path, parameters);
        // update form and display
        form.attr('action', path);
        form.find(".form_path").html(path);
        if (path.indexOf("{") >= 0 && path.indexOf("}") >= 0) {
            return false;
        }
        return true;
    };

    /**
     * Checks the form for required parameters and disables the form's submit
     * button if a required parameter is missing.
     */
    var checkRequired = function(form) {
        var required = form.find(".form_param_required");
        var total = required.length;
        var counter = 0;
        required.each(function() {
            var formField = $(this);
            if (formField.val() !== undefined && formField.val() !== '') {
                counter++;
            }
        });
        var formInputs = form.find("div.form_submit input");
        if (counter >= total) {
            // submit is ok
            formInputs.removeAttr('disabled');
            return true;
        } else {
            // disable form submit until required options at set
            formInputs.attr('disabled', 'disabled');
	            return false;
        }
    };
  
    /**
     * Returns a pretty-printed version of an xml response body.
     */
    var prettify = function(content, contentType) {
        if (contentType !== "text/xml")
            return content;

        var reg = /(>)(<)(\/*)/g;
        var wsexp = / *(.*) +\n/g;
        var contexp = /(<.+>)(.+\n)/g;
        content = content.replace(reg, "$1\n$2$3").replace(wsexp, "$1\n").replace(contexp, "$1\n$2");
        var pad = 0;
        var formatted = "";
        var lines = content.split("\n");
        var indent = 0;
        var lastType = "other";
        // 4 types of tags - single, closing, opening, other (text, doctype, comment) - 4*4 = 16 transitions 
        var transitions = {
            "single->single": 0,
            "single->closing": -1,
            "single->opening": 0,
            "single->other": 0,
            "closing->single": 0,
            "closing->closing": -1,
            "closing->opening": 0,
            "closing->other": 0,
            "opening->single": 1,
            "opening->closing": 0,
            "opening->opening": 1,
            "opening->other": 1,
            "other->single": 0,
            "other->closing": -1,
            "other->opening": 0,
            "other->other": 0
        };

        for (var i = 0; i < lines.length; i++) {
            var ln = lines[i];
            var single = Boolean(ln.match(/<.+\/>/)); // is this line a single tag? ex. <br />
            var closing = Boolean(ln.match(/<\/.+>/)); // is this a closing tag? ex. </a>
            var opening = Boolean(ln.match(/<[^!].*>/)); // is this even a tag (that"s not <!something>)
            var type = single ? "single" : closing ? "closing" : opening ? "opening" : "other";
            var fromTo = lastType + "->" + type;
            lastType = type;
            var padding = "";

            indent += transitions[fromTo];
            for (var j = 0; j < indent; j++) {
                padding += "  ";
            }
            if (fromTo == "opening->closing")
                formatted = formatted.substr(0, formatted.length - 1) + ln + "\n"; // substr removes line break (\n) from prev loop
            else
                formatted += padding + ln + "\n";
        }
        
        return formatted;
    };
    
    return docs;
    
}(jQuery));