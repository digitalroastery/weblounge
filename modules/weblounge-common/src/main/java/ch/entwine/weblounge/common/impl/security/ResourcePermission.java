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

import ch.entwine.weblounge.common.security.Securable;

import java.security.Permission;

/**
 * TODO: Comment ResourcePermission
 */
public class ResourcePermission extends Permission {

  /** Serial version UID */
  private static final long serialVersionUID = 1073544616602445043L;
  
  /** The action that is to be executed on the resource */
  private String action = null;
  
  /** The securable */
  private Securable resource = null;

  /**
   * Creates a new resource permission with the given name.
   * 
   * @param name
   *          the permission name
   * @param action
   *          the action that is to be taken
   * @param resource
   *          the resource that is to be accessed
   */
  public ResourcePermission(String name, String action, Securable resource) {
    super(name);
    this.action = action;
    if (resource == null)
      throw new IllegalArgumentException("Resource cannot be null");
    this.resource = resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Permission#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ResourcePermission) {
      ResourcePermission p = (ResourcePermission)obj;
      return getName().equals(p.getName())
        && (resource.equals(p.resource))
        && ((action == null && p.action == null) || action.equals(p.action));
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Permission#getActions()
   */
  @Override
  public String getActions() {
    return action;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Permission#hashCode()
   */
  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Permission#implies(java.security.Permission)
   */
  @Override
  public boolean implies(Permission permission) {
    // TODO: Finish implementation
    return false;
  }

}
