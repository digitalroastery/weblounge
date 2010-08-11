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
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;

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
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.user.User)
   */
  public boolean delete(ResourceURI uri, User user) throws SecurityException,
      IOException {
    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    return delete(uri, user, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.user.User, boolean)
   */
  public boolean delete(ResourceURI uri, User user, boolean allRevisions)
      throws SecurityException, IOException {
    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    // See if the resource exists
    long[] revisions = index.getRevisions(uri);
    if (revisions == null) {
      logger.warn("Resource '{}' not found in repository index", uri);
      return false;
    }

    // TODO: Check permissions
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
   *      ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.user.User)
   */
  public boolean move(ResourceURI uri, ResourceURI target, User user)
      throws SecurityException, IOException {
    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    // Update the index
    index.move(uri, target.getPath());

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#put(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.content.resource.Resource)
   */
  public Resource<? extends ResourceContent> put(Resource<? extends ResourceContent> resource, User user) throws SecurityException,
      IOException {
    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    // TODO: Check permission

    storeResource(resource);

    // Add entry to index
    if (!index.exists(resource.getURI()))
      index.add(resource);
    else
      index.update(resource);

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#putContent(ch.o2it.weblounge.common.content.Resource,
   *      java.io.InputStream, ch.o2it.weblounge.common.user.User)
   */
  public void putContent(Resource<? extends ResourceContent> resource, InputStream is, User user)
      throws SecurityException, IOException {

    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    // TODO: Check permission

    storeResourceContent(resource, is);

    // Add entry to index
    index.add(resource);
  }

  /**
   * Writes a new resource to the repository storage.
   * 
   * @param resource
   *          the resource content
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  abstract protected void storeResource(Resource<? extends ResourceContent> resource) throws IOException;

  /**
   * Writes a new resource to the repository storage.
   * 
   * @param resource
   *          the resource content
   * @param is
   *          the input stream
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  abstract protected void storeResourceContent(Resource<? extends ResourceContent> resource, InputStream is)
      throws IOException;

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

}
