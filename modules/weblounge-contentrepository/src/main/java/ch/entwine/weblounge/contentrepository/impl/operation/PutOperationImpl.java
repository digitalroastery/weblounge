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
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.PutOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;

import java.io.IOException;

/**
 * This operation implements a put of the given resource.
 */
public final class PutOperationImpl<C extends ResourceContent, R extends Resource<C>> extends AbstractContentRepositoryOperation<R> implements PutOperation<C, R> {

  /** The resource to be locked */
  private Resource<C> resource = null;

  /** Whether to update the preview as part of this put operation */
  private boolean updatePreviews = true;

  /**
   * Creates a new put operation for the given resource.
   * 
   * @param resource
   *          the resource
   * @param updatePreviews
   *          whether to update the resource's previews
   */
  public PutOperationImpl(Resource<C> resource, boolean updatePreviews) {
    this.resource = resource;
    this.updatePreviews = updatePreviews;
  }

  /**
   * Returns the resource.
   * 
   * @return the resource
   */
  public Resource<C> getResource() {
    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return resource.getURI();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#apply(ch.entwine.weblounge.common.content.Resource)
   */
  @SuppressWarnings("unchecked")
  public R apply(R resource) {
    if (!this.resource.equals(resource))
      return resource;
    return (R) this.resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.PutOperation#updatePreviews()
   */
  public boolean updatePreviews() {
    return updatePreviews;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected R run(WritableContentRepository repository)
      throws ContentRepositoryException, IOException {
    return (R) repository.put(resource, updatePreviews);
  }

}
