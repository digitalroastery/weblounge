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
import ch.entwine.weblounge.common.content.repository.DeleteContentOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;

import java.io.IOException;

/**
 * This operation implements a removal of content from the given resource.
 */
public final class DeleteContentOperationImpl<T extends ResourceContent> extends AbstractContentRepositoryOperation<Resource<T>> implements DeleteContentOperation<T> {

  /** The resource to be locked */
  private ResourceURI uri = null;

  /** The resource content */
  private T content = null;

  /**
   * Creates a new delete content operation for the given resource.
   * 
   * @param uri
   *          the resource
   * @param content
   *          the resource content
   */
  public DeleteContentOperationImpl(ResourceURI uri, T content) {
    this.uri = uri;
    this.content = content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.PutContentOperation#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.PutContentOperation#getContent()
   */
  public T getContent() {
    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  @Override
  protected Resource<T> run(WritableContentRepository repository)
      throws ContentRepositoryException, IOException, IllegalStateException {
    return repository.deleteContent(uri, content);
  }

}
