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

import java.io.File;

import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;

/**
 * The renderer configuration stores configuration information about a certain
 * renderer instance, e. g. a <code>JSPRenderer</code> which is part of a
 * <code>RendererBundle</code>.
 * <p>
 * Since renderers are created on demand using the <code>newInstance()
 * </code> mechanism, this class
 * allows easy configuration by just passing it to the <code>init()</code>
 * method of the new renderer. There, it will act as a delegate to answer
 * configuration questions.
 */
public interface RendererConfiguration extends Customizable, Localizable {

  /**
   * Returns the renderer identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Returns the supported rendering methods. The meaning of methods is the
   * possible output format of a renderer. Therefore, the methods usually
   * include <tt>html</tt>, <tt>pdf</tt> and so on.
   * 
   * @return the supported methods
   */
  String[] methods();

  /**
   * Returns <code>true</code> if the given method is supported by the renderer.
   * The method is used to lookup a rendering method for a given renderer id.
   * 
   * @param method
   *          the method name
   * @return <code>true</code> if the renderer supports the rendering method
   */
  boolean provides(String method);

  /**
   * Returns the file used to locate the concrete jsp or xsl file.
   * 
   * @return the file
   */
  File getRenderer();

  /**
   * Returns the url used to get the concrete jsp or xsl file.
   * 
   * @return the url
   */
  String getUrl();

  /**
   * Returns the class name in case of a custom renderer.
   * 
   * @return the class name
   */
  String getClassName();

  /**
   * Returns <code>true</code> if this renderer is composeable.
   * 
   * @return <code>true</code> if this renderer is composeable
   */
  boolean isComposeable();

  /**
   * Returns the time that this renderer is considered to be valid. However, the
   * validity should be rechecked after the time that
   * <code>getRecheckTime</code> returns.
   * 
   * @return the valid time
   */
  long getValidTime();

  /**
   * Returns the time that this renderer will be considered valid without
   * rechecking.
   * 
   * @return the recheck time
   */
  long getRecheckTime();

  /**
   * Returns the path to the editor for this renderer.
   * 
   * @return the editor
   */
  File getEditor();

  /**
   * Returns the pagelet extension class name used to support rendering and
   * editing of this pagelet.
   * 
   * @return the pagelet's extension class name
   */
  String getExtension();

  /**
   * Returns the title in the given language or, if it doesn't exist in that
   * language, in the site default language.
   * 
   * @param language
   *          the language
   * @return the title in the given language
   */
  String getTitle(Language language);

  /**
   * Returns the &lt;link&gt; elements that have been defined for this renderer.
   * 
   * @return the links
   */
  Include[] getLinks();

  /**
   * Returns the &lt;script&gt; elements that have been defined for this
   * renderer.
   * 
   * @return the scripts
   */
  ScriptInclude[] getScripts();

}