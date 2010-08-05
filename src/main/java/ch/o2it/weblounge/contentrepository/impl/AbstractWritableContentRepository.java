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

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

    // See if the page exists
    long[] revisions = index.getRevisions(uri);
    if (revisions == null) {
      logger.warn("Page '{}' not found in repository index", uri);
      return false;
    }

    // TODO: Check permissions
    deletePage(uri, revisions);

    // Remove all page revisions from the index
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
   *      ch.o2it.weblounge.common.content.page.Page)
   */
  public Page put(ResourceURI uri, Page page, User user) throws SecurityException,
      IOException {
    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    if (index.exists(uri))
      throw new IllegalStateException("Attempt to overwrite existing page " + uri);

    // TODO: Check permission

    storePage(uri, page);

    // Add entry to index
    index.add(page);

    return page;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#update(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.user.User)
   */
  public boolean update(ResourceURI uri, Page page, User user)
      throws SecurityException, IOException {
    if (!connected)
      throw new IllegalStateException("Content repository is not connected");

    if (!index.exists(uri))
      throw new IllegalStateException("Attempt to update a non-existing page " + uri);

    // TDOO: Check permissions

    updatePage(uri, page);

    return true;
  }

  /**
   * Writes a new page to the repository storage.
   * 
   * @param uri
   *          the page uri
   * @param page
   *          the page content
   * @throws IOException
   *           if the page can't be written to the storage
   */
  abstract protected void storePage(ResourceURI uri, Page page) throws IOException;

  /**
   * Updates an existing page in the repository storage.
   * 
   * @param uri
   *          the page uri
   * @param page
   *          the page content
   * @throws IOException
   *           if the page can't be written to the storage
   */
  abstract protected void updatePage(ResourceURI uri, Page page) throws IOException;

  /**
   * Deletes the indicated revisions of page <code>uri</code> from the
   * repository. The concrete implementation is responsible for making the
   * deletion of multiple revisions safe, i. e. transactional.
   * 
   * @param uri
   *          the page uri
   * @param revisions
   *          the revisions to remove
   */
  protected abstract void deletePage(ResourceURI uri, long[] revisions) throws IOException;

}
