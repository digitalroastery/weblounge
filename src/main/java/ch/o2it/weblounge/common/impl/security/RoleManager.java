/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.RoleRegistry;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

/**
 * The role manager is used to read role definitions from the system
 * configuration files.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class RoleManager {

  /**
   * Reads a role from the xml configuration node. The node is expected to look
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
    String id = XPathHelper.valueOf(path, node, "@id");
    RoleImpl r = new RoleImpl(id, site);

    // Read extensions

    String extension = XPathHelper.valueOf(path, node, "@extends");
    if (extension != null) {
      RoleRegistry roles = site.getRoles();
      StringTokenizer tok = new StringTokenizer(extension, " ,;");
      while (tok.hasMoreTokens()) {
        String ancestor = tok.nextToken();
        Role ancestorRole = roles.getRole(ancestor);
        if (ancestorRole != null) {
          r.extend(ancestorRole);
        }
      }
    }

    // Add names

    LocalizableObject<String> descriptions = new LocalizableObject<String>();
    Language defaultLanguage = site.getDefaultLanguage();
    LanguageSupport.addDescriptions(path, node, site.getLanguages(), defaultLanguage, descriptions);
    Iterator<Language> li = descriptions.languages();
    while (li.hasNext()) {
      Language l = li.next();
      r.put(descriptions.get(l), l);
    }

    return r;
  }

}