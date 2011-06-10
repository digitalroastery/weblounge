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

package ch.entwine.weblounge.taglib.content;

import javax.servlet.http.Cookie;

/**
 * The tag body of this tag is executed if the cookie with the specified name
 * exists.
 */
public class IfCookieTag extends CookieCheckTag {

  /** Serial version uid */
  private static final long serialVersionUID = 133864500483276000L;

  /**
   * Returns <code>true</code> if the cookie is <code>null</code>.
   * 
   * @param cookie
   *          the cookie to check
   * @see ch.entwine.weblounge.taglib.content.ElementCheckTag#skip(java.lang.String)
   */
  protected boolean skip(Cookie cookie) {
    return cookie == null;
  }

}
