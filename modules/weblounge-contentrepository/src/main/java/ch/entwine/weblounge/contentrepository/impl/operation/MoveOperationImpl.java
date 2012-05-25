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

import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.MoveOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;

import java.io.IOException;

/**
 * This operation implements a move of the given resource.
 */
public final class MoveOperationImpl<T extends ResourceContent> extends AbstractContentRepositoryOperation<Void> implements MoveOperation {

  /** Path where the resource will be moved to */
  private String moveTo = null;

  /** True if all child resources should be moved as well */
  private boolean moveChildren = true;

  /** The uri of the resource that the operation will work on */
  private ResourceURI uri = null;

  /**
   * Creates a new put operation for the given resource.
   * 
   * @param uri
   *          the resource's uri
   * @param targetPath
   *          path that the resource should be moved to
   * @param moveChildren
   *          <code>true</code> to move children as well
   */
  public MoveOperationImpl(ResourceURI uri, String targetPath,
      boolean moveChildren) {
    this.uri = uri;
    this.moveTo = targetPath;
    this.moveTo = targetPath;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.MoveOperation#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.MoveOperation#getTargetPath()
   */
  public String getTargetPath() {
    return moveTo;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.MoveOperation#moveChildren()
   */
  public boolean moveChildren() {
    return moveChildren;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  public Void run(WritableContentRepository repository) throws ContentRepositoryException, IOException {
    repository.move(uri, moveTo, moveChildren);
    return null;
  }

}
