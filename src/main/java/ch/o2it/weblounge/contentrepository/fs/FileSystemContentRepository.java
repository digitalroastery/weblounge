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

package ch.o2it.weblounge.contentrepository.fs;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;

import java.io.File;
import java.util.Dictionary;
import java.util.Iterator;

/**
 * Implementation of a content repository that lives on a filesystem.
 */
public class FileSystemContentRepository implements ContentRepository {

  /** The repository root directory */
  protected File repositoryRoot = null;
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#connect(java.util.Dictionary)
   */
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#disconnect()
   */
  public void disconnect() throws ContentRepositoryException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#exists(ch.o2it.weblounge.common.content.PageURI)
   */
  public boolean exists(PageURI uri) throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#exists(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)
   */
  public boolean exists(PageURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#findPages(ch.o2it.weblounge.common.content.SearchQuery)
   */
  public SearchResult[] findPages(SearchQuery query)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getPage(ch.o2it.weblounge.common.content.PageURI)
   */
  public Page getPage(PageURI uri) throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getPage(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)
   */
  public Page getPage(PageURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getVersions(ch.o2it.weblounge.common.content.PageURI)
   */
  public PageURI[] getVersions(PageURI uri) throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#listPages()
   */
  public Iterator<PageURI> listPages() throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#listPages(long[])
   */
  public Iterator<PageURI> listPages(long[] versions)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI)
   */
  public Iterator<PageURI> listPages(PageURI uri)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI, long[])
   */
  public Iterator<PageURI> listPages(PageURI uri, long[] versions)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI, int)
   */
  public Iterator<PageURI> listPages(PageURI uri, int level)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI, int, long[])
   */
  public Iterator<PageURI> listPages(PageURI uri, int level, long[] versions)
      throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#setURI(java.lang.String)
   */
  public void setURI(String repositoryURI) {
    // TODO Auto-generated method stub

  }

}
