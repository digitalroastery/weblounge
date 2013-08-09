/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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
package ch.entwine.weblounge.common.repository;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;

/**
 * A <code>ContentRepositoryListener</code> will, after adding to a content
 * repository instance, report modifications in a content repository.
 */
public interface ContentRepositoryListener {

  /**
   * Notifies the listener about a new resource which was added to the content
   * repository.
   * 
   * @param resource
   *          the newly added resource
   */
  void resourceAdded(Resource<?> resource);

  /**
   * Notifies the listener about a new resource content which was added to the
   * content repository.
   * 
   * @param resource
   *          the resource to which the content belongs to
   * @param content
   *          the newly added resource content
   */
  void resourceContentAdded(ResourceURI resource, ResourceContent content);

  /**
   * Notifies the listener about a resource which got updated.
   * 
   * @param resource
   *          the resource which got updated
   */
  void resourceUpdated(Resource<?> resource);

  /**
   * Notifies the listener about a resource content which got updated.
   * 
   * @param resource
   *          the resource, whom's content got updated
   * @param content
   *          the updated resource content
   */
  void resourceContentUpdated(ResourceURI resource, ResourceContent content);

  /**
   * Notifies the listener about a resource which got deleted.
   * 
   * @param resource
   *          the deleted resource
   */
  void resourceDeleted(ResourceURI resource);

  /**
   * Notifies the listener about a resource content which got deleted.
   * 
   * @param resource
   *          the resource, the content belonged to
   * @param content
   *          the deleted resource content
   */
  void resourceContentDeleted(ResourceURI resource, ResourceContent content);

}
