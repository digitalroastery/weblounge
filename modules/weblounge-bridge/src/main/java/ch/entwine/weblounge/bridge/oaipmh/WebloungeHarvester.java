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
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchQuery.Order;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.WritableContentRepository;
import ch.entwine.weblounge.common.scheduler.JobException;
import ch.entwine.weblounge.common.scheduler.JobWorker;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.opencastproject.util.data.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Dictionary;

/**
 * This cron job uses the harvester to harvest the <code>matterhorn</code>
 * prefix of the content repository that is configured in the job's
 * configuration options.
 */
public class WebloungeHarvester implements JobWorker {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(WebloungeHarvester.class);

  /** Configuration option for the repository to harvest */
  public static final String OPT_REPOSITORY_URL = "repository.url";

  /** Configuration option for the repository to harvest */
  public static final String OPT_HANDLER_CLASS = "handler.class";

  /** Configuration option for the flavor of the presentation tracks to use */
  public static final String OPT_PRSENTATION_TRACK_FLAVORS = "presentation-track-flavor";

  /** Configuration option for the flavor of the presenter tracks to use */
  public static final String OPT_PRESENTER_TRACK_FLAVORS = "presenter-track-flavor";

  /** Configuration option for the flavor of the dublin core episode to use */
  public static final String OPT_EPISODE_DC_FLAVORS = "episode-dublincore-flavor";

  /** Configuration option for the flavor of the dublin core series to use */
  public static final String OPT_SERIES_DC_FLAVORS = "series-dublincore-flavor";
  
  /** Configuration option for the mime-types to use */
  public static final String OPT_MIMETYPES = "mime-types";

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.JobWorker#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
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

    // Read the configuration value for the flavors
    String presentationTrackFlavor = (String) ctx.get(OPT_PRSENTATION_TRACK_FLAVORS);
    if (StringUtils.isBlank(presentationTrackFlavor))
      throw new JobException(this, "Configuration option '" + OPT_PRSENTATION_TRACK_FLAVORS + "' is missing from the job configuration");

    String presenterTrackFlavor = (String) ctx.get(OPT_PRESENTER_TRACK_FLAVORS);
    if (StringUtils.isBlank(presenterTrackFlavor))
      throw new JobException(this, "Configuration option '" + OPT_PRESENTER_TRACK_FLAVORS + "' is missing from the job configuration");

    String dcEpisodeFlavor = (String) ctx.get(OPT_EPISODE_DC_FLAVORS);
    if (StringUtils.isBlank(dcEpisodeFlavor))
      throw new JobException(this, "Configuration option '" + OPT_EPISODE_DC_FLAVORS + "' is missing from the job configuration");

    String dcSeriesFlavor = (String) ctx.get(OPT_SERIES_DC_FLAVORS);
    if (StringUtils.isBlank(dcSeriesFlavor))
      throw new JobException(this, "Configuration option '" + OPT_SERIES_DC_FLAVORS + "' is missing from the job configuration");
    
    String mimesTypes = (String) ctx.get(OPT_MIMETYPES);
    if (StringUtils.isBlank(mimesTypes))
      throw new JobException(this, "Configuration option '" + OPT_MIMETYPES + "' is missing from the job configuration");
    
    
    // Read the configuration value for the handler class
    String handlerClass = (String) ctx.get(OPT_HANDLER_CLASS);
    if (StringUtils.isBlank(handlerClass))
      throw new JobException(this, "Configuration option '" + OPT_HANDLER_CLASS + "' is missing from the job configuration");

    UserImpl harvesterUser = new UserImpl(name, site.getIdentifier(), "Harvester");

    RecordHandler handler;
    try {
      Class<? extends AbstractWebloungeRecordHandler> c = (Class<? extends AbstractWebloungeRecordHandler>) getClass().getClassLoader().loadClass(handlerClass);
      Class<?> paramTypes[] = new Class[8];
      paramTypes[0] = Site.class;
      paramTypes[1] = WritableContentRepository.class;
      paramTypes[2] = User.class;
      paramTypes[3] = String.class;
      paramTypes[4] = String.class;
      paramTypes[5] = String.class;
      paramTypes[6] = String.class;
      paramTypes[7] = String.class;
      Constructor<? extends AbstractWebloungeRecordHandler> constructor = c.getConstructor(paramTypes);
      Object arglist[] = new Object[8];
      arglist[0] = site;
      arglist[1] = contentRepository;
      arglist[2] = harvesterUser;
      arglist[3] = presentationTrackFlavor;
      arglist[4] = presenterTrackFlavor;
      arglist[5] = dcEpisodeFlavor;
      arglist[6] = dcSeriesFlavor;
      arglist[7] = mimesTypes;
      handler = constructor.newInstance(arglist);
    } catch (Throwable t) {
      throw new IllegalStateException("Unable to instantiate class " + handlerClass + ": " + t.getMessage(), t);
    }

    SearchResult searchResult;
    SearchQuery q = new SearchQueryImpl(site);
    q.withTypes(MovieResource.TYPE);
    q.sortByPublishingDate(Order.Descending);
    q.withPublisher(harvesterUser);
    try {
      searchResult = contentRepository.find(q);
    } catch (ContentRepositoryException e) {
      logger.error("Error searching for resources with harvester publisher.");
      throw new RuntimeException(e);
    }

    Option<Date> harvestingDate = Option.<Date> none();
    if (searchResult.getHitCount() > 0) {
      MovieResourceSearchResultItemImpl resultItem = (MovieResourceSearchResultItemImpl) searchResult.getItems()[0];
      harvestingDate = some(resultItem.getMovieResource().getPublishFrom());
    }

    try {
      harvest(repositoryUrl, harvestingDate, handler);
    } catch (Exception e) {
      logger.error("An error occured while harvesting " + url + ". Skipping this repository for now...", e);
    }
  }

  private void harvest(String url, Option<Date> from, RecordHandler handler)
      throws Exception {
    logger.info("Harvesting " + url + " from " + from + " on thread " + Thread.currentThread());
    OaiPmhRepositoryClient repositoryClient = OaiPmhRepositoryClient.newHarvester(url);
    ListRecordsResponse response = repositoryClient.listRecords(handler.getMetadataPrefix(), from, Option.<Date> none(), Option.<String> none());
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
