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

package ch.o2it.weblounge.common.url;

/**
 * The <code>Url</code> defines common methods for all urls.
 */
public interface Url {

  /** identifier to locate the Url object in the session */
  final static String ID = "weblounge::url_id";

  /**
   * Returns the url separator character, like '/' in web urls.
   * 
   * @return the separator character
   */
  char getPathSeparator();

  /**
   * Returns the webapp-relativ path for this url, e. g.
   * <code>/news/articles/</code>.
   * 
   * @return the url path
   */
  String getPath();

  /**
   * Returns <code>true</code> if the url contains the given path.
   * 
   * @param path
   *          the path
   * @return <code>true</code> if the path is contained in this url
   */
  boolean contains(String path);

  /**
   * Returns <code>true</code> if this url is a prefix of <code>url</code> by
   * means of the implementation of <code>equals</code>. Note that this url is
   * also a prefix if it is identical to <code>url</code>.
   * 
   * @param url
   *          the url
   * @return <code>true</code> if this url is a prefix
   */
  boolean isPrefixOf(Url url);

  /**
   * Returns <code>true</code> if this url is an extension of <code>url</code>
   * by means of the implementation of <code>equals</code>. Note that this url
   * is also an extension if it is identical to <code>url</code>.
   * 
   * @param url
   *          the url
   * @return <code>true</code> if this url is a prefix
   */
  boolean isExtensionOf(Url url);

}