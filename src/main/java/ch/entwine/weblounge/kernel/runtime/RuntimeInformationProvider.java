/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.kernel.runtime;

import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

/**
 * A <code>RuntimeInformationProvider</code> is able to contribute to the
 * runtime information that is returned by the
 * {@link ch.entwine.weblounge.kernel.endpoint.RuntimeInformationEndpoint},
 * based on the current site, the user and the language.
 */
public interface RuntimeInformationProvider {

  /**
   * Returns an identifier that will be used to wrap the runtime information
   * content into its own node.
   * <p>
   * Implementers need to make sure that the value returned is a valid
   * <code>W3C</code> node name.
   * 
   * @return the component identifier
   */
  String getComponentId();

  /**
   * Returns an
   * <code>xml<code> node containing runtime information based on the current
   * site, user and language. Note that any of the parameters may be <code>null</code>
   * , e. g. while the system is still starting up or when no sites have been
   * installed.
   * 
   * @param site
   *          the site the current site
   * @param user
   *          the current user
   * @param language
   *          the current language
   * @return the runtime information
   */
  String getRuntimeInformation(Site site, User user, Language language);

}
