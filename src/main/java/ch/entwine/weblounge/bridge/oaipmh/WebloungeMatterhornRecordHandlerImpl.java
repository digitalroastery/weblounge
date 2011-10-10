package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.content.movie.ScanType;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.movie.VideoStreamImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.opencastproject.mediapackage.AudioStream;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.MediaPackageParser;
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
public class WebloungeMatterhornRecordHandlerImpl implements RecordHandler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(WebloungeMatterhornRecordHandlerImpl.class);

  /** User for resource creation */
  private User harvesterUser = new UserImpl("harvester");

  /** Name of the oai pmh prefix */
  private static final String MATTERHORN_REPOSITORY_PREFIX = "matterhorn";

  /** the site */
  private final Site site;

  /** The content repository */
  private final WritableContentRepository contentRepository;

  public WebloungeMatterhornRecordHandlerImpl(Site site,
      WritableContentRepository contentRepository) {
    this.site = site;
    this.contentRepository = contentRepository;
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
   * Returns <code>true</code> if record was deleted, otherwise
   * <code>false</code>.
   * 
   * @param record
   *          the record node
   * @return <code>true</code> if record was deleted, otherwise
   *         <code>false</code>
   */
  private boolean isDeleted(Node record) {
    // TODO check with xPath if record header has a status="deleted"
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.RecordHandler#handle(org.w3c.dom.Node)
   */
  public void handle(Node record) {
    // TODO Read the record identifier

    // TODO check xml header if record is deleted
    boolean deleted = isDeleted(record);

    // TODO get xml content and parse it to media package
    String xml = "XML";
    MediaPackage mediaPackage = null;
    try {
      mediaPackage = MediaPackageParser.getFromXml(xml);
    } catch (MediaPackageException e) {
      e.printStackTrace();
    }

    // Add Resource to Repo
    MovieResourceImpl movieResource = parseMovieResource(site, mediaPackage);
    try {
      contentRepository.put(movieResource);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding new resource {}: {}", movieResource.getURI(), e.getMessage());
      throw e;
      // TODO Throw exception
    } catch (ContentRepositoryException e) {
      logger.warn("Error adding new resource {}: {}", movieResource.getURI(), e.getMessage());
    } catch (IOException e) {
      logger.warn("Error writing new resource {}: {}", movieResource.getURI(), e.getMessage());
    }

    // Add Content to Repo
    MovieContent movieContent = paresMovieContent(mediaPackage);
    try {
      contentRepository.putContent(movieResource.getURI(), movieContent, null);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", movieResource.getURI(), e.getMessage());
      try {
        contentRepository.delete(movieResource.getURI());
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", movieResource.getURI(), t);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", movieResource.getURI(), e.getMessage());
      try {
        contentRepository.delete(movieResource.getURI());
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", movieResource.getURI(), t);
      }
    } catch (IOException e) {
      logger.warn("Error reading resource content {} from request", movieResource.getURI());
      try {
        contentRepository.delete(movieResource.getURI());
      } catch (Throwable t) {
        logger.error("Error deleting orphan resource {}", movieResource.getURI(), t);
      }
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
    Language language = LanguageUtils.getLanguage(mediaPackage.getLanguage());
    // TODO: Use tracks with correct flavor for movie

    // Set Content
    MediaPackageElement element = mediaPackage.getElements()[0];
    MovieContent content = new MovieContentImpl(element.getURI().toString(), language, element.getMimeType().asString());
    content.setAuthor(mediaPackage.getCreators().toString());
    content.setSize(element.getSize());
    content.setDuration(mediaPackage.getDuration());

    Track track = mediaPackage.getTracks()[0];
    for (Stream stream : track.getStreams()) {
      if (stream instanceof AudioStream) {
        ch.entwine.weblounge.common.content.movie.AudioStream audioStream = new ch.entwine.weblounge.common.impl.content.movie.AudioStreamImpl();
        AudioStream matterhornAudioStream = (AudioStream) stream;
        audioStream.setBitDepth(matterhornAudioStream.getBitDepth());
        audioStream.setBitRate(matterhornAudioStream.getBitRate());
        audioStream.setChannels(matterhornAudioStream.getChannels());
        audioStream.setFormat(matterhornAudioStream.getFormat());
        audioStream.setSamplingRate(matterhornAudioStream.getSamplingRate());
        content.addStream(audioStream);
      } else if (stream instanceof VideoStream) {
        ch.entwine.weblounge.common.content.movie.VideoStream videoStream = new VideoStreamImpl();
        VideoStream matterhornVideoStream = (VideoStream) stream;
        videoStream.setBitRate(matterhornVideoStream.getBitRate());
        videoStream.setFormat(matterhornVideoStream.getFormat());
        videoStream.setFrameHeight(matterhornVideoStream.getFrameHeight());
        videoStream.setFrameWidth(matterhornVideoStream.getFrameWidth());
        videoStream.setFrameRate(matterhornVideoStream.getFrameRate());
        videoStream.setScanType(ScanType.fromString(matterhornVideoStream.getScanType().toString()));
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
    Language language = LanguageUtils.getLanguage(mediaPackage.getLanguage());

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
