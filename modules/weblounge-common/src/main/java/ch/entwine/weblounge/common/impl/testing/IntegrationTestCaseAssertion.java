/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.testing;

import org.w3c.dom.Node;

import java.util.Map;

/**
 * An interface for <code>HTTP</code> response verification.
 */
public interface IntegrationTestCaseAssertion {

  /**
   * Verifies that the assertion holds with respect to the status code and
   * response body.
   * 
   * @param statusCode
   *          the status code
   * @param headers
   *          the response headers
   * @param response
   *          the response body
   * @throws IllegalStateException
   *           if the assertion failed
   * @throws Exception
   *           if the assertion cannot be verified
   */
  void verify(int statusCode, Map<String, String> headers, Node response)
      throws Exception;

}
