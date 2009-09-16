/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl.request;

import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

/**
 * This class is a convenience implementation for the <code>RequestListener</code>
 * interface. The provided methods contain a <code>null</code> implementation of
 * the various listener methods and may be overwritten individually.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class RequestAdapter implements RequestListener {

	/**
	 * Called when a request is ready to be processed. At this stage, the response
	 * is written back to the client.
	 * 
	 * @param request the incoming request
	 * @param response the associated response
	 * @see ch.o2it.weblounge.common.request.RequestListener#requestStarted(WebloungeRequest, WebloungeResponse)
	 */
	public void requestStarted(WebloungeRequest request,
			WebloungeResponse response) {
		return;
	}
	
	/**
	 * This method is called when the processing of <code>request</code>
	 * has been finished and the response has been written to the client.
	 * 
	 * @param request the servlet request
	 * @param response the servlet response
	 * @see ch.o2it.weblounge.common.request.RequestListener#requestDelivered(WebloungeRequest, WebloungeResponse)
	 */
	public void requestDelivered(WebloungeRequest request,
			WebloungeResponse response) {
		return;
	}
	
	/**
	 * Callback for requests that raise an exception. The <code>reason</code>
	 * denotes the <code>HTTP/1.1</code> error response code.
	 * 
	 * @param request the servlet request
	 * @param response the servlet response
	 * @param reason the reason of failure
	 * @see ch.o2it.weblounge.common.request.RequestListener#requestFailed(WebloungeRequest, WebloungeResponse, int)
	 */
	public void requestFailed(WebloungeRequest request,
			WebloungeResponse response, int reason) {
		return;
	}

}