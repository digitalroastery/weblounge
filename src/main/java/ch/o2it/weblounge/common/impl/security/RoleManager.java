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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

/**
 * The role manager is used to read role definitions from the system
 * configuration files.
 */
public class RoleManager {

  /**
   * Reads a role from the XML configuration node. The node is expected to look
   * like this:
   * 
   * <pre>
   * 	&lt;role id=&quot;forum:admin&quot; extends=&quot;forum:moderator&quot;&gt;
   * 		&lt;name language=&quot;en&quot;&gt;Administrator&lt;/name&gt;
   * 		&lt;name language=&quot;de&quot;&gt;Administrator&lt;/name&gt;
   * 	&lt;/role&gt;
   * </pre>
   * 
   * @param node
   *          the role configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @return the role
   */
  public static Role read(XPath path, Node node, Site site) {
    String id = XPathHelper.valueOf(node, "@id", path);
    RoleImpl r = new RoleImpl(id, site);

    // Read extensions

    String extension = XPathHelper.valueOf(node, "@extends", path);
    if (extension != null) {
      StringTokenizer tok = new StringTokenizer(extension, " ,;");
      while (tok.hasMoreTokens()) {
        String ancestor = tok.nextToken();
        Role ancestorRole = site.getRole(ancestor, null);
        if (ancestorRole != null) {
          r.extend(ancestorRole);
        }
      }
    }

    // Add names

    LocalizableContent<String> descriptions = new LocalizableContent<String>();
    Language defaultLanguage = site.getDefaultLanguage();
    LanguageSupport.addDescriptions(node, "name", defaultLanguage, descriptions, false);
    for (Language l : descriptions.languages()) {
      r.put(descriptions.get(l), l);
    }

    return r;
  }

}