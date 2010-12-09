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

/**
 * Main rendering method that is called once the whole view has been loaded.
 */
function renderData(eventData)  {
	switch(eventData.status) {
		case  0: // no sites
			$(".statline").html(i18n.stat_no_sites);
			$('#scr').addClass('ui-helper-hidden');
			break;
		default:
			$(".statline").html(i18n.stat_ok.msgFormat(eventData.status));
			$('#scr').removeClass('ui-helper-hidden');

			tableBody.empty();
			for (var idx in eventData.data) {
				site(eventData.data[idx]);
			}
			$("#plugin_table").trigger("update");
			
			if (drawDetails) renderDetails(eventData);
			initStaticWidgets();
	}
}

function getEntryId(/* Object */ dataEntry) {
    var id = dataEntry.id;
    if (id < 0) {
        id = dataEntry.name;
        if (dataEntry.pid) {
            id += "/" + dataEntry.pid;
        }
    }
    return id;
}

/**
 * Processes a single site from the data that was loaded and adds it to the site
 * table.
 */
function site(/* Object */ dataEntry) {
	var idPath = getEntryId(dataEntry);
	var id = idPath.replace(/[./-]/g, "_");
	var name = dataEntry.name;
	var _ = tableEntryTemplate.clone().appendTo(tableBody).attr('id', 'entry' + id);

	_.find('.bIcon').attr('id', 'img' + id).click(function() {
		showDetails(idPath);
	}).after(drawDetails ? name : ('<a href="' + pluginRoot + '/' + idPath + '">' + name + '</a>'));

	_.find('td:eq(0)').text(dataEntry.id);
	_.find('td:eq(2)').text(dataEntry.state);

	// setup buttons
	if (dataEntry.stateRaw == 1 || dataEntry.stateRaw == 1024) { // disabled
																	// or
																	// disabling
		_.find('li:eq(0)').removeClass('ui-helper-hidden').click(function() { changeDataEntryState(idPath, 'enable') });
	} else {
		_.find('li:eq(1)').removeClass('ui-helper-hidden').click(function() { changeDataEntryState(idPath, 'disable') });
	}
	if (dataEntry.configurable) _.find('li:eq(2)').removeClass('ui-helper-hidden').click(function() { // configure
		changeDataEntryState(dataEntry.pid, 'configure');
	});	
}

function changeDataEntryState(/* long */ id, /* String */ action) {
	if (action == "configure") {
		window.location = appRoot + "/configMgr/" + id;
		return;
	}
	$.post(pluginRoot + "/" + id, {"action":action}, function(data) {
		renderData(data);
	}, "json");	
}

/**
 * Loads the details for the site with the given identifier and makes sure the
 * details are being rendered.
 */
function showDetails(/* long */ id) {
	$.get(pluginRoot + "/" + id + ".json", null, function(data) {
		renderDetails(data);
	}, "json");
}

/**
 * Loads all data and triggers rendering.
 */
function loadData() {
	$.get(pluginRoot + "/.json", null, function(data) {
		renderData(data);
	}, "json");	
}

/**
 * Hides the site details.
 */
function hideDetails(/* String */ id) {
	var __test__ = $("#img" + id);
	$("#img" + id).each(function() {
		$("#pluginInlineDetails").remove();
		$(this).
			removeClass('ui-icon-triangle-1-w').// left
			removeClass('ui-icon-triangle-1-s').// down
			addClass('ui-icon-triangle-1-e').// right
		    attr("title", "Details").
			unbind('click').click(function() {showDetails(id)});
	});
}

/**
 * Adds the site details.
 */
function renderDetails(/* Object */ data) {
	data = data.data[0];
	var id = getEntryId(data).replace(/[./-]/g, "_");
	$("#pluginInlineDetails").remove();
	var __test__ = $("#entry" + id);
	$("#entry" + id + " > td").eq(1).append("<div id='pluginInlineDetails'/>");
	$("#img" + id).each(function() {
		if (drawDetails) {
			var ref = window.location.pathname;
			ref = ref.substring(0, ref.lastIndexOf('/'));
			$(this).
				removeClass('ui-icon-triangle-1-e').// right
				removeClass('ui-icon-triangle-1-s').// down
				addClass('ui-icon-triangle-1-w').// left
				attr("title", "Back").
				unbind('click').click(function() {window.location = ref});
		} else {
			$(this).
				removeClass('ui-icon-triangle-1-w').// left
				removeClass('ui-icon-triangle-1-e').// right
				addClass('ui-icon-triangle-1-s').// down
				attr("title", "Hide Details").
				unbind('click').click(function() {hideDetails(id)});
		}
	});
	$("#pluginInlineDetails").append("<table border='0'><tbody></tbody></table>");
	var details = data.props;
	for (var idx in details) {
		var prop = details[idx];
		var key = i18n[prop.key] ? i18n[prop.key] : prop.key; // i18n

		var txt = "<tr><td class='aligntop' noWrap='true' style='border:0px none'>" + key + "</td><td class='aligntop' style='border:0px none'>";	
		if (prop.value) {
			if ($.isArray(prop.value)) {
				var i = 0;
				for(var pi in prop.value) {
					var value = prop.value[pi];
					if (i > 0) { txt = txt + "<br/>"; }
					var span;
					if (value.substring(0, 2) == "!!") {
						txt = txt + "<span style='color: red;'>" + value + "</span>";
					} else {
						txt = txt + value;
					}
					i++;
				}
			} else {
				txt = txt + prop.value;
			}
		} else {
			txt = txt + "\u00a0";
		}
		txt = txt + "</td></tr>";
		$("#pluginInlineDetails > table > tbody").append(txt);
	}
}

var tableBody = false;
var tableEntryTemplate = false;

$(document).ready(function(){
	tableBody = $('#plugin_table tbody');
	tableEntryTemplate = tableBody.find('tr').clone();

	renderData(scrData);

	$(".reloadButton").click(loadData);

	var extractMethod = function(node) {
		var link = node.getElementsByTagName("a");
		if (link && link.length == 1) {
			return link[0].innerHTML;
		}
		return node.innerHTML;
	};
	
	$("#plugin_table").tablesorter({
		headers: {
			0: { sorter:"digit"},
			3: { sorter: false }
		},
		sortList: [[1,0]],
		textExtraction:extractMethod
	});
});
