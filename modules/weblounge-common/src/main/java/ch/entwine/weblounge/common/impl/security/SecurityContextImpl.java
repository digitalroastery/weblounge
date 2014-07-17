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
import ch.entwine.weblounge.common.security.ActionSet;
import ch.entwine.weblounge.common.security.Authority;
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
public class SecurityContextImpl extends AbstractSecurityContext implements Cloneable {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SecurityContextImpl.class);

  /** Allowed authorizations */
  private Map<Action, Set<Authority>> context = null;

  /** Allowed default authorizations */
  private Map<Action, Set<Authority>> defaultContext = null;

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
    context = new HashMap<Action, Set<Authority>>();
    defaultContext = new HashMap<Action, Set<Authority>>();
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

    Set<Authority> a = context.get(action);
    if (a == null) {
      a = new HashSet<Authority>();
      context.put(action, a);
      actions = null;
    }
    a.add(authority);
    defaultContext.remove(action);
  }

  /**
   * Adds <code>authority</code> to the default authorized authorities regarding
   * the given permission. Default authorities will not be stored in the
   * database, thus saving lots of space and speeding things up.
   * 
   * @param action
   *          the permission
   * @param authority
   *          the item that is allowed to obtain the permission
   */
  public void allowDefault(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Permission cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Security context '{}' requires '{}' for permission '{}'", new Object[] {
        this,
        authority,
        action });
    Set<Authority> a = defaultContext.get(action);
    if (a == null) {
      a = new HashSet<Authority>();
      defaultContext.put(action, a);
      actions = null;
    }
    a.add(authority);
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
      throw new IllegalArgumentException("Action cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Security context '{}' requires '{}' for action '{}'", new Object[] {
        this,
        authority,
        action });

    deny(action, authority, context);
    deny(action, authority, defaultContext);
  }

  /**
   * Removes <code>authority</code> from the denied authorities found in
   * <code>context</code> regarding the given action.
   * 
   * @param action
   *          the action
   * @param authority
   *          the authorization to deny
   * @param context
   *          the authorities context
   */
  private void deny(Action action, Authority authority,
      Map<Action, Set<Authority>> context) {
    Set<Authority> authorities = context.get(action);

    // If the authorities have been found, iterate over them to find a matching
    // authority. We have to do this instead of directly calling
    // authorities.remove(authority) because the context may contain AuthorityImpl
    // instances which will equal a matching role (after casting them to an authority)
    // but not vice versa.
    if (authorities != null) {
      for (Authority a : authorities) {
        if (a.isAuthorizedBy(authority)) {
          authorities.remove(a);
          return;
        }
      }
      if (authorities.size() == 0) {
        context.remove(action);
      }
    }
  }

  /**
   * Denies everyone and everything regarding action <code>action</code>
   * .
   * 
   * @param action
   *          the action
   */
  public void denyAll(Action action) {
    denyAll(action, context);
    denyAll(action, defaultContext);
  }

  /**
   * Denies everyone and everything regarding action <code>action</code>
   * in the specified context.
   * 
   * @param action
   *          the action
   * @param context
   *          the context
   */
  private void denyAll(Action action,
      Map<Action, Set<Authority>> context) {
    Set<Authority> authorities = context.get(action);
    if (authorities != null) {
      authorities.clear();
    }
  }

  /**
   * Denies everyone and everything.
   */
  public void denyAll() {
    context.clear();
    defaultContext.clear();
  }

  /**
   * Checks whether the roles that the caller currently owns satisfy the
   * constraints of this context ion the given action.
   * 
   * @param action
   *          the action to obtain
   * @param authority
   *          the object claiming the action
   * @return <code>true</code> if the item may obtain the action
   */
  public boolean check(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Request to check action '{}' for authority '{}' at {}", new Object[] {
        action,
        authority,
        this });

    return check(action, authority, defaultContext) || check(action, authority, context);
  }

  /**
   * Checks whether the roles that the caller currently owns satisfy the
   * constraints of the given context regarding the given action.
   * 
   * @param action
   *          the action to obtain
   * @param authority
   *          the object claiming the action
   * @param context
   *          the context
   * @return <code>true</code> if the item may obtain the action
   */
  private boolean check(Action action, Authority authority,
      Map<Action, Set<Authority>> context) {
    Set<Authority> authorities = context.get(action);
    if (authorities != null) {
      for (Authority a : authorities) {
        if (authority.isAuthorizedBy(a))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the object <code>o</code> is allowed to act on
   * the secured object in a way that satisfies the given action set
   * <code>p</code>.
   * 
   * @param actions
   *          the required set of actions
   * @param authority
   *          the object claiming the actions
   * @return <code>true</code> if the object may obtain the actions
   */
  public boolean check(ActionSet actions, Authority authority) {
    if (actions == null)
      throw new IllegalArgumentException("Actions cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.debug("Request to check action set for authorization '{}' at {}", authority, this);

    return checkOneOf(actions, authority) && checkAllOf(actions, authority);
  }

  /**
   * Returns the authorities that are explicitly allowed by the context.
   * 
   * @see ch.entwine.weblounge.common.security.SecurityContext#getAllowed(ch.entwine.weblounge.common.security.Action)
   */
  public Authority[] getAllowed(Action p) {
    Set<Authority> authorities = defaultContext.get(p);
    if (authorities == null) {
      authorities = context.get(p);
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
   * @see ch.entwine.weblounge.common.security.SecurityContext#getDenied(ch.entwine.weblounge.common.security.Action)
   */
  public Authority[] getDenied(Action p) {
    return new Authority[] {};
  }

  /**
   * Returns <code>true</code> if the authorization is sufficient to obtain the
   * "oneof" action set.
   * 
   * @param p
   *          the action set
   * @param authorization
   *          the authorization to check
   * @return <code>true</code> if the user has one of the actions
   */
  protected boolean checkOneOf(ActionSet p, Authority authorization) {
    Action[] actions = p.some();
    for (int i = 0; i < actions.length; i++) {
      if (check(actions[i], authorization)) {
        return true;
      }
    }
    return (actions.length == 0);
  }

  /**
   * Returns <code>true</code> if the authorization is sufficient to obtain the
   * "allof" action set.
   * 
   * @param p
   *          the action set
   * @param authorization
   *          the authorization to check
   * @return <code>true</code> if the user has all of the actions
   */
  protected boolean checkAllOf(ActionSet p, Authority authorization) {
    Action[] actions = p.all();
    for (int i = 0; i < actions.length; i++) {
      if (!check(actions[i], authorization)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the actions that are defined in this security context.
   * 
   * @return the actions
   */
  public Action[] actions() {
    if (actions == null) {
      actions = new Action[context.size() + defaultContext.size()];
      List<Action> permissionList = new ArrayList<Action>();
      permissionList.addAll(context.keySet());
      permissionList.addAll(defaultContext.keySet());
      permissionList.toArray(actions);
    }
    return actions;
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
    this.context.clear();
    actions = null;

    // Read permissions
    NodeList permissions = XPathHelper.selectList(context, "/security/permission", path);
    for (int i = 0; i < permissions.getLength(); i++) {
      Node p = permissions.item(i);
      String id = XPathHelper.valueOf(p, "@id", path);
      Action action = new ActionImpl(id);

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
        allow(action, authority);
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
      b.append((new UserImpl(owner)).toXml());
      b.append("</owner>");
    }

    // Permissions
    for (Action p : context.keySet()) {
      Map<String, Set<Authority>> authorities = groupByType(context.get(p));
      for (Map.Entry<String, Set<Authority>> entry : authorities.entrySet()) {
        String type = entry.getKey();
        b.append("<permission id=\"");
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
  public Object clone() throws CloneNotSupportedException {
    SecurityContextImpl ctxt = (SecurityContextImpl) super.clone();
    ctxt.owner = owner;
    ctxt.context.putAll(context);
    ctxt.defaultContext.putAll(defaultContext);
    ctxt.actions = actions;
    ctxt.owner = owner;
    return ctxt;
  }

}