/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.content.page;

import ch.entwine.weblounge.common.content.page.HTMLInclude.Use;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;

/**
 * This interface describes elements that are included in the &lt;head&gt;
 * section of an <code>HTML</code> page, namely &lt;link&gt; and &lt;script&gt;.
 */
public interface HTMLHeadElement {

  /**
   * Indicates the use of this include.
   * 
   * @return
   */
  Use getUse();

  /**
   * Specifies the usage of this element.
   * 
   * @param use
   *          the element use
   */
  void setUse(Use use);

  /**
   * Sets the associated site.
   * 
   * @param site
   *          the site
   */
  void setSite(Site site);

  /**
   * Sets the associated module.
   * 
   * @param module
   *          the module
   */
  void setModule(Module module);

  /**
   * Sets the system environment.
   * 
   * @param environment
   *          the system environment
   */
  void setEnvironment(Environment environment);

  /**
   * Returns the <code>XML</code> representation of this script.
   * 
   * @return the xml representation
   */
  String toXml();

  /**
   * Returns the <code>HTML</code> representation of this script.
   * 
   * @return
   */
  String toHtml();

}