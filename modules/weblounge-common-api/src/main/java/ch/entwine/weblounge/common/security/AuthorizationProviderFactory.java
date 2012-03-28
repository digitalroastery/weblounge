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

package ch.entwine.weblounge.common.security;

/**
 * This factory is used to manage authorization providers, which are able to
 * create authorization objects from the system configuration.
 */
public interface AuthorizationProviderFactory {

  /**
   * Adds <code>provider</code> to the list of authorization providers and uses
   * it to create authorizations of the given type.
   * 
   * @param type
   *          the type to register the provider with
   * @param provider
   *          the provider to add
   */
  void addProvider(String type, AuthorizationProvider provider);

  /**
   * Removes <code>provider</code> from the list of authorization providers.
   * 
   * @param provider
   *          the provider to remove
   */
  void removeProvider(AuthorizationProvider provider);

  /**
   * Returns a provider for the given authorization type <code>type</code> or
   * <code>null</code> if no such provider can be found.
   * 
   * @param type
   *          the authorization type
   * @return a provider
   */
  AuthorizationProvider getProvider(String type);

}