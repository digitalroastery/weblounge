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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.io.IOException;

/**
 * An <code>XMLAction</code> is java code that can be mounted to a specific url
 * and that will be executed if these conditions hold:
 * <ol>
 * <li>The request is directly targeted at the action's mountpoint</li>
 * <li>The action supports the {@link RequestFlavor#XML} request flavor</li>
 * <li>The client specifies that same flavor in his request</li>
 * </ol>
 * <p>
 * Once a request hits the mountpoint of an action, the action is then called in
 * this order:
 * <ol>
 * <li>{@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)}</li>
 * <li>{@link #startResponse(WebloungeRequest, WebloungeResponse)}</li>
 * <li>{@link #startXML(WebloungeRequest, WebloungeResponse)}</li>
 * </ol>
 * <p>
 * <b>Note:</b> A class that implements the <code>XMLAction</code> interface has
 * to provide a default constructor (no arguments), since action handlers are
 * created using reflection.
 */
public interface XMLAction extends Action {

  /**
   * Once the action has evaluated the request in
   * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)} and
   * the call to {@link #startResponse(WebloungeRequest, WebloungeResponse)}
   * returned {@link Action#EVAL_REQUEST}, this method is called to ask the
   * action to write the <code>XML</code> data to the response.
   * <p>
   * <code>Note:</code> Make sure you are using the correct encoding when
   * writing data to the response. Ususally,
   * {@link #startResponse(WebloungeRequest, WebloungeResponse)} will have set
   * an <code>HTTP</code> response header such as
   * <code>Content-Type", "text/xml; charset=utf-8</code>, which means that you
   * should send your data encoded in <code>utf-8</code>.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @throws IOException
   *           if writing the response to the client fails
   * @throws ActionException
   *           if generating the <code>XML</code> response results in an error
   */
  void startXML(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException;

}