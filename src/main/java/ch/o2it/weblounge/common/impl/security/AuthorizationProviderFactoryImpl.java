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
import ch.o2it.weblounge.common.security.AuthorizationProviderFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This factory is used to manage authorization providers, which are able to
 * create authorization objects from the system configuration.
 */
public class AuthorizationProviderFactoryImpl implements AuthorizationProviderFactory {

  /** The list of providers, associated with the authorization type */
  private Map<String, AuthorizationProvider> provider_;

  /**
   * Creates a new provider factory.
   */
  public AuthorizationProviderFactoryImpl() {
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
    Set entries = provider_.entrySet();
    Iterator i = entries.iterator();
    while (i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();
      if (entry.getValue().equals(provider))
        key = entry.getKey();
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

}