/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

/**
 * This support implementation provides special methods for encoding <code>JSON</code>
 * responses.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public abstract class AbstractJSONAction extends AbstractAjaxAction {

	/**
	 * Returns a JSON response to the server. This class keeps track of all the
	 * necessary response header, so implementing classes should start to return
	 * the response body.
	 * 
	 * @param request the weblounge request
	 * @param response the weblounge response
	 */
	public abstract void startJSONResponse(WebloungeRequest request, WebloungeResponse response);
	
	/**
	 * @see ch.o2it.weblounge.site.impl.AbstractAjaxAction.module.action.AbstractAjaxActionHandler#startAjaxResponse(ch.o2it.weblounge.api.request.WebloungeRequest, ch.o2it.weblounge.api.request.WebloungeResponse)
	 */
	public final void startAjaxResponse(WebloungeRequest request, WebloungeResponse response) {
		response.setHeader("Content-Type", "text/json; charset=utf-8");
		startJSONResponse(request, response);
	}

}