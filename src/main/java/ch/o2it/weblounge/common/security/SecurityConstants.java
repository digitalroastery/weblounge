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

package ch.o2it.weblounge.common.security;

/**
 * Common security constant definitions.
 */
public interface SecurityConstants {

  /** Name of the admin role */
  String MH_ADMIN = "weblounge_admin";

  /** Name of the anonymous role */
  String MH_ANONYMOUS = "weblounge_anonymous";

  /** The default organization identifier */
  String DEFAULT_ORGANIZATION_ID = "weblounge_system";

  /** The default organization name */
  String DEFAULT_ORGANIZATION_NAME = "Weblounge";

  /** Name of the default organization's local admin role */
  String DEFAULT_ORGANIZATION_ADMIN = "admin";

  /** Name of the default organization's local anonymous role */
  String DEFAULT_ORGANIZATION_ANONYMOUS = "anonymous";

}
