/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.pool;

/**
 * This interface is intended for pool observers that wish to track the pool's
 * size.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public interface PoolListener {

  /**
   * This method is called whenever the pool size has changed. It notifies the
   * listener about the amount as well as the current pool size.
   * 
   * @param pool
   *          the source of this event
   * @param amount
   *          the increase (positive) or decrease (negative)
   * @param size
   *          the current pool size
   */
  void poolSizeChanged(Pool pool, int amount, int size);

}