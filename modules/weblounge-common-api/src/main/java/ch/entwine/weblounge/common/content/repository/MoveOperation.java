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


/**
 * The move operation represents the process of a resource (and potentially its
 * children) being moved from one path or url to another.
 */
public interface MoveOperation extends ContentRepositoryResourceOperation<Void> {

  /**
   * Returns the path that the resource should be moved to.
   * 
   * @return the target path
   */
  String getTargetPath();

  /**
   * Returns <code>true</code> if the resource's children should be moved as
   * well.
   * 
   * @return <code>true</code> to move child resources as well
   */
  boolean moveChildren();

}