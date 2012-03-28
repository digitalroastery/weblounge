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

package ch.entwine.weblounge.common.content.page;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

/**
 * This interface defines the methods for listeners that are interested in page
 * access.
 */
public interface PageAccessListener {

  /**
   * Notifies the listener about an access to the page identified by
   * <code>uri</code>.
   * 
   * @param uri
   *          the accessed uri
   * @param user
   *          the accessing user
   * @param language
   *          the requested language
   * @param version
   *          the requested version
   */
  void access(ResourceURI uri, User user, Language language, String version);

}