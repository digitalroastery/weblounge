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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.PageInclude;

import java.util.List;

/**
 * The action configuration stores configuration information about an action
 * handler instance. The configuration data is being read from the
 * <code>&lt;action&gt;</code> section of the <code>module.xml</code>.
 */
public interface ActionConfiguration extends Customizable {

  /**
   * Returns the action identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Returns the supported content flavors. The meaning of flavors are the
   * possible output formats of an action. Common flavors include include
   * <code>HTML</code>, <code>XML</code> or <code>JSON</code>.
   * 
   * @return the supported content flavors
   */
  List<String> getFlavors();

  /**
   * Returns the mountpoint used to call the action. The mountpoint is
   * interpreted relative to the site root.
   * <p>
   * The extension can either be empty, <code>/*</code> or <code>/**</code>,
   * depending on whether to match only the mountpoint (e. g. <code>/news</code>
   * ), to match the mountpoint and any direct children (e. g.
   * <code>/news/today</code>) or the mountpoint and any subsequent urls.
   * 
   * @return the action mountpoint
   */
  String getMountpoint();

  /**
   * Returns the class name in case of a custom action.
   * 
   * @return the class name
   */
  Class<? extends Action> getActionClass();

  /**
   * Returns the url that is being used to render this action.
   * 
   * @return the target path
   */
  String getPageURI();

  /**
   * Returns the identifier of the template that is used to render this action.
   * 
   * @return the template identifier
   */
  String getTemplate();

  /**
   * Returns the amount of time in milliseconds that the output of this action
   * will be considered valid. This value is used by the cache to throw away
   * outdated content.
   * 
   * @return the valid time of the action output in milliseconds.
   */
  long getValidTime();

  /**
   * Returns the amount of time in milliseconds that the output of this action
   * will be considered valid. After this time, clients will be advised to
   * recheck whether the output they may have cached is still valid.
   * 
   * @return the recheck time of the action output in milliseconds.
   */
  long getRecheckTime();

  /**
   * Returns the &lt;link&gt; and &lt;script&gt; elements that have been defined
   * for this action.
   * 
   * @return the includes
   */
  List<PageInclude> getIncludes();

  /**
   * Returns the action name in one or more languages.
   * 
   * @return the action name
   */
  Localizable getName();

}