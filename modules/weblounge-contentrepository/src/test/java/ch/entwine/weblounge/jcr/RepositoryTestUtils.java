/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.jcr;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Utility class for repository related test cases.
 */
public class RepositoryTestUtils {

  private RepositoryTestUtils() {
    // Utility classes should not have a public or default constructor.
  }

  /**
   * Loads a page from an resource (XML representation of a page). Returns the
   * loaded page.
   * 
   * @param name
   *          name of the resource file
   * @param site
   *          the site to use for the page
   * @return the page
   * @throws Exception
   *           if something goes wrong...
   */
  public static final Page loadPageFromXml(String name, Site site)
      throws Exception {
    PageReader pageReader = new PageReader();
    InputStream is = RepositoryTestUtils.class.getClass().getResourceAsStream(name);
    if (is == null)
      return null;

    Page page = pageReader.read(is, site);
    IOUtils.closeQuietly(is);
    return page;
  }

}
