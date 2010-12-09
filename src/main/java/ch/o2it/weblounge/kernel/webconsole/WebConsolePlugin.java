/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.kernel.webconsole;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.kernel.WebloungeSharedResources;

import org.apache.commons.io.IOUtils;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class implements plugin for the felix webconsole that provides access to
 * weblounge functionality as well as sites.
 */
public class WebConsolePlugin extends AbstractWebConsolePlugin {

  /** Serial version uid */
  private static final long serialVersionUID = 868658142169523757L;

  /** Plugin uri */
  private static final String URI = "weblounge";

  /** Plugin name */
  private static final String TITLE = "Weblounge";

  /** Path to the main index */
  private static final String INDEX = "/webconsole/index.html";

  /** Template key for the sites data */
  private static final String SITES_DATA_KEY = "sites-data";

  /** Template key for the total sites count */
  private static final String SITES_COUNT_KEY = "sites-count";

  /** Template key for the number of active sites */
  private static final String SITES_ACTIVE_COUNT_KEY = "sites-active";

  /** Template key for the number of inactive sites */
  private static final String SITES_INACTIVE_COUNT_KEY = "sites-inactive";

  /** Template key for the uri to the weblounge shared resources */
  private static final String SHARED_RESOURCES_KEY = "shared-resources";

  /** The list of registered sites */
  private List<Site> sites = new ArrayList<Site>();

  /** Reference to the shared resources */
  private WebloungeSharedResources sharedResources = null;

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#getLabel()
   */
  @Override
  public String getLabel() {
    return URI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#getTitle()
   */
  @Override
  public String getTitle() {
    return TITLE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#renderContent(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void renderContent(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    InputStream is = null;
    try {
      is = getClass().getResourceAsStream(INDEX);
      String template = IOUtils.toString(is);

      // Process the template
      template = addSharedResourcesMountpoint(template);
      template = addSitesData(template);

      // Write the template to the output
      res.getWriter().print(template);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Returns the replacement for <code>sites-data</code> in the template.
   * 
   * @param the
   *          template
   * @return the sites data
   */
  protected String addSharedResourcesMountpoint(String template) {
    String uri = null;
    if (sharedResources != null)
      uri = sharedResources.getResourcesMountpoint();
    else
      uri = WebloungeSharedResources.DEFAULT_RESOURCES_MOUNTPOINT;
    template = template.replace("${" + SHARED_RESOURCES_KEY + "}", uri);
    return template;
  }

  /**
   * Returns the replacement for <code>sites-data</code> in the template.
   * 
   * @param the
   *          template
   * @return the sites data
   */
  protected String addSitesData(String template) {
    int activeSites = 0;
    int inactiveSites = 0;

    // Collect and process the site data
    StringBuffer sitesData = new StringBuffer();
    synchronized (sites) {
      int i = 1;
      for (Site site : sites) {
        String state = site.isRunning() ? "active" : "inactive";
        activeSites += site.isRunning() ? 1 : 0;
        inactiveSites += !site.isRunning() ? 1 : 0;
        sitesData.append("{");
        sitesData.append("\"id\":").append(i).append(",");
        sitesData.append("\"name\":\"").append(site.getName()).append("\",");
        sitesData.append("\"version\":\"").append("-").append("\",");
        sitesData.append("\"status\":\"").append(state).append("\"");
        sitesData.append("}");
        i++;
      }
    }
    template = template.replace("${" + SITES_DATA_KEY + "}", sitesData.toString());

    // Total number of sites
    template = template.replace("${" + SITES_COUNT_KEY + "}", Integer.toString(sites.size()));

    // Number of active sites
    template = template.replace("${" + SITES_ACTIVE_COUNT_KEY + "}", Integer.toString(activeSites));

    // Number of inactive sites
    template = template.replace("${" + SITES_INACTIVE_COUNT_KEY + "}", Integer.toString(inactiveSites));

    return template;
  }

  /**
   * Adds a site to the list of registered sites.
   * 
   * @param site
   *          the site to add
   */
  void addSite(Site site) {
    synchronized (sites) {
      sites.add(site);
    }
  }

  /**
   * Removes a site from the list of registered sites.
   * 
   * @param site
   *          the site to remove
   */
  void removeSite(Site site) {
    synchronized (sites) {
      sites.remove(site);
    }
  }

  /**
   * Sets a reference to the shared resources.
   * 
   * @param sharedResources
   *          the shared resources
   */
  void setSharedResources(WebloungeSharedResources sharedResources) {
    this.sharedResources = sharedResources;
  }

}
