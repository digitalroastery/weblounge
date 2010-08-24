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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;
import ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract base implementation of a <code>WritableContentRepository</code>.
 */
public abstract class AbstractWritableContentRepository extends AbstractContentRepository implements WritableContentRepository {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractWritableContentRepository.class);

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public boolean delete(ResourceURI uri) throws ContentRepositoryException, IOException {
    return delete(uri, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI,
   *      boolean)
   */
  public boolean delete(ResourceURI uri, boolean allRevisions)
      throws ContentRepositoryException, IOException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // See if the resource exists
    if (!index.exists(uri)) {
      logger.warn("Resource '{}' not found in repository index", uri);
      return false;
    }

    // Make sure the resource is not being referenced elsewhere
    // TODO: Make this it's own index
    SearchQuery searchByResource = new SearchQueryImpl(site);
    searchByResource.withProperty(SolrFields.PAGELET_PROPERTIES, "resourceid");
    if (index.find(searchByResource).getItems().length > 0) {
      logger.warn("Resource '{}' is still being referenced", uri);
      return false;
    }

    // Get the revisions to delete
    long[] revisions = new long[] { uri.getVersion() };
    if (allRevisions) {
      revisions = index.getRevisions(uri);
    }

    deleteResource(uri, revisions);

    // Remove all resource revisions from the index
    for (long revision : revisions) {
      ResourceURI u = new ResourceURIImpl(uri, revision);
      index.delete(u);
    }

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#move(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.content.ResourceURI)
   */
  public void move(ResourceURI uri, ResourceURI target) throws IOException, ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    index.move(uri, target.getPath());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#put(ch.o2it.weblounge.common.content.Resource)
   */
  public Resource<? extends ResourceContent> put(
      Resource<? extends ResourceContent> resource)
      throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Add entry to index
    if (!index.exists(resource.getURI())) {
      if (resource.contents().size() > 0)
        throw new IllegalStateException("Cannot add content metadata without content");
      index.add(resource);
    } else {
      logger.debug("Checking content section of existing {} {}", resource.getType(), resource);
      Resource<?> r = get(resource.getURI());
      if (resource.contents().size() != r.contents().size())
        throw new IllegalStateException("Cannot modify content metadata without content");
      for (ResourceContent c : resource.contents()) {
        if (!c.equals(r.getContent(c.getLanguage())))
          throw new IllegalStateException("Cannot modify content metadata without content");
      }
      index.update(resource);
    }

    // Write the updated resource to disk
    storeResource(resource);

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#putContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.content.ResourceContent, java.io.InputStream)
   */
  @SuppressWarnings("unchecked")
  public <T extends ResourceContent> Resource<T> putContent(ResourceURI uri,
      T content, InputStream is) throws ContentRepositoryException,
      IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Make sure the resource exists
    if (!index.exists(uri))
      throw new IllegalStateException("Cannot add content to missing resource " + uri);
    Resource<T> resource = null;
    try {
      resource = (Resource<T>) get(uri);
      if (resource == null) {
        throw new IllegalStateException("Resource " + uri + " not found");
      }
    } catch (ClassCastException e) {
      logger.error("Trying to add content of type {} to incompatible resource", content.getClass());
      throw new IllegalStateException(e);
    }

    // Store the content and add entry to index
    resource.addContent(content);
    storeResourceContent(uri, content, is);
    storeResource(resource);
    index.update(resource);

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#deleteContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.content.ResourceContent)
   */
  @SuppressWarnings("unchecked")
  public <T extends ResourceContent> Resource<T> deleteContent(ResourceURI uri,
      T content) throws ContentRepositoryException, IOException,
      IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Make sure the resource exists
    if (!index.exists(uri))
      throw new IllegalStateException("Cannot remove content from missing resource " + uri);
    Resource<T> resource = null;
    try {
      resource = (Resource<T>) get(uri);
      if (resource == null) {
        throw new IllegalStateException("Resource " + uri + " not found");
      }
    } catch (ClassCastException e) {
      logger.error("Trying to remove content of type {} from incompatible resource", content.getClass());
      throw new IllegalStateException(e);
    }

    // Store the content and add entry to index
    resource.removeContent(content.getLanguage());
    deleteResourceContent(uri, content);
    storeResource(resource);
    index.update(resource);

    return resource;
  }

  /**
   * Writes a new resource to the repository storage.
   * 
   * @param resource
   *          the resource content
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  abstract protected void storeResource(
      Resource<? extends ResourceContent> resource) throws IOException;

  /**
   * Writes the resource content to the repository storage.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @param is
   *          the input stream
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  protected abstract <T extends ResourceContent> void storeResourceContent(
      ResourceURI uri, T content, InputStream is) throws IOException;

  /**
   * Deletes the indicated revisions of resource <code>uri</code> from the
   * repository. The concrete implementation is responsible for making the
   * deletion of multiple revisions safe, i. e. transactional.
   * 
   * @param uri
   *          the resource uri
   * @param revisions
   *          the revisions to remove
   */
  protected abstract void deleteResource(ResourceURI uri, long[] revisions)
      throws IOException;

  /**
   * Deletes the resource content from the repository storage.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  protected abstract <T extends ResourceContent> void deleteResourceContent(
      ResourceURI uri, T content) throws IOException;

}
