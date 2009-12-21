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

package ch.o2it.weblounge.common.request;

/**
 * Interface for listeners that are interested in the lifecycle of a weblounge
 * request. A typical example for such a listener would be a request statistics
 * implementation.
 * <p>
 * <b>Note:</b> It is important to understand the meaning of this interface. The
 * interface gives access to tracking requests that go through the system.
 * Although both request and response are passed, it is not intended to serve a
 * s a request handler interface, since there might be plenty of listeners and
 * if one starts writing output to the request, no other listener will be able
 * to.
 * <p>
 * That's why this interface relies on the cooperation of implementers. <b>Do
 * not write any output to the response</b> until you know for sure what you are
 * doing!
 */
public interface RequestListener {

  /**
   * Called when a request is about to be processed.
   * 
   * @param request
   *          the incoming request
   * @param response
   *          the associated response
   */
  void requestStarted(WebloungeRequest request, WebloungeResponse response);

  /**
   * This method is called when the processing of <code>request</code> has been
   * finished and the response has been written to the client.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   */
  void requestDelivered(WebloungeRequest request, WebloungeResponse response);

  /**
   * Callback for requests that raise an exception. The <code>reason</code>
   * denotes the <code>HTTP/1.1</code> error response code.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @param reason
   *          the reason of failure
   */
  void requestFailed(WebloungeRequest request, WebloungeResponse response,
      int reason);

}