/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl;

/**
 * A <code>Lease</code> is an item that can be managed by a <code>Pool</code>.
 * 
 * TODO Replace with commons-pooling
 */
public interface Lease {

  /**
   * This method is called by the pool if the lease is given out. Then the lease
   * state is <code>leased</code>.
   */
  void leased();

  /**
   * This method is called by the pool if the lease has been returned to the
   * pool. The lease state is now <code>pooled</code>.
   */
  void returned();

  /**
   * This method is called by the pool if the lease has been retired. This is
   * especially the case if
   * <ul>
   * <li>The lease returned <code>true</code> when asked if it wants to be
   * disposed</li>
   * <li>The pool has been cleaned up and this lease was over the keep limit</li>
   * </ul>
   * The lease is no pool member from here on.
   */
  void retired();

  /**
   * This method should returnn <code>true</code> if the pool should forget
   * about this lease. This may be the case if the item has reached some
   * unusable state.
   * 
   * @return <code>true</code> if the item should be disposed
   */
  boolean dispose();

}