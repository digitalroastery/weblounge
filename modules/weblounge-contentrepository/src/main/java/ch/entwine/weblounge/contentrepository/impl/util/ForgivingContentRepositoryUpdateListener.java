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

package ch.entwine.weblounge.contentrepository.impl.util;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.repository.AsynchronousContentRepositoryListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This update listener will be used if the operation is supposed to be
 * asynchronous, but the caller is not interested in the result.
 */
public class ForgivingContentRepositoryUpdateListener implements AsynchronousContentRepositoryListener {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ForgivingContentRepositoryUpdateListener.class);

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.AsynchronousContentRepositoryListener#resourceUpdated(ch.entwine.weblounge.common.content.Resource)
   */
  public void resourceUpdated(Resource<?> resource) {
    logger.debug("Resource {} successfully updated");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.AsynchronousContentRepositoryListener#resourceUpdateFailed(ch.entwine.weblounge.common.content.Resource,
   *      java.lang.Throwable)
   */
  public void resourceUpdateFailed(Resource<?> resource, Throwable t) {
    logger.debug("Resource {} update failed: {}", resource, t.getMessage());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.AsynchronousContentRepositoryListener#resourceContentUpdated(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  public void resourceContentUpdated(Resource<?> resource,
      ResourceContent content) {
    logger.debug("Resource content {} successfully updated");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.AsynchronousContentRepositoryListener#resourceContentUpdateFailed(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.lang.Throwable)
   */
  public void resourceContentUpdateFailed(Resource<?> resource,
      ResourceContent content, Throwable t) {
    logger.debug("Resource content {} update failed: {}", resource, t.getMessage());
  }

}
