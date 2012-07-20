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

import static ch.entwine.weblounge.common.url.UrlUtils.isExtendedPrefix;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.MoveOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * This operation implements a move of the given resource.
 */
public final class MoveOperationImpl extends AbstractContentRepositoryOperation<Void> implements MoveOperation {

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
    this.moveChildren = moveChildren;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#getResourceURI()
   */
  @Override
  public ResourceURI getResourceURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#apply(ResourceURI, Resource)
   */
  @Override
  public <C extends ResourceContent, R extends Resource<C>> R apply(ResourceURI uri, R resource) {
    if (resource == null)
      return null;
    if (resource.getPath() == null && !ResourceUtils.equalsByIdOrPathAndVersion(this.uri, resource.getURI()))
      return resource;
    else if (resource.getPath() == null || !ResourceUtils.equalsByIdOrPathAndVersion(this.uri, resource.getURI())) {
      resource.setPath(moveTo);
      return resource;
    } else if (moveChildren && isExtendedPrefix(this.uri.getPath(), resource.getPath())) {
      String originalPathPrefix = this.uri.getPath();
      String originalPath = resource.getPath();
      String pathSuffix = originalPath.substring(originalPathPrefix.length());
      String newPath = null;

      // Is the original path just a prefix, or is it an exact match?
      if (StringUtils.isNotBlank(pathSuffix))
        newPath = UrlUtils.concat(moveTo, pathSuffix);
      else
        newPath = moveTo;

      resource.setPath(newPath);
    }
    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.MoveOperation#getTargetPath()
   */
  @Override
  public String getTargetPath() {
    return moveTo;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.MoveOperation#moveChildren()
   */
  @Override
  public boolean moveChildren() {
    return moveChildren;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.operation.AbstractContentRepositoryOperation#run(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  @Override
  protected Void run(WritableContentRepository repository)
      throws ContentRepositoryException, IOException, IllegalStateException {
    repository.move(uri, moveTo, moveChildren);
    return null;
  }

}
