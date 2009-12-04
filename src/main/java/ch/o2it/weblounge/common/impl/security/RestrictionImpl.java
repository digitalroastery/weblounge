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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Restriction;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

/**
 * This object is a base implementation for restriction configurations and their
 * enforcement.
 * <p>
 * A restriction definition contains information on permissions and the
 * authorizations that are needed to obtain them. In addition, the restriction
 * defines how the authorizations are being evaluated. A restriction definition
 * looks like this:
 * 
 * <pre>
 * 		&lt;restriction id=&quot;system:write&quot; evaluate=&quot;allow,deny&quot;&gt;
 * 			&lt;allow type=&quot;ch.o2it.weblounge.api.security.Role&quot;&gt;system:editor&lt;/allow&gt;
 * 			&lt;allow type=&quot;ch.o2it.weblounge.api.security.User&quot;&gt;tobias.wunden&lt;/allow&gt;
 * 			&lt;deny&gt;all&lt;/deny&gt;
 * 		&lt;/restriction&gt;
 * </pre>
 */
public class RestrictionImpl implements Restriction {

  /** The evaluation order */
  private int evaluationOrder_;

  /** The allowed items */
  private Set<Authority> allowRules_;

  /** The denied items */
  private Set<Authority> denyRules_;

  /** The permission */
  private Permission permission_;

  /**
   * Creates a new restriction which allows everything.
   */
  public RestrictionImpl() {
    evaluationOrder_ = ALLOW_DENY;
  }

  /**
   * Creates a restriction from the given &lt;security&gt; section.
   * 
   * @param node
   *          the restriction configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  public RestrictionImpl(XPath path, Node node) {
    init(path, node);
  }

  /**
   * Returns the permission that is being restricted.
   * 
   * @return the permission
   */
  public Permission getPermission() {
    return permission_;
  }

  /**
   * Adds <code>authority</code> to the list of allowed items.
   * 
   * @param authorization
   *          the authorization to allow
   */
  public void allow(Authority authorization) {
    if (allowRules_ == null)
      allowRules_ = new HashSet<Authority>();
    allowRules_.add(authorization);
  }

  /**
   * Permits every authority to pass the <code>allow</code> rule evaluation.
   */
  public void allowAll() {
    allowRules_ = null;
  }

  /**
   * Adds <code>authority</code> to the list of denied items.
   * 
   * @param authority
   *          the authority to deny
   */
  public void deny(Authority authority) {
    if (denyRules_ == null)
      denyRules_ = new HashSet<Authority>();
    denyRules_.add(authority);
  }

  /**
   * Prevents all authorities from passing the <code>deny</code> rule
   * evaluation.
   */
  public void denyAll() {
    denyRules_ = null;
  }

  /**
   * Returns the order in which the restriction is tested. The order is one of:
   * <ul>
   * <li>{@link #ALLOW_DENY}</li>
   * <li>{@link #DENY_ALLOW}</li>
   * </ul>
   * 
   * @return the evaluation order
   */
  public int getEvaluationOrder() {
    return evaluationOrder_;
  }

  /**
   * Sets the evaluation order of this restriction. The order must be one of
   * <ul>
   * <li>{@link #ALLOW_DENY}</li>
   * <li>{@link #DENY_ALLOW}</li>
   * </ul>
   * otherwise, an <code>IllegalArgumentException</code> is thrown.
   * 
   * @param order
   *          the new evaluation order
   * @throws IllegalArgumentException
   *           if the order is unknown
   */
  public void setEvaluationOrder(int order) throws IllegalArgumentException {
    switch (order) {
    case ALLOW_DENY:
    case DENY_ALLOW:
      evaluationOrder_ = order;
      break;
    default:
      throw new IllegalArgumentException("Unknown evaluation order");
    }
  }

  /**
   * Sets the evaluation order to first check the allow rules and then, if
   * nothing matched, the deny restrictions.
   * 
   * @see #evaluateDenyAllow()
   */
  public void evaluateAllowDeny() {
    evaluationOrder_ = ALLOW_DENY;
  }

