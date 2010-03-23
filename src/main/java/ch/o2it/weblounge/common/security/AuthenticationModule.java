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

import ch.o2it.weblounge.common.Customizable;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;

/**
 * An authentication module defines an authentication facility that can be
 * activated and deactivated by configuration means.
 */
public interface AuthenticationModule extends Customizable {
  
  /** The relevance values for authentication modules */
  public enum Relevance { required, requisite, sufficient, optional };

  /**
   * Returns the name of the module's implementing class.
   * 
   * @return the module class name
   */
  String getModuleClass();

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
  Relevance getRelevance();

  /**
   * Returns an <code>XML</code> representation of the authentication module,
   * which will look similar to the following example:
   * 
   * <pre>
   * &lt;loginmodule class="ch.o2it.weblounge.MyLoginModule" relevance="sufficient"/&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>AuthenticationModule</code> from the serialized output of this
   * method.
   * 
   * @return the <code>XML</code> representation of the authentication module
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  String toXml();

}