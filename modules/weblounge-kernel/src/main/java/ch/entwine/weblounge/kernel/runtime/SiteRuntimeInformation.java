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
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns runtime information on the current site.
 */
public class SiteRuntimeInformation implements RuntimeInformationProvider {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getComponentId()
   */
  public String getComponentId() {
    return "site";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getRuntimeInformation(ch.entwine.weblounge.common.site.Site,
   *      ch.entwine.weblounge.common.security.User,
   *      ch.entwine.weblounge.common.language.Language, Environment)
   */
  public String getRuntimeInformation(Site site, User user, Language language, Environment environment) {
    if (site == null)
      return null;
    String siteXml = site.toXml();

    // Filter out the root tag
    Pattern p = Pattern.compile("^<([^>]*)>(.*)</site>$");
    Matcher m = p.matcher(siteXml);
    if (m.matches())
      siteXml = m.group(2);
    siteXml = "<id>" + site.getIdentifier() + "</id>" + siteXml;
    siteXml = siteXml.replaceAll("<password.*</password>", "");
    siteXml = ConfigurationUtils.processTemplate(siteXml, site, environment);
    return siteXml;
  }

}
