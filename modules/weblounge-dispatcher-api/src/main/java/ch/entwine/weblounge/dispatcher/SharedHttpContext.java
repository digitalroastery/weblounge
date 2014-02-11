/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.dispatcher;


/**
 * Constant definition for the shared <code>HTTP</code> context.
 */
public interface SharedHttpContext {

  /**
   * The shared context key as used throughout OSGi.
   */
  String CONTEXT_ID = "contextId";

  /**
   * The context key for marking shared contexts.
   */
  String SHARED = "context.shared";

  /**
   * The key for the servlet alias.
   */
  String ALIAS = "alias";

  /**
   * Key for the servlet name.
   */
  String SERVLET_NAME = "servlet-name";

  /**
   * The key for defining a pattern for request filters.
   */
  String PATTERN = "pattern";

  /**
   * Prefix for servlet init keys.
   */
  String INIT_PREFIX = "init.";

  /** Property to define the ranking of a service in the filter chain */
  String SERVICE_RANKING = "service.ranking";

  /** The shared context identifier */
  String WEBLOUNGE_CONTEXT_ID = "weblounge";

}
