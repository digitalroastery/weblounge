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

package ch.entwine.weblounge.contentrepository.impl.util;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is a place holder implementation for sites that have no content
 * repository associated with them. The implementation will not return any
 * content. In addition, it's not writable, so nothing can be written to it.
 */
public class EmptyContentRepository implements ContentRepository {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
  public void connect(Site site) throws ContentRepositoryException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#disconnect()
   */
  public void disconnect() throws ContentRepositoryException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getType()
   */
  public String getType() {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#isReadOnly()
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#isIndexing()
   */
  public boolean isIndexing() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#exists(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#existsInAnyVersion(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean existsInAnyVersion(ResourceURI uri)
      throws ContentRepositoryException {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getResourceURI(java.lang.String)
   */
  public ResourceURI getResourceURI(String resourceId)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#find(ch.entwine.weblounge.common.content.SearchQuery)
   */
  public SearchResult find(SearchQuery query) throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#suggest(java.lang.String,
   *      java.lang.String, int)
   */
  public List<String> suggest(String dictionary, String seed, int count)
      throws ContentRepositoryException {
    return new ArrayList<String>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public Resource<? extends ResourceContent> get(ResourceURI uri)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public Iterator<ResourceURI> list(ResourceURI uri)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI,
   *      long)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, long version)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI,
   *      int)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI,
   *      int, long)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level, long version)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getResourceCount()
   */
  public long getResourceCount() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getVersionCount()
   */
  public long getVersionCount() {
    return 0;
  }

}
