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

package ch.entwine.weblounge.contentrepository;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.PageListener;
import ch.entwine.weblounge.common.security.User;

import java.io.IOException;

/**
 * This manager provides access to a site's pages.
 */
public interface PageManager {

  /** Mode identifier */
  String MODE = "weblounge:editmode";

  /** Mode identifier live */
  String MODE_LIVE = "live";

  /** Mode identifier work */
  String MODE_WORK = "work";

  /**
   * Adds a <code>PageListener</code> to the page registry, who will be notified
   * about new, moved, deleted or altered pages.
   * 
   * @param listener
   *          the new page listener
   */
  void addPageListener(PageListener listener);

  /**
   * Removes a <code>PageListener</code> from the page registry.
   * 
   * @param listener
   *          the page listener
   */
  void removePageListener(PageListener listener);

  /**
   * Locks the given page and returns <code>true</code> if the operation was
   * successful, <code>false</code> otherwise.
   * 
   * @return <code>true</code> if locking was successful
   * @throws SecurityException
   *           if access is denied for the given user and action
   * @throws IOException
   *           if locking fails due to a database error
   */
  boolean lock(ResourceURI uri, User user) throws SecurityException, IOException;

  /**
   * Unlocks the given page if it has been locked by this user and returns
   * <code>true</code> otherwise <code>false</code> is returned.
   * 
   * @return <code>true</code> if unlocking was successful
   * @throws SecurityException
   *           if access is denied for the given user and action
   * @throws IOException
   *           if unlocking fails due to a database error
   */
  boolean unlock(ResourceURI uri, User user) throws SecurityException, IOException;

  /**
   * This method copies the work page to the live version if the user has
   * publishing rights on the page.
   * 
   * @param url
   *          the url to publish
   * @param user
   *          the user
   * @throws SecurityException
   *           if the user is not entitled to publish the page
   * @throws IOException
   *           if publishing fails due to a database error
   */
  boolean publish(ResourceURI uri, User user) throws SecurityException, IOException;

  /**
   * This method removes the current live version from the database.
   * 
   * @param url
   *          the url to unpublish
   * @param user
   *          the user
   * @throws SecurityException
   *           if the user is not entitled to unpublish the page
   * @throws IOException
   *           if unpublishing fails due to a database error
   */
  boolean unpublish(ResourceURI uri, User user) throws SecurityException,
      IOException;

}