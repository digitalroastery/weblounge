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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.security.AccessControlEntry;
import ch.entwine.weblounge.common.security.AccessControlList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A list of access control list entries.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "acl", namespace = "ch.entwine.weblounge.security")
@XmlRootElement(name = "acl", namespace = "ch.entwine.weblounge.security")
public final class AccessControlListImpl implements AccessControlList {

  /** The list of access control entries */
  @XmlElement(name = "ace")
  private List<AccessControlEntry> entries = null;

  /**
   * Courtesy of JAXB
   */
  public AccessControlListImpl() {
    this.entries = new ArrayList<AccessControlEntry>();
  }

  /**
   * Creates an access control list with <code>entries</code> as initial
   * entries.
   * 
   * @param entries
   *          the initial entries
   */
  public AccessControlListImpl(AccessControlEntry... entries) {
    this.entries = new ArrayList<AccessControlEntry>();
    if (entries != null)
      this.entries.addAll(Arrays.asList(entries));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.AccessControlList#getEntries()
   */
  public List<AccessControlEntry> getEntries() {
    return entries;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return entries.toString();
  }

}
