/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository.impl.operation;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.LockOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.security.User;

import java.io.IOException;

/**
 * This operation implements a lock on the given resource.
 */
public final class LockOperationImpl<T extends ResourceContent> extends AbstractContentRepositoryOperation<Resource<T>> implements LockOperation<T> {

  /** The potential lock owner */
  private User user = null;

  /** The resource to be locked */
  private Resource<T> resource = null;

  /**
   * Creates a new locking operation for the given resource.
   * 
   * @param resource
   *          the resource
   * @param user
   *          the potential lock owner
   */
  public LockOperationImpl(Resource<T> resource, User user) {
    this.resource = resource;
    this.user = user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.LockOperation#getResource()
   */
  public Resource<T> getResource() {
    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.LockOperation#getUser()
   */
  public User getUser() {
    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  public Resource<T> run(WritableContentRepository repository) throws ContentRepositoryException, IOException {
    return repository.lock(resource.getURI(), user);
  }

}
