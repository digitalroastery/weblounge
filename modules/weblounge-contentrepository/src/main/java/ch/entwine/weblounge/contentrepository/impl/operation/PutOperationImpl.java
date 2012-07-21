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

import static ch.entwine.weblounge.common.content.ResourceUtils.equalsByIdOrPathAndVersion;

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
public final class PutOperationImpl extends AbstractContentRepositoryOperation<Resource<? extends ResourceContent>> implements PutOperation {

  /** The resource to be locked */
  private Resource<?> resource = null;

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
  public PutOperationImpl(Resource<?> resource, boolean updatePreviews) {
    this.resource = resource;
    this.updatePreviews = updatePreviews;
  }

  /**
   * Returns the resource.
   * 
   * @return the resource
   */
  public Resource<? extends ResourceContent> getResource() {
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
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#apply(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.Resource)
   */
  @SuppressWarnings("unchecked")
  public <C extends ResourceContent, R extends Resource<C>> R apply(
      ResourceURI uri, R resource) {

    // Is this a resource creation...
    if (resource == null) {
      if (equalsByIdOrPathAndVersion(uri, this.resource.getURI()))
        return (R) this.resource;
      return null;
    }

    // or simply an update (needs to be handled separately because of resource
    // that could have been moved around, resulting in a different path)?
    if (!equalsByIdOrPathAndVersion(this.resource.getURI(), resource.getURI()))
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
  @Override
  protected Resource<? extends ResourceContent> run(
      WritableContentRepository repository) throws ContentRepositoryException,
      IOException {
    return repository.put(resource, updatePreviews);
  }

}
