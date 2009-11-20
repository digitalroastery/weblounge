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

import ch.o2it.weblounge.common.security.AuthorizationProvider;
import ch.o2it.weblounge.common.security.SecurityManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the security manager for a single site.
 */
public class SecurityManagerImpl implements SecurityManager {

  /** The list of providers, associated with the authorization type */
  private Map<String, AuthorizationProvider> provider_;

  /**
   * Creates a new security manager.
   */
  public SecurityManagerImpl() {
    provider_ = new HashMap<String, AuthorizationProvider>();
  }

  /**
   * Adds <code>provider</code> to the list of authorization providers and uses
   * it to create authorizations of the given type.
   * 
   * @param type
   *          the type to register the provider with
   * @param provider
   *          the provider to add
   */
  public void addProvider(String type, AuthorizationProvider provider) {
    provider_.put(type, provider);
  }

  /**
   * Removes <code>provider</code> from the list of authorization providers.
   * 
   * @param provider
   *          the provider to remove
   */
  public void removeProvider(AuthorizationProvider provider) {
    Object key = null;
    for (Map.Entry<String, AuthorizationProvider> entry : provider_.entrySet()) {
      if (entry.getValue().equals(provider)) {
        key = entry.getKey();
        break;
      }
    }
    provider_.remove(key);
  }

  /**
   * Returns a provider for the given authorization type <code>type</code> or
   * <code>null</code> if no such provider can be found.
   * 
   * @param type
   *          the authorization type
   * @return a provider
   */
  public AuthorizationProvider getProvider(String type) {
    return provider_.get(type);
  }

  /**
   * Method to read in a security context definition from a configuration file.
   * 
   * @param config
   *          the <code>security</code> node
   * @return the security context
   */
  /*
   * public SecurityContext read(Node config) throws Exception {
   * RestrictionSecurityContext ctxt = new RestrictionSecurityContext();
   * 
   * // TODO Read Owner
   * 
   * // TODO Read permissions
   * 
   * if (config != null) { NodeList restrictions =
   * XPathAPI.selectNodeList(config, "restriction"); for (int i = 0; i <
   * restrictions.getLength(); i++) { Node permissionNode =
   * restrictions.item(i); Permission p = new
   * PermissionImpl(XPathAPI.selectSingleNode(permissionNode,
   * "@id").getNodeValue());
   * 
   * // determine restriction order
   * 
   * Restriction authorizationSet = new RestrictionImpl(); String evaluation =
   * XPathAPI.selectSingleNode(permissionNode, "@evaluate").getNodeValue(); if
   * (evaluation != null && evaluation.toLowerCase().trim().startsWith("allow"))
   * { authorizationSet.setEvaluationOrder(Restriction.ALLOW_DENY); } else {
   * authorizationSet.setEvaluationOrder(Restriction.DENY_ALLOW); }
   * 
   * // read allow and deny rules
   * 
   * NodeList allows = XPathAPI.selectNodeList(permissionNode, "allow | deny");
   * for (int j = 0; j < allows.getLength(); j++) { Node allowNode =
   * allows.item(j); String type = XPathAPI.selectSingleNode(permissionNode,
   * "@type").getNodeValue(); String ids =
   * XPathAPI.selectSingleNode(permissionNode, "text()").getNodeValue();
   * StringTokenizer tok = new StringTokenizer(ids, " ,;"); while
   * (tok.hasMoreTokens()) { String id = tok.nextToken(); AuthorizationProvider
   * ap = getProvider(type); Authority authorization = ap.getAuthorization(type,
   * id); if (allowNode.getLocalName().equals("allow"))
   * authorizationSet.allow(authorization); else
   * authorizationSet.deny(authorization); } ctxt.permit(p, authorizationSet); }
   * } } return ctxt; }
   */

}