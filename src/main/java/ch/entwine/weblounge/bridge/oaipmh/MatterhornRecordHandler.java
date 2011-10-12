package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.bridge.oaipmh.harvester.ListRecordsResponse;
import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.movie.VideoStreamImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.opencastproject.mediapackage.AudioStream;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageBuilder;
import org.opencastproject.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.Stream;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.VideoStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Date;

/**
 * TODO: Comment WebloungeMatterhornRecordHandlerImpl
 */
public class MatterhornRecordHandler extends WebloungeRecordHandler implements RecordHandler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(MatterhornRecordHandler.class);

  /** User for resource creation */
  private User harvesterUser = new UserImpl("harvester");

  /** Name of the oai pmh prefix */
  private static final String MATTERHORN_REPOSITORY_PREFIX = "matterhorn";

  /** The media package builder */
  private MediaPackageBuilder mediaPackageBuilder;

  public MatterhornRecordHandler(Site site,
      WritableContentRepository contentRepository) {
    super(site, contentRepository);
    mediaPackageBuilder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.RecordHandler#getMetadataPrefix()
   */
  public String getMetadataPrefix() {
    return MATTERHORN_REPOSITORY_PREFIX;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.RecordHandler#handle(org.w3c.dom.Node)
   */
  public void handle(Node record) {
    String recordIdentifier = ListRecordsResponse.identifierOfRecord(record);
    boolean isDeleted = ListRecordsResponse.statusOfRecord(record);

    if (isDeleted) {
      SearchResult searchResult;
      SearchQuery q = new SearchQueryImpl(site);
      q.withSubject(recordIdentifier);
      try {
        searchResult = contentRepository.find(q);
      } catch (ContentRepositoryException e) {
        logger.error("Error searching for resources with given subject: " + recordIdentifier);
        throw new RuntimeException(e);
      }
      if (searchResult.getHitCount() != 1) {
        logger.error("The found element size is wrong!, size: " + searchResult.getHitCount());
        return;
      }
      ResourceSearchResultItem resourceResult = (ResourceSearchResultItem) searchResult.getItems()[0];
      try {
        contentRepository.delete(resourceResult.getResourceURI());
      } catch (ContentRepositoryException e) {
        logger.warn("Illegal state while deleting resource {}: {}", resourceResult.getResourceURI(), e.getMessage());
      } catch (IOException e) {
        logger.error("Error deleting resource {}: {}", resourceResult.getResourceURI(), e.getMessage());
      }
    } else {
      saveRecordAsMovieResource(record, recordIdentifier);
    }
  }

  /**
   * Load the the {@link MediaPackage} from the record, parse it to a
   * {@link MovieContent} and save it to the content repository.
   * 
   * @param record
   *          the matterhorn record
   * @param recordIdentifier
   *          the record identifier
   */
  private void saveRecordAsMovieResource(Node record, String recordIdentifier) {
    Node mediaPackageNode = ListRecordsResponse.metadataOfRecord(record);
    final MediaPackage mediaPackage;
    try {
      mediaPackage = mediaPackageBuilder.loadFromXml(mediaPackageNode);
    } catch (MediaPackageException e) {
      logger.warn("Error loading mediapackage from record");
      throw new RuntimeException(e);
    }
    logger.info("Harvested mediapackage " + mediaPackage.getIdentifier().toString());

    // Add Resource to Repo
    MovieResourceImpl movieResource = parseMovieResource(site, mediaPackage);
    movieResource.addSubject(recordIdentifier);
    try {
      contentRepository.put(movieResource);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding new resource {}: {}", movieResource.getURI(), e.getMessage());
      throw new RuntimeException(e);
    } catch (ContentRepositoryException e) {
      logger.warn("Error adding new resource {}: {}", movieResource.getURI(), e.getMessage());
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.warn("Error writing new resource {}: {}", movieResource.getURI(), e.getMessage());
      throw new RuntimeException(e);
    }

    // Add Content to Repo
    MovieContent movieContent = paresMovieContent(mediaPackage);
    try {
      // new URL(movieContent.getFilename()).openStream()
      contentRepository.putContent(movieResource.getURI(), movieContent, null);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", movieResource.getURI(), e.getMessage());
      try {
        contentRepository.delete(movieResource.getURI());
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", movieResource.getURI(), t);
      }
      throw new RuntimeException(e);
    } catch (ContentRepositoryException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", movieResource.getURI(), e.getMessage());
      try {
        contentRepository.delete(movieResource.getURI());
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", movieResource.getURI(), t);
      }
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.warn("Error reading resource content {} from request", movieResource.getURI());
      try {
        contentRepository.delete(movieResource.getURI());
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", movieResource.getURI(), t);
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a {@link MovieResource} from a matterhorn {@link MediaPackage}
   * track.
   * 
   * @param mediaPackage
   *          the matterhorn media package
   * @return the movie resource
   */
  private MovieContent paresMovieContent(MediaPackage mediaPackage) {
    Language language = getISO3Language(mediaPackage.getLanguage());

    // Set Content
    MediaPackageElement element = mediaPackage.getElements()[0];
    MovieContent content = new MovieContentImpl(element.getURI().toString(), language, element.getMimeType().asString());
    StringBuilder author = new StringBuilder();
    String[] creators = mediaPackage.getCreators();
    for (int i = 0; i < creators.length; i++) {
      if (i != 0)
        author.append(", ");
      author.append(creators[i]);
    }
    content.setAuthor(author.toString());
    if (element.getSize() != -1)
      content.setSize(element.getSize());
    if (mediaPackage.getDuration() != -1)
      content.setDuration(mediaPackage.getDuration());

    // TODO: Use tracks with correct flavor for movie
    Track track = mediaPackage.getTracks()[0];
    for (Stream stream : track.getStreams()) {
      if (stream instanceof AudioStream) {
        ch.entwine.weblounge.common.content.movie.AudioStream audioStream = new ch.entwine.weblounge.common.impl.content.movie.AudioStreamImpl();
        AudioStream matterhornAudioStream = (AudioStream) stream;
        if (matterhornAudioStream.getBitDepth() != null)
          audioStream.setBitDepth(matterhornAudioStream.getBitDepth());
        if (matterhornAudioStream.getBitRate() != null)
          audioStream.setBitRate(matterhornAudioStream.getBitRate());
        if (matterhornAudioStream.getChannels() != null)
          audioStream.setChannels(matterhornAudioStream.getChannels());
        if (StringUtils.isNotBlank(matterhornAudioStream.getFormat()))
          audioStream.setFormat(matterhornAudioStream.getFormat());
        if (matterhornAudioStream.getSamplingRate() != null)
          audioStream.setSamplingRate(matterhornAudioStream.getSamplingRate());
        content.addStream(audioStream);
      } else if (stream instanceof VideoStream) {
        ch.entwine.weblounge.common.content.movie.VideoStream videoStream = new VideoStreamImpl();
        VideoStream matterhornVideoStream = (VideoStream) stream;
        if (matterhornVideoStream.getBitRate() != null)
          videoStream.setBitRate(matterhornVideoStream.getBitRate());
        if (StringUtils.isNotBlank(matterhornVideoStream.getFormat()))
          videoStream.setFormat(matterhornVideoStream.getFormat());
        if (matterhornVideoStream.getFrameHeight() != null)
          videoStream.setFrameHeight(matterhornVideoStream.getFrameHeight());
        if (matterhornVideoStream.getFrameWidth() != null)
          videoStream.setFrameWidth(matterhornVideoStream.getFrameWidth());
        if (matterhornVideoStream.getFrameRate() != null)
          videoStream.setFrameRate(matterhornVideoStream.getFrameRate());
        // if (matterhornVideoStream.getScanType() != null)
        // videoStream.setScanType(ScanType.fromString(matterhornVideoStream.getScanType().toString()));
        content.addStream(videoStream);
      }
    }

    // Use the logged in user as the author
    content.setCreator(harvesterUser);
    content.setCreationDate(new Date());
    return content;
  }

  /**
   * Creates a new {@link MovieResource} from a matterhorn {@link MediaPackage}.
   * 
   * @param site
   *          the site
   * @param mediaPackage
   *          the matterhorn media package
   * @return the movie resource
   */
  private MovieResourceImpl parseMovieResource(Site site,
      MediaPackage mediaPackage) {

    // TODO: Use dublin core catalog for metadata
    Language language = getISO3Language(mediaPackage.getLanguage());

    MovieResourceImpl movieResource = new MovieResourceImpl(new MovieResourceURIImpl(site));
    movieResource.setCreated(harvesterUser, new Date());

    // Set Header Metadata
    movieResource.setTitle(mediaPackage.getTitle(), language);
    // movieResource.setDescription(description, language)
    for (String subject : mediaPackage.getSubjects()) {
      movieResource.addSubject(subject);
    }
    // movieResource.setCoverage(coverage, language)
    // movieResource.setRights(rights, language)

    return movieResource;
  }

}
