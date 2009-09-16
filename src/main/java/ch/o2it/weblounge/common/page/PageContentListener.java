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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.language.Language;

/**
 * This interface defines the methods for listeners interested in page content
 * changes.
 */
public interface PageContentListener {

  /**
   * Notifies the listener that the pagelet has been created.
   * 
   * @param source
   *          the created pagelet
   * @param language
   *          the initial pagelet language
   */
  void pageletAdded(Pagelet source, Language language);

  /**
   * Notifies the listener that the pagelet has been moved.
   * 
   * @param source
   *          the moved pagelet
   */
  void pageletMoved(Pagelet source);

  /**
   * Notifies the listener that the pagelet has been removed.
   * 
   * @param source
   *          the removed pagelet
   */
  void pageletRemoved(Pagelet source);

  /**
   * Notifies the listener that something in the pagelet has changed.
   * 
   * @param source
   *          the pagelet
   * @param language
   *          the changed pagelet language version
   */
  void pageletChanged(Pagelet source, Language language);

  /**
   * Notifies the listener that the pagelet has been translated to the given
   * language.
   * 
   * @param source
   *          the pagelet
   * @param language
   *          the new pagelet language version
   */
  void pageletTranslated(Pagelet source, Language language);

}