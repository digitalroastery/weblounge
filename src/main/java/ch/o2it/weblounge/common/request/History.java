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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.url.WebUrl;

/**
 * The history object tracks the user's movement and can be used to reload the
 * last url, step back etc. A history object is attached to every session and
 * can be retrieved by calling <code>RequestSupport.getHistory()</code>.
 * 
 * TODO Do we still need this?
 */
public interface History {

  /**
   * Adds a navigation entry to this history object. If the history features
   * more then the maximum number of possible entries, then the least recent
   * entry is discarded.
   * 
   * @param url
   *          the entry
   */
  void addEntry(WebUrl url);

  /**
   * Returns an entry from the navigation history. Note that the entry with the
   * lowest possible index <code>0</code> is the most recent.<br>
   * If no entry can be found for the given index, then the root page is
   * returned as the url.
   * 
   * @return an entry from the navigation history
   */
  WebUrl getMove(int step);

  /**
   * Return the most recent entry in the history table.
   * 
   * @return the most recent entry
   */
  WebUrl getLatestMove();

  /**
   * Removes the most recent move from the navigation history.
   */
  void discardLatestMove();

  /**
   * Removes the <code>count</code> most recent moves from the navigation
   * history.
   * 
   * @param count
   *          remove this number of entries
   */
  void discardLatestMoves(int count);

  /**
   * Returns the current number of entries in this history.
   * 
   * @return the number of history entries
   */
  int size();

}