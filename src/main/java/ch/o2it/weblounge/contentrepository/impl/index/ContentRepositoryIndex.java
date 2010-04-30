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

package ch.o2it.weblounge.contentrepository.impl.index;

import ch.o2it.weblounge.common.content.PageURI;

import java.util.Iterator;

/**
 * This index into the repository is used to map page and resource urls into
 * repository indices and vice versa. In addition, it will facilitate listing
 * url hierarchies.
 */
public class ContentRepositoryIndex {

  /**
   * Adds all relevant entries for the given page uri to the index.
   * 
   * @param uri
   *          the page uri
   */
  public void add(PageURI uri) {
    // TODO Auto-generated method stub
  }

  /**
   * Removes all entries for the given page uri from the index.
   * 
   * @param uri
   *          the page uri
   */
  public void delete(PageURI uri) {
    // TODO Auto-generated method stub
  }

  /**
   * Returns all revisions for the specified page or <code>null</code> if the
   * page doesn't exist.
   * 
   * @param uri
   *          the page uri
   * @return the revisions
   */
  public long[] getRevisions(PageURI uri) {
    return null;
  }

  /**
   * Returns the identifier of the page with uri <code>uri</code> or
   * <code>null</code> if the uri is not part of the index.
   * 
   * @param uri
   *          the uri
   * @return the id
   * @throws IllegalArgumentException
   *           if the uri does not contain a path
   */
  public String toId(PageURI uri) {
    if (uri.getPath() == null)
      throw new IllegalArgumentException("PageURI must contain a path");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Updates the path of the given page uri.
   * 
   * @param uri
   *          the uri
   * @param path
   *          the page
   */
  public void update(PageURI uri) {
    // TODO Auto-generated method stub

  }

  /**
   * Removes all entries from the index. 
   */
  public void clear() {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param uri
   * @return
   */
  public boolean exists(PageURI uri) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @param uri
   * @param level
   * @param versions
   * @return
   */
  public Iterator<PageURI> list(PageURI uri, int level, long[] versions) {
    // TODO Auto-generated method stub
    return null;
  }

}
