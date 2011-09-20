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
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SecurityUtils;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.security.WebloungeUser;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;

/**
 * Returns runtime information on the current user.
 */
public class SecurityRuntimeInformation implements RuntimeInformationProvider {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getComponentId()
   */
  public String getComponentId() {
    return "security";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getRuntimeInformation(ch.entwine.weblounge.common.site.Site,
   *      ch.entwine.weblounge.common.security.User,
   *      ch.entwine.weblounge.common.language.Language, Environment)
   */
  public String getRuntimeInformation(Site site, User user, Language language, Environment environment) {
    if (user == null)
      return null;
    
    StringBuffer b = new StringBuffer();

    b.append("<user id=\"" + user.getLogin() + "\"");
    if (user.getRealm() != null) {
      b.append(" realm=\"");
      b.append(user.getRealm());
      b.append("\"");
    }
    b.append(">");

    // First name
    if (user instanceof WebloungeUser && ((WebloungeUser)user).getFirstName() != null) {
      b.append("<firstname>");
      b.append(((WebloungeUser)user).getFirstName());
      b.append("</firstname>");
    }

    // First name
    if (user instanceof WebloungeUser && ((WebloungeUser)user).getLastName() != null) {
      b.append("<lastname>");
      b.append(((WebloungeUser)user).getLastName());
      b.append("</lastname>");
    }

    // Name, if first name and last name were not given
    if (user.getName() != null) {
      b.append("<name><![CDATA[");
      b.append(user.getName());
      b.append("]]></name>");
    }

    // Email
    if (user instanceof WebloungeUser && ((WebloungeUser)user).getEmail() != null) {
      b.append("<email>");
      b.append(((WebloungeUser)user).getEmail());
      b.append("</email>");
    }
    
    b.append("</user>");
    
    // Add role information
    StringBuffer roles = new StringBuffer();
    for (Role role : SecurityUtils.getRoles(user)) {
      roles.append("<role context=\"").append(role.getContext()).append("\" ");
      roles.append("id=\"").append(role.getIdentifier()).append("\">");
      roles.append("<name><![CDATA[");
      roles.append(role.toString(language));
      roles.append("]]></name>");
      roles.append("</role>");
    }
    if (roles.length() > 0) {
      b.append("<roles>");
      b.append(roles.toString());
      b.append("</roles>");
    }

    return b.toString();
  }

}
