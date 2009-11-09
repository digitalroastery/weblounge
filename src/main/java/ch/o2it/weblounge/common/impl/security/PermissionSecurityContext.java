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
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

/**
 * This class models the security constraints that apply to an arbitrary object
 * in the system.
 * <p>
 * A security context definition contains information on permissions and roles
 * or users that are needed to obtain them. The context usually looks like
 * follows:
 * 
 * <pre>
 * 		&lt;security&gt;
 * 			&lt;owner&gt;tobias.wunden&lt;/owner&gt;
 * 			&lt;permission id=&quot;system:publish&quot; type=&quot;role&quot;&gt;system:publisher&lt;/permission&gt;
 * 			&lt;permission id=&quot;system:write&quot; type=&quot;user&quot;&gt;tobias.wunden&lt;/permission&gt;
 * 		&lt;/security&gt;
 * </pre>
 */
public class PermissionSecurityContext extends AbstractSecurityContext {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(PermissionSecurityContext.class);

  /** Allowed authorizations */
  private Map<Permission, Set<Authority>> context_ = null;

  /** Allowed default authorizations */
  private Map<Permission, Set<Authority>> defaultContext_ = null;

  /** The permissions */
  private Permission[] permissions_ = null;

  /**
   * Creates a default restriction set with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   */
  public PermissionSecurityContext() {
    this(null);
  }

  /**
   * Creates a default restriction set with the given name and initially no
   * restrictions.
   * 
   * @param owner
   *          the secured object owner
   */
  public PermissionSecurityContext(User owner) {
    super(owner);
    context_ = new HashMap<Permission, Set<Authority>>();
    defaultContext_ = new HashMap<Permission, Set<Authority>>();
  }

  /**
   * Adds <code>authority</code> to the authorized authorities regarding the
   * given permission.
   * <p>
   * <b>Note:</b> Calling this method replaces any default authorities on the
   * given permission, so if you want to keep them, add them here explicitly.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the item that is allowed to obtain the permission
   */
  public void allow(Permission permission, Authority authority) {
    Arguments.checkNull(permission, "permission");
    Arguments.checkNull(authority, "authority");
    log_.debug("Security context '" + this + "' requires '" + authority + "' for permission '" + permission + "'");

    Set<Authority> a = context_.get(permission);
    if (a == null) {
      a = new HashSet<Authority>();
      context_.put(permission, a);
      permissions_ = null;
    }
    a.add(authority);
    defaultContext_.remove(permission);
  }

  /**
   * Adds <code>authority</code> to the default authorized authorities regarding
   * the given permission. Default authorities will not be stored in the
   * database, thus saving lots of space and speeding things up.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the item that is allowed to obtain the permission
   */
  public void allowDefault(Permission permission, Authority authority) {
    Arguments.checkNull(permission, "permission");
    Arguments.checkNull(authority, "authority");
    log_.debug("Security context '" + this + "' requires '" + authority + "' for permission '" + permission + "'");
    Set<Authority> a = defaultContext_.get(permission);
    if (a == null) {
      a = new HashSet<Authority>();
      defaultContext_.put(permission, a);
      permissions_ = null;
    }
    a.add(authority);
  }

  /**
   * Removes <code>authority</code> from the denied authorities regarding the
   * given permission. This method will remove the authority from both the
   * explicitly allowed and the default authorities.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the authorization to deny
   */
  public void deny(Permission permission, Authority authority) {
    Arguments.checkNull(permission, "permission");
    Arguments.checkNull(authority, "authority");
    log_.debug("Security context '" + this + "' requires '" + authority + "' for permission '" + permission + "'");

    deny(permission, authority, context_);
    deny(permission, authority, defaultContext_);
  }

  /**
   * Removes <code>authority</code> from the denied authorities found in
   * <code>context</code> regarding the given permission.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the authorization to deny
   * @param context
   *          the authorities context
   */
  private void deny(Permission permission, Authority authority, Map context) {
    HashSet authorities = (HashSet) context.get(permission);

    // If the authorities have been found, iterate over them to find a matching
    // authority. We have to do this instead of directly calling
    // authorities.remove(authority)
    // because the hashmap may contain AuthorityImpl instances which will equal
    // a matching role (after casting them to an authority) but not v. v.
    if (authorities != null) {
      Iterator i = authorities.iterator();
      while (i.hasNext()) {
        Authority a = (Authority) i.next();
        if (a.equals(authority)) {
          authorities.remove(a);
          return;
        }
      }
      if (authorities.size() == 0) {
        context.remove(permission);
      }
    }
  }

  /**
   * Denies everyone and everything regarding permission <code>permission</code>
   * .
   * 
   * @param permission
   *          the permission
   */
  public void denyAll(Permission permission) {
    denyAll(permission, context_);
    denyAll(permission, defaultContext_);
  }

  /**
   * Denies everyone and everything regarding permission <code>permission</code>
   * in the specified context.
   * 
   * @param permission
   *          the permission
   * @param context
   *          the context
   */
  private void denyAll(Permission permission, Map context) {
    HashSet a = (HashSet) context.get(permission);
    if (a != null) {
      a.clear();
    }
  }

  /**
   * Denies everyone and everything.
   */
  public void denyAll() {
    context_.clear();
    defaultContext_.clear();
  }

