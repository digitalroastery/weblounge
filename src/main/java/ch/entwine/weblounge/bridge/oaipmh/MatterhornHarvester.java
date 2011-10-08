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

package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.scheduler.JobException;
import ch.entwine.weblounge.common.scheduler.JobWorker;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

/**
 * This cron job uses the harvester to harvest the <code>matterhorn</code>
 * prefix of the content repository that is configured in the job's
 * configuration options.
 */
public class MatterhornHarvester implements JobWorker {

  /** Configuration option for the repository to harvest */
  private static final String OPT_REPOSITORY_URL = "repository.url";

  /** Configuration option for the flavor of the tracks to use */
  private static final String OPT_TRACK_FLAVORS = "track.flavors";

  /** Name of the oai pmh prefix */
  private static final String MATTERHORN_REPOSITORY_PREFIX = "matterhorn";

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.JobWorker#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {

    Site site = (Site) ctx.get(Site.class.getName());
    String repositoryPrefix = MATTERHORN_REPOSITORY_PREFIX;

    // Get hold of the content repository
    WritableContentRepository contentRepository = null;
    if (site.getContentRepository().isReadOnly())
      throw new JobException(this, "Content repository of site '" + site + "' is read only");
    contentRepository = (WritableContentRepository) site.getContentRepository();

    // Read the configuration value for the repository url
    String repositoryUrl = (String) ctx.get(OPT_REPOSITORY_URL);
    if (StringUtils.isBlank(repositoryUrl))
      throw new JobException(this, "Configuration option '" + OPT_REPOSITORY_URL + "' is missing from the job configuration");

    // Make sure the url is well formed
    URL url = null;
    try {
      url = new URL(repositoryUrl);
    } catch (MalformedURLException e) {
      throw new JobException(this, "Repository url '" + repositoryUrl + "' is malformed: " + e.getMessage());
    }

    // TODO instantiate a harvester with the given repositoryUrl and
    // repositoryPrefix

    // TODO Start harvesting

    // TODO Add and delete to and from the content repository
    // for (.. : harvestingResultSet) {
    // String xml = getPayload();
    // MediaPackage mediaPackage = MediaPackageParser.getFromXml(xml);
    // TODO: Use dublin core catalog for metadata
    // TODO: Use tracks with correct flavor for movie
    // }
  }

  /**
   * @return
   */
  private String getPayload() {
    // TODO Auto-generated method stub
    return null;
  }

}
