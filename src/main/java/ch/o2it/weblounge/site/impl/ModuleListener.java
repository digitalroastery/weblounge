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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.site.Module;

/**
 * This interface defines the methods that have to be implemented by a
 * <code>ModuleListener</code>. The listener will be informed about module
 * relative events like e. g. a module shutdown.
 */
public interface ModuleListener {

  /**
   * Callback for listeners telling them that module <code>module</code> just
   * started.
   * 
   * @param module
   *          the started module
   */
  void moduleStarted(Module module);

  /**
   * Method used to inform listeners about an upcoming module shutdown.
   * 
   * @param module
   *          the module that is going to shut down
   */
  void moduleStopped(Module module);

}