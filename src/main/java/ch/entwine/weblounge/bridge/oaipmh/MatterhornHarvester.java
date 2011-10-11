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

import static org.opencastproject.util.data.Option.some;

import ch.entwine.weblounge.bridge.oaipmh.harvester.ListRecordsResponse;
import ch.entwine.weblounge.bridge.oaipmh.harvester.OaiPmhRepositoryClient;
import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.scheduler.JobException;
import ch.entwine.weblounge.common.scheduler.JobWorker;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.opencastproject.util.data.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Dictionary;

/**
 * This cron job uses the harvester to harvest the <code>matterhorn</code>
 * prefix of the content repository that is configured in the job's
 * configuration options.
 */
public class MatterhornHarvester implements JobWorker {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(MatterhornHarvester.class);

  /** Configuration option for the repository to harvest */
  private static final String OPT_REPOSITORY_URL = "repository.url";

  /** Configuration option for the flavor of the tracks to use */
  private static final String OPT_TRACK_FLAVORS = "track.flavors";

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.JobWorker#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {

    Site site = (Site) ctx.get(Site.class.getName());

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

    RecordHandler matterhornHandler = new WebloungeMatterhornRecordHandlerImpl(site, contentRepository);
    Option<Date> from = some((Date) new Date());
    // penv =
    // newPersistenceEnvironment(newEntityManagerFactory(componentContext,
    // "org.opencastproject.oaipmh.harvester"));
    try {
      // DateTime now = new DateTime();
      harvest(repositoryUrl, from, matterhornHandler);
      // save the time of the last harvest but with a security delta of 1
      // minutes
      // LastHarvested lastHarvested = new LastHarvested(repositoryUrl,
      // now.minusMinutes(1).toDate());
      // update(penv, );
    } catch (Exception e) {
      logger.error("An error occured while harvesting " + url + ". Skipping this repository for now...", e);
    }
  }

  private void harvest(String url, Option<Date> from, RecordHandler handler)
      throws Exception {
    logger.info("Harvesting " + url + " from " + from + " on thread " + Thread.currentThread());
    OaiPmhRepositoryClient repositoryClient = OaiPmhRepositoryClient.newHarvester(url);
    ListRecordsResponse response = repositoryClient.listRecords(handler.getMetadataPrefix(), Option.<Date> none(), Option.<Date> none(), Option.<String> none());
    if (!response.isError()) {
      for (Node recordNode : ListRecordsResponse.getAllRecords(response, repositoryClient)) {
        handler.handle(recordNode);
      }
    } else if (response.isErrorNoRecordsMatch()) {
      logger.info("Repository returned no records.");
    } else {
      logger.error("Repository returned error code: " + response.getErrorCode().getOrElse("?"));
    }
  }

}