  /**
   * Checks whether the roles that the caller currently owns satisfy the
   * constraints of this context ion the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authority
   *          the object claiming the permission
   * @return <code>true</code> if the item may obtain the permission
   */
  public boolean check(Permission permission, Authority authority) {
    Arguments.checkNull(permission, "permission");
    Arguments.checkNull(authority, "authority");
    log_.debug("Request to check permission '" + permission + "' for authority '" + authority + "' at " + this);

    return check(permission, authority, defaultContext_) || check(permission, authority, context_);
  }

  /**
   * Checks whether the roles that the caller currently owns satisfy the
   * constraints of the given context regarding the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authority
   *          the object claiming the permission
   * @param context
   *          the context
   * @return <code>true</code> if the item may obtain the permission
   */
  private boolean check(Permission permission, Authority authority, Map context) {
    HashSet authorities = (HashSet) context.get(permission);
    if (authorities != null) {
      Iterator i = authorities.iterator();
      while (i.hasNext()) {
        Authority a = (Authority) i.next();
        if (authority.equals(a))
          return true;
      }
      return false;
    }
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
   * Returns the authorities that are explicitly allowed by the context.
   * 
   * @see ch.o2it.weblounge.common.security.SecurityContext#getAllowed(ch.o2it.weblounge.common.security.Permission)
   */
  public Authority[] getAllowed(Permission p) {
    Set<Authority> authorities = defaultContext_.get(p);
    if (authorities == null) {
      authorities = context_.get(p);
    }
    if (authorities != null) {
      Authority[] a = new Authority[authorities.size()];
      return authorities.toArray(a);
    } else {
      return new Authority[] {};
    }
  }

  /**
   * Returns all authorities that are explicitly denied by this security
   * context. Since this context only defines allowed items, the returned array
   * will always be empty.
   * 
   * @see ch.o2it.weblounge.common.security.SecurityContext#getDenied(ch.o2it.weblounge.common.security.Permission)
   */
  public Authority[] getDenied(Permission p) {
    return new Authority[] {};
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
      permissions_ = new Permission[context_.size() + defaultContext_.size()];
      List<Permission> permissionList = new ArrayList<Permission>();
      permissionList.addAll(context_.keySet());
      permissionList.addAll(defaultContext_.keySet());
      permissionList.toArray(permissions_);
    }
    return permissions_;
  }

  /**
   * Initializes this context from an xml node.
   * 
   * @param context
   *          the security context node
   * @param path
   *          the XPath object used to parse the configuration
   */
  public void init(XPath path, Node context) {
    context_.clear();
    permissions_ = null;

    // Read permissions
    NodeList permissions = XPathHelper.selectList(context, "//security/permission", path);
    for (int i = 0; i < permissions.getLength(); i++) {
      Node p = permissions.item(i);
      String id = XPathHelper.valueOf(p, "@id", path);
      Permission permission = new PermissionImpl(id);

      // Authority name
      String require = XPathHelper.valueOf(p, "text()", path);
      if (require == null) {
        continue;
      }

      // Authority type
      String type = XPathHelper.valueOf(p, "@type", path);

      // Check for multiple authorities
      StringTokenizer tok = new StringTokenizer(require, " ,;");
      while (tok.hasMoreTokens()) {
        String authorityId = tok.nextToken();
        Authority authority = new AuthorityImpl(resolveAuthorityTypeShortcut(type), authorityId);
        allow(permission, authority);
      }

    }
  }

  /**
   * Serializes this security context.
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
    Iterator constraints = context_.keySet().iterator();
    while (constraints.hasNext()) {
      Permission p = (Permission) constraints.next();
      Map<String, Set<Authority>> authorities = groupByType(context_.get(p));
      Iterator types = authorities.keySet().iterator();
      while (types.hasNext()) {
        String type = (String) types.next();
        b.append("<permission id=\"");
        b.append(p.getContext() + ":" + p.getIdentifier());
        b.append("\" type=\"");
        b.append(getAuthorityTypeShortcut(type));
        b.append("\">");
        boolean first = true;
        for (Authority authority : authorities.get(type)) {
          if (!first) {
            b.append(",");
          }
          b.append(authority.getAuthorityId());
          first = false;
        }
        b.append("</permission>");
      }
    }
    b.append("</security>");
    return b.toString();
  }

  /**
   * Returns the authorities grouped by their types.
   * 
   * @param authorities
   *          the authorities hash set
   * @return the grouped authorities
   */
  private Map<String, Set<Authority>> groupByType(Set<Authority> authorities) {
    Map<String, Set<Authority>> types = new HashMap<String, Set<Authority>>();
    for (Authority a : authorities) {
      Set<Authority> al = types.get(a.getAuthorityType());
      if (al == null) {
        al = new HashSet<Authority>();
        types.put(a.getAuthorityType(), al);
      }
      al.add(a);
    }
    return types;
  }

  /**
   * Returns a copy of this security context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    PermissionSecurityContext ctxt = new PermissionSecurityContext(owner);
    ctxt.context_.putAll(context_);
    ctxt.defaultContext_.putAll(defaultContext_);
    ctxt.permissions_ = permissions_;
    ctxt.owner = owner;
    return ctxt;
  }

}