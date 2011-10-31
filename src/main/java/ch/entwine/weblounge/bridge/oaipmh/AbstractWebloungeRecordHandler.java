package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.bridge.oaipmh.harvester.ListRecordsResponse;
import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base implementation of a weblounge record handler
 */
public abstract class AbstractWebloungeRecordHandler implements RecordHandler {

  /** Logging facility */
  protected final Logger logger;

  /** the site */
  protected final Site site;

  /** The content repository */
  protected final WritableContentRepository contentRepository;

  /** User for resource creation */
  protected User harvesterUser;

  /** ISO3 language map */
  protected final Map<String, Language> iso3Languages = new HashMap<String, Language>();

  /** Presentation track flavor */
  protected final String presentationTrackFlavor;

  /** Presenter track flavor */
  protected final String presenterTrackFlavor;

  /** Dublin core episode flavor */
  protected final String dcEpisodeFlavor;

  /** Dublin core series flavor */
  protected final String dcSeriesFlavor;

  /**
   * Creates a new abstract weblounge record handler
   * 
   * @param site
   *          the site
   * @param contentRepository
   *          the content repository
   * @param harvesterUser
   *          the harvester user
   * @param presentationTrackFlavor
   *          the presentation track flavor
   * @param presenterTrackFlavor
   *          the presenter track flavor
   * @param dcEpisodeFlavor
   *          the dublin core episode flavor
   * @param dcSeriesFlavor
   *          the dublin core series flavor
   */
  public AbstractWebloungeRecordHandler(Site site,
      WritableContentRepository contentRepository, User harvesterUser,
      String presentationTrackFlavor, String presenterTrackFlavor,
      String dcEpisodeFlavor, String dcSeriesFlavor) {
    this.site = site;
    this.contentRepository = contentRepository;
    this.harvesterUser = harvesterUser;
    this.presentationTrackFlavor = presentationTrackFlavor;
    this.presenterTrackFlavor = presenterTrackFlavor;
    this.dcEpisodeFlavor = dcEpisodeFlavor;
    this.dcSeriesFlavor = dcSeriesFlavor;
    logger = LoggerFactory.getLogger(getClass());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler#handle(org.w3c.dom.Node)
   */
  public void handle(Node record) {
    String recordIdentifier = ListRecordsResponse.identifierOfRecord(record);
    boolean isDeleted = ListRecordsResponse.statusOfRecord(record);
    String dateString = ListRecordsResponse.dateOfRecord(record);

    Date date;
    try {
      date = OaiPmhUtil.fromUtc(dateString);
    } catch (ParseException e1) {
      logger.debug("Unable to parse date '{}'", dateString);
      return;
    }

    logger.info("Start harvesting resource " + recordIdentifier);

    SearchResult searchResult = searchSource(recordIdentifier);

    if (isDeleted) {
      if (searchResult.getHitCount() != 1) {
        logger.warn("The found element size is wrong!, size: " + searchResult.getHitCount());
        return;
      }
      ResourceSearchResultItem resourceResult = (ResourceSearchResultItem) searchResult.getItems()[0];
      deleteResource(resourceResult.getResourceURI());
      logger.info("Deleted harvestet resource " + recordIdentifier);
    } else {
      if (searchResult.getHitCount() == 1) {
        MovieResourceSearchResultItemImpl movieResultItem = (MovieResourceSearchResultItemImpl) searchResult.getItems()[0];
        MovieResource movieResource = movieResultItem.getMovieResource();

        if (!date.after(movieResource.getPublishFrom()))
          return;
        logger.warn("Update harvested element {}", recordIdentifier);

        Resource<MovieContent> resource = parseResource(record);
        resource.getURI().setIdentifier(movieResource.getIdentifier());
        resource.setPublished(harvesterUser, date, null);
        removeContents(resource);

        MovieContent content = parseResourceContent(record);
        content.setSource(recordIdentifier);
        resource.addContent(content);

        addResource(resource);
        addContent(resource.getURI(), content);
      } else if (searchResult.getHitCount() > 1) {
        logger.error("The repository contains already more than one element of {}", recordIdentifier);
      } else {
        Resource<?> resource = parseResource(record);
        resource.setPublished(harvesterUser, date, null);
        ResourceContent content = parseResourceContent(record);

        if (resource == null || content == null)
          return;

        addResource(resource);
        content.setSource(recordIdentifier);
        addContent(resource.getURI(), content);
        logger.info("Harvesting resource " + recordIdentifier);
      }
    }
  }

  /**
   * Search a page with the resource identifier.
   * 
   * @param sourceIdentifier
   *          the record identifier
   * @return the search result
   */
  private SearchResult searchSource(String sourceIdentifier) {
    SearchResult searchResult;
    SearchQuery q = new SearchQueryImpl(site);
    q.withSource(sourceIdentifier);
    try {
      searchResult = contentRepository.find(q);
    } catch (ContentRepositoryException e) {
      logger.error("Error searching for resources with given subject: " + sourceIdentifier);
      throw new RuntimeException(e);
    }
    return searchResult;
  }

  /**
   * Parse the record to a {@link ResourceContent}
   * 
   * @return the resource content to add to the repository
   */
  protected abstract MovieContent parseResourceContent(Node record);

  /**
   * Parse the record to a {@link Resource}
   * 
   * @return the resource to add to the repository
   */
  protected abstract Resource<MovieContent> parseResource(Node record);

  /**
   * Parse a matterhorn iso3 language string to a weblounge language.
   * 
   * @param languageCode
   *          the matterhorn iso3 language
   * @return the weblounge language
   * @throws UnknownLanguageException
   *           if language was not found
   */
  protected Language getISO3Language(String languageCode)
      throws UnknownLanguageException {
    Language language = iso3Languages.get(languageCode);
    if (language != null)
      return language;
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.getISO3Language().equals(languageCode)) {
        language = new LanguageImpl(new Locale(locale.getLanguage(), "", ""));
        iso3Languages.put(languageCode, language);
        break;
      }
    }
    if (language == null)
      throw new UnknownLanguageException(languageCode);
    // TODO: Think about how to handle unsupported languages
    return language;
  }

  /**
   * Add a resource content to the repository.
   * 
   * @param resourceUri
   *          the resource rui
   * @param content
   *          the resource content
   */
  protected void addContent(ResourceURI resourceUri, ResourceContent content) {
    InputStream is = null;
    try {
      contentRepository.putContent(resourceUri, content, null);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", resourceUri, e.getMessage());
      try {
        contentRepository.delete(resourceUri);
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", resourceUri, t);
      }
      throw new RuntimeException(e);
    } catch (ContentRepositoryException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", resourceUri, e.getMessage());
      try {
        contentRepository.delete(resourceUri);
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", resourceUri, t);
      }
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.warn("Error reading resource content {} from request", resourceUri);
      try {
        contentRepository.delete(resourceUri);
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", resourceUri, t);
      }
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Remove all contents from the resource and repository
   * 
   * @param resource
   *          the resource
   */
  private void removeContents(Resource<?> resource) {
    for (ResourceContent existingContent : resource.contents()) {
      Language language = existingContent.getLanguage();
      resource.removeContent(language);
      try {
        contentRepository.deleteContent(resource.getURI(), resource.getContent(language));
      } catch (IllegalStateException e) {
        logger.warn("Illegal state while deleting the resource {}: {}", resource.getURI(), e.getMessage());
        throw new RuntimeException(e);
      } catch (ContentRepositoryException e) {
        logger.warn("Error deleting the resource {}: {}", resource.getURI(), e.getMessage());
        throw new RuntimeException(e);
      } catch (IOException e) {
        logger.warn("Error writing the resource {}: {}", resource.getURI(), e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Add a resource to the repository
   * 
   * @param resource
   *          the resource
   */
  protected void addResource(Resource<?> resource) {
    try {
      contentRepository.put(resource);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding new resource {}: {}", resource.getURI(), e.getMessage());
      throw new RuntimeException(e);
    } catch (ContentRepositoryException e) {
      logger.warn("Error adding new resource {}: {}", resource.getURI(), e.getMessage());
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.warn("Error writing new resource {}: {}", resource.getURI(), e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * Delete a resource from the repository
   * 
   * @param resourceUri
   *          the resource uri to delete
   */
  protected void deleteResource(ResourceURI resourceUri) {
    try {
      contentRepository.delete(resourceUri, true);
    } catch (ContentRepositoryException e) {
      logger.warn("Illegal state while deleting resource {}: {}", resourceUri, e.getMessage());
    } catch (IOException e) {
      logger.error("Error deleting resource {}: {}", resourceUri, e.getMessage());
    }
  }

}