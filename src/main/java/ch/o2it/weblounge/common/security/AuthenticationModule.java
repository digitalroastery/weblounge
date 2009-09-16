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

package ch.o2it.weblounge.common.security;

import java.util.List;
import java.util.Map;

/**
 * TODO: Comment AuthenticationModule
 */
public interface AuthenticationModule {

  /**
   * Returns the name of the module's implementing class.
   * 
   * @return the module class name
   */
  String getModuleClass();

  /**
   * Returns configuration options for this login module.
   * 
   * @return the configuration options
   */
  Map<String, List<String>> getOptions();

  /**
   * Returns the module's relevance. Please see the JAAS documentation on the
   * different relevance values:
   * <ul>
   * <li>required</li>
   * <li>requisite</li>
   * <li>sufficient</li>
   * <li>optional</li>
   * </ul>
   * 
   * @return the module's relevance_
   */
  String getRelevance();

}