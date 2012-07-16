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

import ch.entwine.weblounge.common.content.ResourceURI;

/**
 * Interface that identifies operations that modify a given resource.
 */
public interface ContentRepositoryResourceOperation<C, R, T> extends ContentRepositoryOperation<T> {

  /**
   * Returns the resource.
   * 
   * @return the resource
   */
  ResourceURI getResourceURI();

  /**
   * Applies this operation to the in-memory instance of <code>resource</code>
   * and returns the modified version.
   * 
   * @param resource
   *          the resource
   * @return the modified resource
   */
  R apply(R resource);

}