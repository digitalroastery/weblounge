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

package ch.o2it.weblounge.common.impl.util.registry;

/**
 * @author Tobias Wunden
 * @version Jun 5, 2003
 * 
 *          Interface definition for objects that want to listen to changes in a
 *          <code>Registry</code>.
 */

public interface RegistryListener {

  /**
   * Callback for <code>RegistryListener</code>s that is called by the observed
   * regsitry if the content changed. That is, either an entry has been added or
   * removed from the registry.
   * 
   * @param e
   *          the event containing information about what has changed
   */
  void registryChanged(RegistryEvent e);

}