/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
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

package ch.o2it.weblounge.dispatcher;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Interface for a class that is capable of including or forwarding requests, e.
 * g. a soap request or jsp dispatcher.
 * 
 * @author Tobias Wunden <tobias.wunden@o2it.ch>
 * @version 1.0
 */
public interface DispatchListener {

  /**
   * Returns either the original path (no redirection) or an altered version of
   * the original path.
   * 
   * @param url
   *          the original url
   * @param request
   *          the original request
   * @param response
   *          the original response
   * @return the altered (redirected) url
   */
  String redirect(String url, WebloungeRequest request, WebloungeResponse response);

  /**
   * Includes the request and returns <code>true</code> or discards it an
   * returns <code>false</code>.
   * 
   * @param request
   *          the request to include
   * @param response
   *          the response
   * @param url
   *          the url to include
   * @return <code>true</code> if the include is handled, <code>false</code>
   *         otherwise
   */
  boolean include(WebloungeRequest request, WebloungeResponse response, String url) throws ServletException, IOException;

  /**
   * Forwards the request and returns <code>true</code> or discards it an
   * returns <code>false</code>.
   * 
   * @param request
   *          the request to forward
   * @param response
   *          the response
   * @param url
   *          the url to forward
   * @return <code>true</code> if the include is handled, <code>false</code>
   *         otherwise
   */
  boolean forward(WebloungeRequest request, WebloungeResponse response, String url) throws ServletException, IOException;

  /**
   * Includes the request and returns <code>true</code> or discards it an
   * returns <code>false</code>.
   * 
   * @param request
   *          the request to include
   * @param response
   *          the response
   * @param url
   *          the url to include
   * @return <code>true</code> if the include is handled, <code>false</code>
   *         otherwise
   */
  boolean internalInclude(ServletRequest request, ServletResponse response, String url) throws ServletException, IOException;

  /**
   * Forwards the request and returns <code>true</code> or discards it an
   * returns <code>false</code>.
   * 
   * @param request
   *          the request to forward
   * @param response
   *          the response
   * @param url
   *          the url to forward
   * @return <code>true</code> if the include is handled, <code>false</code>
   *         otherwise
   */
  boolean internalForward(ServletRequest request, ServletResponse response, String url) throws ServletException, IOException;

}