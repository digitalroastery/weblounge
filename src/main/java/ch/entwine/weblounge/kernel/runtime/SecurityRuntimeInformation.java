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

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SecurityUtils;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
   *      ch.entwine.weblounge.common.language.Language)
   */
  public String getRuntimeInformation(Site site, User user, Language language) {
    if (user == null)
      return null;
    String securityXml = user.toXml();

    // Filter out the root tag
    Pattern p = Pattern.compile("^<([^>]*)>(.*)</\\1>$");
    Matcher m = p.matcher(securityXml);
    if (m.matches())
      securityXml = m.group(2);

    // Remove the password
    securityXml = securityXml.replaceAll("<password.*</password>", "");
    securityXml = ConfigurationUtils.processTemplate(securityXml, site);
    
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
      securityXml += "<roles>" + roles.toString() + "</roles>";
    }

    return securityXml;
  }

}
