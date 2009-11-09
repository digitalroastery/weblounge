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

import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.Restriction;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;

/**
 * This class models the security constraints that apply to an arbitrary object
 * in the system.
 * <p>
 * A security context definition contains information on permissions and roles
 * that are needed to obtain them. The context usually looks like follows:
 * 
 * <pre>
 * 		&lt;security&gt;
 * 			&lt;owner&gt;tobias.wunden&lt;/owner&gt;
 * 			&lt;restriction id=&quot;system:read&quot; evaluate=&quot;allow,deny&quot;&gt;
 * 				&lt;allow type=&quot;ch.o2it.weblounge.api.security.Authorization&quot;&gt;main&lt;/allow&gt;
 * 				&lt;allow type=&quot;ch.o2it.weblounge.api.security.Role&quot;&gt;system:editor&lt;/allow&gt;
 * 				&lt;allow type=&quot;ch.o2it.weblounge.api.security.User&quot;&gt;tobias.wunden&lt;/allow&gt;
 * 				&lt;deny&gt;all&lt;/deny&gt;
 * 			&lt;/restriction&gt;
 * 		&lt;/security&gt;
 * </pre>
 */
public class RestrictionSecurityContext extends AbstractSecurityContext {

  /** Restrictions */
  private Map<Permission, Restriction> securityCtxt_;

  /** The permissions */
  private Permission[] permissions_;

  /** the class name, used for the logging facility */
  private final static String className = RestrictionSecurityContext.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a default restriction set with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   */
  public RestrictionSecurityContext() {
    this(null);
  }

  /**
   * Creates a default restriction set with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   * 
   * @param owner
   *          the secured object owner
   */
  public RestrictionSecurityContext(User owner) {
    super(owner);
    securityCtxt_ = new HashMap<Permission, Restriction>();
  }

  /**
   * Adds <code>authority</code> to the authorized authorities regarding the
   * given permission. If the permission has no restrictions defined so far, a
   * new restriction is being created which is evaluated
   * <code>allow,deny</code>.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the item that is allowed to obtain the permission
   */
  public void allow(Permission permission, Authority authority) {
    Arguments.checkNull(permission, "permission");
    Arguments.checkNull(authority, "authorization");
    log_.debug("Security context '" + this + "' requires '" + authority + "' for permission '" + permission + "'");
    Restriction r = securityCtxt_.get(permission);
    if (r == null) {
      r = new RestrictionImpl();
      r.setEvaluationOrder(Restriction.ALLOW_DENY);
      securityCtxt_.put(permission, r);
      permissions_ = null;
    }
    r.allow(authority);
  }

  /**
   * Allows everyone and everything regarding permission <code>permission</code>
   * .
   * 
   * @param permission
   *          the permission
   */
  public void allowAll(Permission permission) {
    Restriction r = securityCtxt_.get(permission);
    if (r == null) {
      r = new RestrictionImpl();
      r.setEvaluationOrder(Restriction.ALLOW_DENY);
      securityCtxt_.put(permission, r);
      permissions_ = null;
    }
    r.allowAll();
  }

  /**
   * Adds <code>authority</code> to the denied authorities regarding the given
   * permission. If the permission has no restrictions defined so far, a new
   * restriction is being created which is evaluated <code>deny,allow</code>.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the authorization to deny
   */
  public void deny(Permission permission, Authority authority) {
    Restriction r = securityCtxt_.get(permission);
    if (r == null) {
      r = new RestrictionImpl();
      r.setEvaluationOrder(Restriction.DENY_ALLOW);
      securityCtxt_.put(permission, r);
      permissions_ = null;
    }
    r.deny(authority);
  }

  /**
   * Denies everyone and everything regarding permission <code>permission</code>
   * .
   * 
   * @param permission
   *          the permission
   */
  public void denyAll(Permission permission) {
    Restriction r = securityCtxt_.get(permission);
    if (r == null) {
      r = new RestrictionImpl();
      r.setEvaluationOrder(Restriction.DENY_ALLOW);
      securityCtxt_.put(permission, r);
      permissions_ = null;
    }
    r.denyAll();
  }

  /**
   * Checks whether the roles that the caller currently owns satisfy the
   * constraints of this context ion the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorization
   *          the object claiming the permission
   * @return <code>true</code> if the item may obtain the permission
   */
  public boolean check(Permission permission, Authority authorization) {
    Arguments.checkNull(permission, "permission");
    Arguments.checkNull(authorization, "authorization");
    log_.debug("Request to check permission '" + permission + "' for authorization '" + authorization + "' at " + this);

    Restriction authorizationSet = securityCtxt_.get(permission);
    if (authorizationSet != null) {
      boolean check = authorizationSet.check(authorization);
      log_.debug("Check for ('" + authorization + "', '" + permission + "') " + ((check) ? "succeeded" : "failed") + " for context " + this);
      return check;
    }
    log_.warn(authorization + " asked for permission '" + permission + "' which is not defined in security context " + this);
    return false;
  }

