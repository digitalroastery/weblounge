/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.contentrepository;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.user.User;

import java.io.IOException;

/**
 * This type of repository implements methods to write contents to it.
 */
public interface WritableContentRepository extends ContentRepository {

  /**
   * Puts the page to the specified location. Depending on whether the page
   * identified by <code>uri</code> already exists, the method either creates a
   * new page or updates the existing one.
   * <p>
   * The returned page contains the same data than the one passed in as the
   * <code>page</code> argument but with an updated uri.
   * 
   * @param uri
   *          the page uri
   * @param page
   *          the page
   * @return the page with the given uri
   */
  Page put(PageURI uri, Page page);

  /**
   * Updates the given page. This method writes the page header to the database
   * under the specified version tag.
   * 
   * @param user
   *          the user updating the page
   * @param uri
   *          uri of the page to update
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if updating fails due to a database error
   */
  boolean update(PageURI uri, User user) throws SecurityException, IOException;

  /**
   * This method moves the given page to the new uri.
   * 
   * @param uri
   *          uri of the page to move
   * @param target
   *          the target uri
   * @param user
   *          the user
   * @return <code>true</code> if the page could be moved
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if moving fails due to a database error
   */
  boolean move(PageURI uri, PageURI target, User user)
      throws SecurityException, IOException;

  /**
   * This method removes the given page in all available versions from the
   * database.
   * 
   * @param uri
   *          uri of the page to remove
   * @param user
   *          the user
   * @return <code>true</code> if the page could be removed
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if removal fails due to a database error
   */
  boolean delete(PageURI uri, User user) throws SecurityException, IOException;

  /**
   * This method removes the given page in the specified version from the
   * database.
   * 
   * @param uri
   *          uri of the page to remove
   * @param version
   *          the version to remove
   * @param user
   *          the user
   * @param allRevisions
   *          <code>true</code> to remove all revisions
   * @return <code>true</code> if the page could be removed
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if removal fails due to a database error
   */
  boolean delete(PageURI uri, User user, boolean allRevisions)
      throws SecurityException, IOException;

  /**
   * Triggers a re-index of the repository's search index.
   * <p>
   * Depending on the implementation of the search index, the repository might
   * be locked during this operation.
   * 
   * @throws ContentRepositoryException
   *           if the index operation fails
   */
  void index() throws ContentRepositoryException;

}