  /**
   * Sets the evaluation order to first check the allow rules and then, if
   * nothing matched, the deny restrictions.
   * 
   * @see #evaluateAllowDeny()
   */
  public void evaluateDenyAllow() {
    evaluationOrder_ = ALLOW_DENY;
  }

  /**
   * Returns <code>true</code> if the authorization passes the restriction.
   * 
   * @param authorization
   *          the authorization to test
   * @return <code>true</code> if the authorization passes the test
   */
  public boolean check(Authority authorization) {
    switch (evaluationOrder_) {
    case ALLOW_DENY:
      return isAllowed(authorization) || !isDenied(authorization);
    case DENY_ALLOW:
      return !isDenied(authorization) && isAllowed(authorization);
    }
    return false;
  }

  /**
   * Returns the types that are used in this restriction.
   * 
   * @return the types
   */
  public String[] getTypes() {
    Set<String> types = new HashSet<String>();
    if (allowRules_ != null) {
      for (Authority r : allowRules_) {
        String type = r.getAuthorityType();
        types.add(type);
      }
    }
    if (denyRules_ != null) {
      for (Authority r : denyRules_) {
        String type = r.getAuthorityType();
        types.add(type);
      }
    }
    return types.toArray(new String[types.size()]);
  }

  /**
   * Returns all <code>allow</code> entries for the given type.
   * 
   * @return the allow entries for <code>type</code>
   */
  public Authority[] getAllowed(String type) {
    Set<Authority> allowed = new HashSet<Authority>();
    if (allowRules_ != null) {
      for (Authority a : allowRules_) {
        if (a.getAuthorityType().equals(type)) {
          allowed.add(a);
        }
      }
    }
    return allowed.toArray(new Authority[allowed.size()]);
  }

  /**
   * Returns all <code>allow</code> entries.
   * 
   * @return the allow entries
   */
  public Authority[] getAllowed() {
    if (allowRules_ != null)
      return (Authority[]) allowRules_.toArray();
    else
      return new Authority[] {};
  }

  /**
   * Returns all <code>deny</code> entries for the given type.
   * 
   * @return the deny entries for <code>type</code>
   */
  public Authority[] getDenied(String type) {
    Set<Authority> denied = new HashSet<Authority>();
    if (denyRules_ != null) {
      for (Authority a : denyRules_) {
        if (a.getAuthorityType().equals(type)) {
          denied.add(a);
        }
      }
    }
    return denied.toArray(new Authority[denied.size()]);
  }

  /**
   * Returns all <code>deny</code> entries.
   * 
   * @return the deny entries
   */
  public Authority[] getDenied() {
    if (denyRules_ != null)
      return (Authority[]) denyRules_.toArray();
    else
      return new Authority[] {};
  }

  /**
   * Returns <code>true</code> if <code>authority</code> passes the allow rule
   * evaluation, that is, if either all authorities are allowed or the authority
   * is contained in the <code>allow</code> list.
   * 
   * @param authority
   *          the authorization to test
   */
  public boolean isAllowed(Authority authority) {
    return allowRules_ == null || allowRules_.contains("any") || allowRules_.contains(authority);
  }

  /**
   * Returns <code>true</code> if <code>authority</code> passes the deny rule
   * evaluation, that is, if either all authorities are denied or the authority
   * is contained in the <code>deny</code> list.
   * 
   * @param authority
   *          the authorization to test
   */
  public boolean isDenied(Authority authority) {
    return denyRules_ == null || denyRules_.contains("any") || denyRules_.contains(authority);
  }

