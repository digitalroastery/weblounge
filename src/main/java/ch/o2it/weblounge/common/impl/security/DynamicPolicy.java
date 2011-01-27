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

package ch.o2it.weblounge.common.impl.security;

import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;

/**
 * TODO: Comment XMLPolicy
 */
public class DynamicPolicy extends Policy {

  /** The original JAAS policy */
  private Policy deferredPolicy = null;

  /**
   * Creates a new custom policy, backed by <code>deferredPolicy</code> in order
   * to gather permissions defined by the system.
   * 
   * @param deferredPolicy
   *          the original policy
   */
  public DynamicPolicy(Policy deferredPolicy) {
    this.deferredPolicy = deferredPolicy;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Policy#getPermissions(java.security.CodeSource)
   */
  @Override
  public PermissionCollection getPermissions(CodeSource codesource) {
    return deferredPolicy.getPermissions(codesource);
  }

  /**
   * {@inheritDoc}
   * @see java.security.Policy#getPermissions(java.security.ProtectionDomain)
   */
  @Override
  public PermissionCollection getPermissions(ProtectionDomain domain) {
    PermissionCollection permissions = deferredPolicy.getPermissions(domain);
    Principal[] principals = domain.getPrincipals();
    for (@SuppressWarnings("unused") Principal principal : principals) {
      // TODO: Translate custom/well-known principals into permissions
    }
    return permissions;
  }
  
  /**
   * {@inheritDoc}
   * @see java.security.Policy#refresh()
   */
  @Override
  public void refresh() {
    deferredPolicy.refresh();
  }

}