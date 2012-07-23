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
 * The put operation represents the process of writing a resource to the content
 * repository.
 */
public interface PutOperation extends ContentRepositoryResourceOperation<Resource<? extends ResourceContent>> {

  /**
   * Returns the resource that is to be stored.
   * 
   * @return the resource
   */
  Resource<? extends ResourceContent> getResource();

  /**
   * Returns <code>true</code> if this resource's previews need to be updated as
   * part of this operation.
   * 
   * @return <code>true</code> to update the previews
   */
  boolean updatePreviews();

}