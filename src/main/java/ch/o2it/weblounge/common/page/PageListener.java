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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.security.User;

/**
 * This interface defines the methods and fields for a listener that is
 * interested in a page's lifecycle. Such listeners should register with the
 * corresponding site, because this is where the events are triggered.
 * 
 * TODO: Add more callbacks (See PageListenerAdapter)
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
  void pageCreated(PageURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * removed by user <code>user</code>.
   * 
   * @param uri
   *          the page's former location
   * @param user
   *          the removing user
   */
  void pageRemoved(PageURI uri, User user);

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
  void pageMoved(PageURI from, PageURI to, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * published by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user publishing the page
   */
  void pagePublished(PageURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * unpublished by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user unpublishing the page
   */
  void pageUnpublished(PageURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * locked by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user locking the page
   */
  void pageLocked(PageURI uri, User user);

  /**
   * This method is called if the page at location <code>url</code> has been
   * released by user <code>user</code>.
   * 
   * @param uri
   *          the page's location
   * @param user
   *          the user releasing the page lock
   */
  void pageUnlocked(PageURI uri, User user);

}