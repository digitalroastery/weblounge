/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.SystemAction;

import java.security.BasicPermission;
import java.security.Permission;

/**
 * A permission object for a {@link Resource} that can be accessed using a
 * specific {@link Action}.
 */
public final class ResourcePermission extends BasicPermission {

  /** Serial version UID */
  private static final long serialVersionUID = 4306908026063283597L;

  /** The resource */
  private final Resource<?> securable;

  /** The action that is about to be performed */
  private final Action action;

  /**
   * Creates a new permission object that can be used to validate the permission
   * to execute <code>action</code> on <code>resource</code>.
   * 
   * @param resource
   *          the resource to be accessed
   * @param action
   *          the action to be performed
   */
  public ResourcePermission(Resource<?> resource, Action action) {
    super("Resource " + action.getIdentifier() + " permission");
    this.securable = resource;
    this.action = action;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.security.Permission#getActions()
   */
  @Override
  public String getActions() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.security.Permission#implies(java.security.Permission)
   */
  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof ResourcePermission))
      return false;

    ResourcePermission pp = (ResourcePermission) p;
    Action impliedAction = pp.getAction();

    // Write action contains read
    if (SystemAction.WRITE.equals(action)) {
      return SystemAction.READ.equals(impliedAction);
    }

    // TODO Finalize implementation of implied roles

    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.security.Permission#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object p) {
    if (!(p instanceof ResourcePermission))
      return false;
    ResourcePermission pp = (ResourcePermission) p;
    return securable.equals(pp.securable) && action.equals(pp.action);
  }

  /**
   * {@inheritDoc}
   *
   * @see java.security.Permission#hashCode()
   */
  @Override
  public int hashCode() {
    return securable.getURI().hashCode();
  }

  /**
   * Returns the resource that is being accessed.
   * 
   * @return the resource
   */
  public Resource<?> getResource() {
    return securable;
  }

  /**
   * Returns the action that is to be applied to the resource.
   * 
   * @return the action
   */
  public Action getAction() {
    return action;
  }

}