  /**
   * Returns <code>true</code> if the object <code>o</code> is allowed to act on
   * the secured object in a way that satisfies the given permissionset
   * <code>p</code>.
   * 
   * @param permissions
   *          the required set of permissions
   * @param authorization
   *          the object claiming the permissions
   * @return <code>true</code> if the object may obtain the permissions
   */
  public boolean check(PermissionSet permissions, Authority authorization) {
    Arguments.checkNull(permissions, "permissions");
    Arguments.checkNull(authorization, "authorization");
    log_.debug("Request to check permissionset for authorization '" + authorization + "' at " + this);
    return checkOneOf(permissions, authorization) && checkAllOf(permissions, authorization);
  }

  /**
   * Returns <code>true</code> if the authorization is sufficient to obtain the
   * "oneof" permission set.
   * 
   * @param p
   *          the permission set
   * @param authorization
   *          the authorization to check
   * @return <code>true</code> if the user has one of the permissions
   */
  protected boolean checkOneOf(PermissionSet p, Authority authorization) {
    Permission[] permissions = p.some();
    for (int i = 0; i < permissions.length; i++) {
      if (check(permissions[i], authorization)) {
        return true;
      }
    }
    return (permissions.length == 0) || false;
  }

  /**
   * Returns <code>true</code> if the authorization is sufficient to obtain the
   * "allof" permission set.
   * 
   * @param p
   *          the permission set
   * @param authorization
   *          the authorization to check
   * @return <code>true</code> if the user has all of the permissions
   */
  protected boolean checkAllOf(PermissionSet p, Authority authorization) {
    Permission[] permissions = p.all();
    for (int i = 0; i < permissions.length; i++) {
      if (!check(permissions[i], authorization)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the permissions that are defined in this security context.
   * 
   * @return the permissions
   */
  public Permission[] permissions() {
    if (permissions_ == null) {
      permissions_ = new Permission[securityCtxt_.size()];
      securityCtxt_.keySet().toArray(permissions_);
    }
    return permissions_;
  }

  /**
   * Returns the authorization that is required for <code>permission</code>.
   * 
   * @return the required role
   */
  public Authority getAuthorization(Permission permission) {
    return (Authority) securityCtxt_.get(permission);
  }

  /**
   * Returns the roles that are required for the given <code>permissions</code>.
   * 
   * @return the required roles
   */
  public Authority[] getAuthorization(Permission[] permissions) {
    Set<Role> roles = new HashSet<Role>();
    for (int i = 0; i < permissions.length; i++) {
      Role r = (Role) securityCtxt_.get(permissions[i]);
      if (r != null)
        roles.add(r);
    }
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * Initializes this context from an xml node.
   * 
   * @param context
   *          the security context node
   */
  public void init(XPath path, Node context) {
    securityCtxt_.clear();
    permissions_ = null;

    NodeList restrictions = XPathHelper.selectList(context, "restriction", path);
    for (int i = 0; i < restrictions.getLength(); i++) {
      Node restrictionNode = restrictions.item(i);
      Permission p = new PermissionImpl(XPathHelper.valueOf(restrictionNode, "@id", path));
      Restriction restriction = new RestrictionImpl(path, restrictionNode);
      securityCtxt_.put(p, restriction);
    }
  }

  /**
   * Serializes this restriction set.
   * 
   * @return the serialized form of this restriction set
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<security>");

    // Owner
    if (owner != null) {
      b.append("<owner>");
      b.append(owner.getLogin());
      b.append("</owner>");
    }

    // Permissions
    for (Permission p : securityCtxt_.keySet()) {
      Restriction r = securityCtxt_.get(p);
      b.append(r.toXml());
    }
    b.append("</security>");
    return b.toString();
  }

  /**
   * Returns all authorities that are allowed with respect to permission
   * <code>p</code>.
   * 
   * @see ch.o2it.weblounge.common.security.SecurityContext#getAllowed(ch.o2it.weblounge.common.security.Permission)
   */
  public Authority[] getAllowed(Permission p) {
    Restriction r = securityCtxt_.get(p);
    if (r != null) {
      return r.getAllowed();
    } else {
      return new Authority[] {};
    }
  }

  /**
   * Returns all authorities that are denied with respect to permission
   * <code>p</code>.
   * 
   * @see ch.o2it.weblounge.common.security.SecurityContext#getDenied(ch.o2it.weblounge.common.security.Permission)
   */
  public Authority[] getDenied(Permission p) {
    Restriction r = securityCtxt_.get(p);
    if (r != null) {
      return r.getDenied();
    } else {
      return new Authority[] {};
    }
  }

}