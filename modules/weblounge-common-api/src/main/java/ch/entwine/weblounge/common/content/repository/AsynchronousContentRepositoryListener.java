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

package ch.entwine.weblounge.common.content.repository;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;

/**
 * TODO: Comment AsynchronousContentRepositoryListener
 */
public interface AsynchronousContentRepositoryListener {

  /**
   * This method is called when the resource update was successful.
   * 
   * @param resource
   *          the resource
   */
  void resourceUpdated(Resource<?> resource);

  /**
   * This method is called when the resource update failed.
   * 
   * @param resource
   *          the resource
   * @param t
   *          the reason of failure
   */
  void resourceUpdateFailed(Resource<?> resource, Throwable t);

  /**
   * This method is called when the resource content update was successful.
   * 
   * @param resource
   *          the resource
   * @param content
   *          the resource content
   */
  void resourceContentUpdated(Resource<?> resource, ResourceContent content);

  /**
   * This method is called when the resource content update failed.
   * 
   * @param resource
   *          the resource
   * @param content
   *          the resource content
   * @param t
   *          the reason of failure
   */
  void resourceContentUpdateFailed(Resource<?> resource,
      ResourceContent content, Throwable t);

}
