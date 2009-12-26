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

package ch.o2it.weblounge.dispatcher;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

/**
 * The <code>RequestHandler</code> class defines an interface for objects
 * capable of servicing <code>HttpServletRequests</code> targeting paths inside
 * the content management system.
 * <p>
 * If the system receives a request, it looks for the
 * <code>RequestHandler</code> that is registered with the target url. In most
 * cases, this will be the class <code>SimpleRequestHandler</code>, but more
 * special handlers may also apply.
 */
public interface RequestHandler {

  /** The processing mode */
  public enum Mode { Default, Cached, Head };

  /**
   * Service method of the <code>RequestHandler</code>. The method takes a
   * <code>HttpServletRequest</code> and the corresponding <code>
	 * HttpServletResponse</code>
   * object to service the request.
   * <p>
   * This method should return <code>true</code> if the handler is decided to
   * handle the request, <code>false</code> otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @return <code>true</code> if this handler processed the request
   */
  boolean service(WebloungeRequest request, WebloungeResponse response);

  /**
   * Returns the request handler identifier.
   * 
   * @return the handler identifier
   */
  String getIdentifier();

  /**
   * Returns the request handler name.
   * 
   * @return the handler name
   */
  String getName();

}