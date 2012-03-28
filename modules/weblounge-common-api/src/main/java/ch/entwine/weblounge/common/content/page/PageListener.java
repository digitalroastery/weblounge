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
import ch.entwine.weblounge.common.security.User;

/**
 * This interface defines the methods and fields for a listener that is
 * interested in a page's lifecycle. Such listeners should register with the
 * corresponding site, because this is where the events are triggered.
 */
public interface PageListener {

  /**
   * This method is called if the page at location <code>url</code> has been
   * created by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the creating user
   */
  void pageCreated(ResourceURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * removed by user <code>user</code>.
   * 
   * @param uri
   *          the page's former location
   * @param user
   *          the removing user
   */
  void pageRemoved(ResourceURI uri, User user);

  /**
   * This method is called if the page at location <code>from</code> has been
   * moved to <code>to</code> by user <code>user</code>.
   * 
   * @param from
   *          the page's former location
   * @param to
   *          the page's new location
   * @param user
   *          the user moving the page
   */
  void pageMoved(ResourceURI from, ResourceURI to, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * published by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user publishing the page
   */
  void pagePublished(ResourceURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * unpublished by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user that unpublished the page
   */
  void pageUnpublished(ResourceURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * locked by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user locking the page
   */
  void pageLocked(ResourceURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * released by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user releasing the page lock
   */
  void pageUnlocked(ResourceURI uri, User user);

}