  /**
   * Reads the restriction configuration node and adds the values to the proper
   * allow or deny rule.
   * 
   * @param node
   *          the restriction configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  protected void init(XPath path, Node node) throws ConfigurationException {
    allowRules_ = null;
    denyRules_ = null;

    try {
      // Permission
      String permission = XPathHelper.valueOf(node, "@id", path);
      permission_ = new PermissionImpl(permission);

      // Evaluation order
      String evaluation = XPathHelper.valueOf(node, "@evaluation", path);
      if (evaluation == null || evaluation.trim().startsWith("allow"))
        evaluateAllowDeny();
      else
        evaluateDenyAllow();

      // Read rules
      NodeList rules = XPathHelper.selectList(node, "allow | deny", path);
      for (int i = 0; i < rules.getLength(); i++) {
        Node rule = rules.item(i);
        String type = XPathHelper.valueOf(rule, "@type", path);
        String id = XPathHelper.valueOf(rule, "text()", path);

        // Check for multiple authorities
        StringTokenizer tok = new StringTokenizer(id, " ,;");
        while (tok.hasMoreTokens()) {
          String authorityId = tok.nextToken();
          if (type != null && authorityId != null) {
            if (rule.getLocalName().trim().equals("allow")) {
              if (allowRules_ == null)
                allowRules_ = new HashSet<Authority>();
              allowRules_.add(new RestrictionRule(AbstractSecurityContext.resolveAuthorityTypeShortcut(type), authorityId));
            } else {
              if (denyRules_ == null)
                denyRules_ = new HashSet<Authority>();
              denyRules_.add(new RestrictionRule(type, authorityId));
            }
          } else {
            throw new ConfigurationException("Malformed allow rule found in restriction definition!");
          }
        }

      }

    } catch (Exception e) {
      throw new ConfigurationException("Malformed restriction definition found!", e);
    }

  }

  /**
   * Serializes this restriction set.
   * 
   * @return the serialized form of this restriction set
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    b.append("<restriction id=\"");
    b.append(permission_.toString());
    b.append("\" evaluate=\"");
    b.append((getEvaluationOrder() == Restriction.ALLOW_DENY) ? "allow,deny\">" : "deny,allow\">");
    String[] types = getTypes();

    // Allow rules
    for (int i = 0; i < types.length; i++) {
      Authority[] allowed = getAllowed(types[i]);
      if (allowed.length > 0) {
        b.append("<allow type=\"");
        b.append(allowed[i].getAuthorityType());
        b.append("\">");
        for (int j = 0; j < allowed.length; j++) {
          if (j > 0)
            b.append(",");
          b.append(allowed[j].getAuthorityId());
        }
        b.append("</allow>");
      }

      // Deny rules
      Authority[] denied = getDenied(types[i]);
      if (denied.length > 0) {
        b.append("<deny type=\"");
        b.append(denied[i].getAuthorityType());
        b.append("\">");
        for (int j = 0; j < denied.length; j++) {
          if (j > 0)
            b.append(",");
          b.append(denied[j].getAuthorityId());
        }
        b.append("</deny>");
      }
    }
    b.append("\"</restriction>");

    return b.toString();
  }

  /**
   * A restriction rule represents the <code>allow</code> and <code>deny</code>
   * entries of a restriction definition. It features a type and a value, e. g.
   * the type <code>ch.o2it.weblounge.api.security.Role</code> and
   * <code>system:editor</code>, meaning that this rule will match the system
   * role <code>editor</code>.
   */
  final class RestrictionRule extends AuthorityImpl {

    /**
     * Creates a new allow or deny rule with the given type and id.
     * 
     * @param id
     *          the authorization identifier
     * @param type
     *          the authorization type
     */
    public RestrictionRule(String type, String id) {
      super(type, id);
    }

    /**
     * Returns <code>true</code> if the authorization identifier of this
     * authorization equals the string representation of <code>o</code>.
     * <p>
     * <strong>Note:</strong> this implementation also returns <code>true</code>
     * if either the authorization type is different from the type specified in
     * this rule or if <code>authorization</code> is <code>null</code>.
     * 
     * @param authority
     *          the authority trying to evaluate
     * @return <code>true</code> if this rule matches the authorization
     */
    public boolean evaluate(Authority authority) {
      return authority == null || type.equals(authority.getAuthorityType()) || id.equals(authority.getAuthorityId());
    }

  }

}