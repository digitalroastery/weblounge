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
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.PutContentOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;

import java.io.IOException;
import java.io.InputStream;

/**
 * This operation implements a put of content to the given resource.
 */
public final class PutContentOperationImpl extends AbstractContentRepositoryOperation<Resource<? extends ResourceContent>> implements PutContentOperation {

  /** The resource to be locked */
  private ResourceURI uri = null;

  /** The resource content */
  private ResourceContent content = null;

  /** The data stream */
  private InputStream inputStream = null;

  /**
   * Creates a new put operation for the given resource.
   * 
   * @param uri
   *          the resource
   * @param content
   *          the resource content
   * @param is
   *          the input stream
   */
  public PutContentOperationImpl(ResourceURI uri, ResourceContent content,
      InputStream is) {
    this.uri = uri;
    this.content = content;
    this.inputStream = is;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation#apply(ResourceURI,
   *      Resource)
   */
  @SuppressWarnings("unchecked")
  public <C extends ResourceContent, R extends Resource<C>> R apply(
      ResourceURI uri, R resource) {
    if (resource == null)
      return null;
    if (!ResourceUtils.equalsByIdOrPathAndVersion(this.uri, resource.getURI()))
      return resource;
    resource.addContent((C) content);
    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.PutContentOperation#getContent()
   */
  public ResourceContent getContent() {
    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.PutContentOperation#getInputStream()
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  @Override
  protected Resource<? extends ResourceContent> run(
      WritableContentRepository repository) throws ContentRepositoryException,
      IOException, IllegalStateException {
    return repository.putContent(uri, content, inputStream);
  }

}
