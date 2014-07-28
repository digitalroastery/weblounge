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

import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Securable;
import ch.entwine.weblounge.common.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
public class SecurityContextImpl extends AbstractSecurityContext implements Securable, Cloneable {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SecurityContextImpl.class);

  /** Allowed authorizations */
  private Map<Action, Set<Authority>> aclAllow = null;

  /** Denied authorizations */
  private Map<Action, Set<Authority>> aclDeny = null;

  /** Default allowed authorizations */
  private Map<Action, Set<Authority>> aclAllowDefaults = null;

  /** Default denied authorizations */
  private Map<Action, Set<Authority>> aclDenyDefaults = null;

  /** Order in which to evaluate allow and deny rules */
  protected Order evaluationOrder = Order.AllowDeny;

  /** The actions */
  private Action[] actions = null;

  /**
   * Creates a default restriction set with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   */
  public SecurityContextImpl() {
    this(null);
  }

  /**
   * Creates a default restriction set with the given name and initially no
   * restrictions.
   * 
   * @param owner
   *          the secured object owner
   */
  public SecurityContextImpl(User owner) {
    super(owner);
    aclAllow = new HashMap<Action, Set<Authority>>();
    aclAllowDefaults = new HashMap<Action, Set<Authority>>();
    aclDeny = new HashMap<Action, Set<Authority>>();
    aclDenyDefaults = new HashMap<Action, Set<Authority>>();
  }

  /**
   * Sets the order in which to evaluate allow and deny access rules.
   * 
   * @param order
   *          the evaluation order
   * @throws IllegalArgumentException
   *           if <code>order</code> is <code>null</code>
   */
  public void setAllowDenyOrder(Order order) {
    if (order == null)
      throw new IllegalArgumentException("Order must not be null");
    this.evaluationOrder = order;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  @Override
  public Order getAllowDenyOrder() {
    return evaluationOrder;
  }

  /**
   * Adds <code>authority</code> to the authorized authorities regarding the
   * given permission.
   * <p>
   * <b>Note:</b> Calling this method replaces any default authorities on the
   * given permission, so if you want to keep them, add them here explicitly.
   * 
   * @param action
   *          the permission
   * @param authority
   *          the item that is allowed to obtain the permission
   */
  public void allow(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Permission cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Security context '{}' requires '{}' for permission '{}'", new Object[] {
        this,
        authority,
        action });

    Set<Authority> authorities = aclAllow.get(action);
    if (authorities == null) {
      authorities = new HashSet<Authority>();
      aclAllow.put(action, authorities);
    }
    authorities.add(authority);
    aclAllowDefaults.remove(action);
    actions = null;
  }

  /**
   * Allows to action on this object in all ways defined by {@link #actions()}
   * by any authority.
   */
  public void allowAll() {
    for (Action action : actions()) {
      allow(action, ANY_AUTHORITY);
    }
  }

  /**
   * Denies everyone and everything regarding action <code>action</code> .
   * 
   * @param action
   *          the action
   */
  public void allowAll(Action action) {
    allow(action, ANY_AUTHORITY);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean isAllowed(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action must not be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority must not be null");

    // Check if the action is explicitly authorized
    Authority[] authorities = getAllowed(action);
    for (Authority a : authorities) {
      if (authority.matches(a) || authority.implies(a)) {
        return true;
      }
    }

    // No?
    return false;
  }

  /**
   * Adds <code>authority</code> to the authorities that are allowed access by
   * default regarding the given action.
   * <p>
   * Note that default authorities will not be serialized as part of the
   * security context, thus saving space and speeding things up.
   * 
   * @param action
   *          the permission
   * @param authority
   *          the item that is allowed
   */
  public void allowDefault(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Security context '{}' requires '{}' for action '{}'", new Object[] {
        this,
        authority,
        action });
    Set<Authority> a = aclAllowDefaults.get(action);
    if (a == null) {
      a = new HashSet<Authority>();
      aclAllowDefaults.put(action, a);
    }
    a.add(authority);
    actions = null;
  }

  /**
   * Removes <code>authority</code> from the denied authorities regarding the
   * given action. This method will remove the authority from both the
   * explicitly allowed and the default authorities.
   * 
   * @param action
   *          the action
   * @param authority
   *          the authorization to deny
   */
  public void deny(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action must not be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority must not be null");
    logger.debug("Security context '{}' requires '{}' for action '{}'", new Object[] {
        this,
        authority,
        action });

    Set<Authority> authorities = aclDeny.get(action);
    if (authorities == null) {
      authorities = new HashSet<Authority>();
      aclDeny.put(action, authorities);
    }
    authorities.add(authority);
    aclDenyDefaults.remove(action);
    actions = null;
  }

  /**
   * Adds <code>authority</code> to the authorities that are denied access by
   * default regarding the given action.
   * <p>
   * Note that default authorities will not be serialized as part of the
   * security context, thus saving space and speeding things up.
   * 
   * @param action
   *          the permission
   * @param authority
   *          the item that is denied the action
   */
  public void denyDefault(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Security context '{}' denies '{}' for action '{}'", new Object[] {
        this,
        authority,
        action });
    Set<Authority> a = aclDenyDefaults.get(action);
    if (a == null) {
      a = new HashSet<Authority>();
      aclDenyDefaults.put(action, a);
    }
    a.add(authority);
    actions = null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean isDenied(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action must not be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority must not be null");

    // Check if the action is explicitly denied
    Authority[] authorities = getDenied(action);
    for (Authority a : authorities) {
      if (authority.matches(a)) {
        return true;
      }
    }

    // No?
    return false;
  }

  /**
   * Denies everyone and everything regarding action <code>action</code> .
   * 
   * @param action
   *          the action
   */
  public void denyAll(Action action) {
    deny(action, ANY_AUTHORITY);
  }

  /**
   * Denies everyone and everything.
   */
  public void denyAll() {
    for (Action action : actions()) {
      deny(action, ANY_AUTHORITY);
    }
  }

  /**
   * Returns the authorities that are explicitly allowed by the context.
   * 
   * @see ch.entwine.weblounge.common.security.SecurityContext#getAllowed(ch.entwine.weblounge.common.security.Action)
   */
  public Authority[] getAllowed(Action p) {
    Set<Authority> authorities = aclAllowDefaults.get(p);
    if (authorities == null) {
      authorities = aclAllow.get(p);
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
   * context.
   * 
   * @see ch.entwine.weblounge.common.security.SecurityContext#getDenied(ch.entwine.weblounge.common.security.Action)
   */
  public Authority[] getDenied(Action p) {
    Set<Authority> authorities = aclDenyDefaults.get(p);
    if (authorities == null) {
      authorities = aclDeny.get(p);
    }
    if (authorities != null) {
      Authority[] a = new Authority[authorities.size()];
      return authorities.toArray(a);
    } else {
      return new Authority[] {};
    }
  }

  /**
   * Returns the actions that are defined in this security context.
   * 
   * @return the actions
   */
  public Action[] actions() {
    if (actions == null) {
      List<Action> permissionList = new ArrayList<Action>();
      permissionList.addAll(aclAllow.keySet());
      permissionList.addAll(aclAllowDefaults.keySet());
      permissionList.addAll(aclDeny.keySet());
      permissionList.addAll(aclDenyDefaults.keySet());
      actions = permissionList.toArray(new Action[permissionList.size()]);
    }
    return actions;
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param context
   *          the publish context node
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static SecurityContextImpl fromXml(Node context)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param context
   *          the publish context node
   * @param xpath
   *          the xpath processor
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #toXml()
   */
  public static SecurityContextImpl fromXml(Node context, XPath xpath)
      throws IllegalStateException {

    Node contextRoot = XPathHelper.select(context, "/security", xpath);
    if (contextRoot == null)
      return new SecurityContextImpl();
    
    SecurityContextImpl securityCtx = new SecurityContextImpl();
    securityCtx.aclAllow.clear();
    securityCtx.aclDeny.clear();

    // Owner
    Node ownerNode = XPathHelper.select(contextRoot, "owner/user");
    if (ownerNode != null) {
      String login = XPathHelper.valueOf(ownerNode, "@id", xpath);
      if (login == null)
        throw new IllegalStateException("Found owner node without id");
      String realm = XPathHelper.valueOf(ownerNode, "@realm", xpath);
      String name = XPathHelper.valueOf(ownerNode, "text()", xpath);
      securityCtx.setOwner(new UserImpl(login, realm, name));
    }

    // Order
    String evaluationOrder = XPathHelper.valueOf(contextRoot, "acl/@order", xpath);
    if ("deny,allow".equals(evaluationOrder))
      securityCtx.setAllowDenyOrder(Securable.Order.DenyAllow);
    else
      securityCtx.setAllowDenyOrder(Securable.Order.AllowDeny);

    // Read allow permissions
    NodeList allows = XPathHelper.selectList(contextRoot, "acl/allow", xpath);
    for (int i = 0; i < allows.getLength(); i++) {
      Node p = allows.item(i);
      String id = XPathHelper.valueOf(p, "@id", xpath);
      Action action = new ActionImpl(id);

      // Authority name
      String require = XPathHelper.valueOf(p, "text()", xpath);
      if (require == null) {
        continue;
      }

      // Authority type
      String type = XPathHelper.valueOf(p, "@type", xpath);

      // Check for multiple authorities
      StringTokenizer tok = new StringTokenizer(require, " ,;");
      while (tok.hasMoreTokens()) {
        String authorityId = tok.nextToken();
        Authority authority = new AuthorityImpl(resolveAuthorityTypeShortcut(type), authorityId);
        securityCtx.allow(action, authority);
      }

    }

    // Read allow permissions
    NodeList denies = XPathHelper.selectList(contextRoot, "acl/deny", xpath);
    for (int i = 0; i < denies.getLength(); i++) {
      Node p = denies.item(i);
      String id = XPathHelper.valueOf(p, "@id", xpath);
      Action action = new ActionImpl(id);

      // Authority name
      String require = XPathHelper.valueOf(p, "text()", xpath);
      if (require == null) {
        continue;
      }

      // Authority type
      String type = XPathHelper.valueOf(p, "@type", xpath);

      // Check for multiple authorities
      StringTokenizer tok = new StringTokenizer(require, " ,;");
      while (tok.hasMoreTokens()) {
        String authorityId = tok.nextToken();
        Authority authority = new AuthorityImpl(resolveAuthorityTypeShortcut(type), authorityId);
        securityCtx.deny(action, authority);
      }

    }
    
    return securityCtx;
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

      // id
      b.append(" id=\"");
      b.append(owner.getLogin());
      b.append("\"");

      // realm
      if (owner.getRealm() != null) {
        b.append(" realm=\"");
        b.append(owner.getRealm());
        b.append("\"");
      }

      b.append(">");

      // name
      if (owner.getName() != null) {
        b.append("<![CDATA[").append(owner.getName()).append("]]>");
      }

      b.append("</owner>");
    }

    // Access control
    if (aclAllow.size() > 0 || aclDeny.size() > 0) {

      switch (evaluationOrder) {
        case AllowDeny:
          b.append("<acl order=\"allow,deny\">");
          break;
        case DenyAllow:
          b.append("<acl order=\"deny,allow\">");
          break;
        default:
          break;
      }

      // Allows
      SortedSet<Action> aclAllowList = new TreeSet<Action>(aclAllow.keySet());
      for (Action p : aclAllowList) {
        Map<String, Set<Authority>> authorities = groupByType(aclAllow.get(p));
        for (Map.Entry<String, Set<Authority>> entry : authorities.entrySet()) {
          String type = entry.getKey();
          b.append("<allow id=\"");
          b.append(p.getContext() + ":" + p.getIdentifier());
          b.append("\" type=\"");
          b.append(getAuthorityTypeShortcut(type));
          b.append("\">");
          boolean first = true;
          for (Authority authority : entry.getValue()) {
            if (!first) {
              b.append(",");
            }
            b.append(authority.getAuthorityId());
            first = false;
          }
          b.append("</allow>");
        }
      }

      // Allows
      SortedSet<Action> aclDenyList = new TreeSet<Action>(aclDeny.keySet());
      for (Action p : aclDenyList) {
        Map<String, Set<Authority>> authorities = groupByType(aclDeny.get(p));
        for (Map.Entry<String, Set<Authority>> entry : authorities.entrySet()) {
          String type = entry.getKey();
          b.append("<deny id=\"");
          b.append(p.getContext() + ":" + p.getIdentifier());
          b.append("\" type=\"");
          b.append(getAuthorityTypeShortcut(type));
          b.append("\">");
          boolean first = true;
          for (Authority authority : entry.getValue()) {
            if (!first) {
              b.append(",");
            }
            b.append(authority.getAuthorityId());
            first = false;
          }
          b.append("</deny>");
        }
      }

      b.append("</acl>");

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
  public Object clone() throws CloneNotSupportedException {
    SecurityContextImpl ctxt = (SecurityContextImpl) super.clone();
    ctxt.owner = owner;
    ctxt.aclAllow.putAll(aclAllow);
    ctxt.aclAllowDefaults.putAll(aclAllowDefaults);
    ctxt.actions = actions;
    ctxt.owner = owner;
    return ctxt;
  }

}