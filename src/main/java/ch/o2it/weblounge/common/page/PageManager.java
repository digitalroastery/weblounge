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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SecurityException;
import ch.o2it.weblounge.common.user.User;

import java.io.IOException;
import java.util.Iterator;

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
   * Registers the vetoable page listener <code>l</code> with this url registry.
   * The listener will be notified about common changes that apply to the pages
   * in this registry and will be asked to approve some of them.
   * 
   * @param l
   *          the listener
   */
  void addPageTransactionListener(PageTransactionListener l);

  /**
   * Removes the vetoable page listener <code>l</code> from this url registry's
   * listener list.
   * 
   * @param l
   *          the listener
   */
  void removePageTransactionListener(PageTransactionListener l)
      throws SecurityException;

  /**
   * Creates the partition with the given name and inserts the default page.
   * 
   * @param partition
   *          the partition name
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if creation fails due to a database error
   */
  Page create(PageURI uri, User user, Language language, String template,
      String title, String type, String[] keywords) throws SecurityException,
      IOException;

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
   * This method removes the given page in the specified version from the
   * database.
   * 
   * @param uri
   *          uri of the page to remove
   * @param version
   *          the version to remove
   * @param user
   *          the user
   * @return <code>true</code> if the page could be removed
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if removal fails due to a database error
   */
  boolean remove(PageURI uri, User user, long version)
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
  boolean remove(PageURI uri, User user) throws SecurityException, IOException;

  /**
   * Returns the requested page or <code>null</code> if the page is not
   * available.
   * 
   * @param uri
   *          the page uri
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * 
   * @return the page or <code>null</code>
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if page lookup fails due to a database error
   */
  Page getPage(PageURI uri, User user, Permission p) throws SecurityException,
      IOException;

  /**
   * Returns the requested page or <code>null</code> if the page is not
   * available.
   * 
   * @param uri
   *          the page uri
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * @param preload
   *          <code>true</code> to force loading of the page body
   * 
   * @return the page or <code>null</code>
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if page lookup fails due to a database error
   */
  Page getPage(PageURI uri, User user, Permission p, boolean preload)
      throws SecurityException, IOException;

  /**
   * Returns <code>true</code> if the requested page exists for the given user
   * and is accessible with respect to permissions and version.
   * 
   * @param url
   *          the page url
   * @param site
   *          the associated site
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * @return <code>true</code> if the page exists
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if page lookup fails due to a database error
   */
  boolean exists(PageURI uri, User user, Permission p)
      throws SecurityException, IOException;

  /**
   * Locks the given page and returns <code>true</code> if the operation was
   * successful, <code>false</code> otherwise.
   * 
   * @return <code>true</code> if locking was successful
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if locking fails due to a database error
   */
  boolean lock(PageURI uri, User user) throws SecurityException, IOException;

  /**
   * Unlocks the given page if it has been locked by this user and returns
   * <code>true</code> otherwise <code>false</code> is returned.
   * 
   * @return <code>true</code> if unlocking was successful
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if unlocking fails due to a database error
   */
  boolean unlock(PageURI uri, User user) throws SecurityException, IOException;

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
  boolean publish(PageURI uri, User user) throws SecurityException, IOException;

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
  boolean unpublish(PageURI uri, User user) throws SecurityException,
      IOException;

  /**
   * Returns all direct child pages of the page identified by
   * <code>pageURI</code>.
   * <p>
   * The iteration only contains pages which are accessible by <code>user</code>
   * with respect to <code>permission</code>.
   * <p>
   * A <code>SecurityException</code> is thrown if the page identified by
   * <code>pageURI</code> is not accessible for reading.
   * 
   * @param pageURI
   *          uri of the parent page
   * @param user
   *          the user accessing the page
   * @param the
   *          permission needed
   * @return the child pages
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Iterator<Page> pages(PageURI pageURI, User user, Permission permission)
      throws SecurityException, IOException;

  /**
   * This method looks up pages whith the given keywords and types and returns
   * in maximum <code>count</code> page headers which are of one of the given
   * types contain at least one of the defined keywords and match the security
   * constraints defined by the permission set.
   * 
   * @param keywords
   *          the keywords to match
   * @param site
   *          the site to search
   * @param u
   *          the user that wants access to the page header
   * @param p
   *          the permission that is needed
   * @param v
   *          the page version
   * @param count
   *          the maximum number of results
   * @return the page headers matching the above criterias
   * @throws IOException
   *           if lookup fails due to a database error
   */
  Page[] findByKeyword(String[] keywords, User u, Permission p, long v,
      int count) throws IOException;

  /**
   * This method looks up pages whith the given keywords and types and returns
   * in maximum <code>count</code> page headers which are of one of the given
   * types contain at least one of the defined keywords and match the security
   * constraints defined by the permission set.
   * 
   * @param types
   *          the types to match
   * @param site
   *          the site to search
   * @param u
   *          the user that wants access to the page header
   * @param p
   *          the permission that is needed
   * @param v
   *          the page version
   * @param count
   *          the maximum number of results
   * @return the page headers matching the above criterias
   * @throws IOException
   *           if lookup fails due to a database error
   */
  Page[] findByType(String[] types, User u, Permission p, long v, int count)
      throws IOException;

  /**
   * This method looks up pages whith the given keywords and types and returns
   * in maximum <code>count</code> page headers which are of one of the given
   * types contain at least one of the defined keywords and match the security
   * constraints defined by the permission set.
   * 
   * @param types
   *          the types to match
   * @param keywords
   *          the keywords to match
   * @param site
   *          the site to search
   * @param u
   *          the user that wants access to the page header
   * @param p
   *          the permission that is needed
   * @param v
   *          the page version
   * @param count
   *          the maximum number of results
   * @return the page headers matching the above criterias
   * @throws IOException
   *           if lookup fails due to a database error
   */
  Page[] findByTypeAndKeyword(String[] types, String[] keywords, User u,
      Permission p, long v, int count) throws IOException;

  /**
   * This method looks up pages whith the given keywords and types and returns
   * in maximum <code>count</code> page headers which are of one of the given
   * types contain at least one of the defined keywords and match the security
   * constraints defined by the permission set.
   * 
   * @param types
   *          the types to match
   * @param keywords
   *          the keywords to match
   * @param site
   *          the site to search
   * @param u
   *          the user that wants access to the page header
   * @param p
   *          the permission that is needed
   * @param v
   *          the page version
   * @param count
   *          the maximum number of results
   * @return the page headers matching the above criterias
   * @throws IOException
   *           if lookup fails due to a database error
   */
  Page[] find(String[] types, String[] keywords, User u, Permission p, long v,
      int count) throws IOException;

}