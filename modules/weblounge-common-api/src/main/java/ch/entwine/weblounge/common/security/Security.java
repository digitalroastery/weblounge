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

package ch.entwine.weblounge.common.security;

/**
 * Marker interface to indicate that security is in place. This interface will
 * be published in the OSGi service registry once security filters have been
 * installed.
 * <p>
 * Bundles that depend on active security settings can simply add a reference to
 * this interface to be noticed once security is active.
 */
public interface Security {

  /** Name of the system admin role */
  String SYSTEM_ADMIN_ROLE = "systemadministrator";

  /** Name of the site admin role */
  String SITE_ADMIN_ROLE = "administrator";

  /** Name of the publisher role */
  String PUBLISHER_ROLE = "publisher";

  /** Name of the editor role */
  String EDITOR_ROLE = "editor";

  /** Name of the anonymous role */
  String GUEST_ROLE = "guest";

  /** The context for system role definitions */
  String SYSTEM_CONTEXT = "weblounge";

  /** Login of the generic anonymous user */
  String ANONYMOUS_USER = "anonymous";

  /** Name of the generic anonymous user */
  String ANONYMOUS_NAME = "Anonymous";

  /** Login of the generic admin user */
  String ADMIN_USER = "admin";

  /** Name of the generic admin user */
  String ADMIN_NAME = "Weblounge System Administrator";

}
