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

package ch.o2it.weblounge.contentrepository.impl.util;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Iterator;

/**
 * This is a placeholder implementation for sites that have no content
 * repository associated with them. The implementation will not return any
 * content. In addition, it's not writable, so nothing can be written to it.
 */
public class EmptyContentRepository implements ContentRepository {

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#connect(ch.o2it.weblounge.common.site.Site,
   *      java.util.Dictionary)
   */
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#disconnect()
   */
  public void disconnect() throws ContentRepositoryException {
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#start()
   */
  public void start() throws ContentRepositoryException {
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#stop()
   */
  public void stop() throws ContentRepositoryException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#find(ch.o2it.weblounge.common.content.SearchQuery)
   */
  public SearchResult find(SearchQuery query)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public Resource<?> get(ResourceURI uri) throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.language.Language)
   */
  public InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#getLanguages(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public ResourceURI[] getVersions(ResourceURI uri) throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public Iterator<ResourceURI> list(ResourceURI uri)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      long)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, long version)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      int)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      int, long)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level, long version)
      throws ContentRepositoryException {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#setURI(java.lang.String)
   */
  public void setURI(String repositoryURI) {
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getURI()
   */
  public String getURI() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getResourceCount()
   */
  public long getResourceCount() {
    return 0;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getVersionCount()
   */
  public long getVersionCount() {
    return 0;
  }

}
