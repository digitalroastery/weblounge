/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository;

import ch.entwine.weblounge.common.content.Resource;

/**
 * A resource listener can be implemented to follow the life cycle of a given
 * set of resources.
 */
public interface ResourceListener<T extends Resource<?>> {

  /**
   * Called when a resource has been created and added to the system.
   * 
   * @param resource
   *          the new resource
   */
  void created(T resource);

  /**
   * Called when a resource or its content has been updated.
   * 
   * @param resource
   *          the resource
   */
  void updated(T resource);

  /**
   * Called when a resource or (part of) its content has been deleted.
   * 
   * @param resource
   *          the resource
   */
  void published(T resource);

  /**
   * Called when a resource has been unpublished.
   * 
   * @param resource
   *          the resource
   */
  void unpublished(T resource);

  /**
   * Called when a resource has been published.
   * 
   * @param resource
   *          the resource
   */
  void deleted(T resource);

}
