package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Date;

/**
 * A Swisseducast LOR implementation of a record handler
 */
public class SwisseducastRecordHandler extends AbstractWebloungeRecordHandler implements RecordHandler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(SwisseducastRecordHandler.class);

  /** Name of the oai pmh prefix */
  private static final String SWISSEDUCAST_REPOSITORY_PREFIX = "chor_dc";

  /** The attributes */
  private String title;
  private String creator;
  private String licence;
  private String publisher;
  private String subject;
  private String type;
  private String source;
  private String discipline;
  private String contributor;
  private String description;
  private String languageCode;
  private String alternativeTitle;
  private String extent;

  /**
   * Creates a new swisseducast record handler
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
  public SwisseducastRecordHandler(Site site,
      WritableContentRepository contentRepository, User harvesterUser,
      String presentationTrackFlavor, String presenterTrackFlavor,
      String dcEpisodeFlavor, String dcSeriesFlavor) {
    super(site, contentRepository, harvesterUser, presentationTrackFlavor, presenterTrackFlavor, dcEpisodeFlavor, dcSeriesFlavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler#getMetadataPrefix()
   */
  public String getMetadataPrefix() {
    return SWISSEDUCAST_REPOSITORY_PREFIX;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.AbstractWebloungeRecordHandler#parseResource()
   */
  protected Resource<MovieContent> parseResource(Node record) {
    Language language = getISO3Language(languageCode);

    MovieResourceImpl movieResource = new MovieResourceImpl(new MovieResourceURIImpl(site));
    movieResource.setCreated(harvesterUser, new Date());

    // Set Header Metadata
    movieResource.setTitle(title, language);
    movieResource.setDescription(description, language);
    movieResource.addSubject(subject);

    return movieResource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.AbstractWebloungeRecordHandler#parseResourceContent()
   */
  protected MovieContent parseResourceContent(Node record) {
    Language language = getISO3Language(languageCode);

    // Set Content
    MovieContent content = new MovieContentImpl(source, language, "video/");
    return content;
  }

}
