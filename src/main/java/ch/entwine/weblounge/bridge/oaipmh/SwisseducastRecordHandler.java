package ch.entwine.weblounge.bridge.oaipmh;

import static ch.entwine.weblounge.bridge.oaipmh.harvester.OaiPmhResponse.createXPath;
import static ch.entwine.weblounge.bridge.oaipmh.harvester.OaiPmhResponse.xpathString;

import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
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
public class SwisseducastRecordHandler extends WebloungeRecordHandler implements RecordHandler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(SwisseducastRecordHandler.class);

  /** User for resource creation */
  private User harvesterUser = new UserImpl("harvester");

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
   * Creates a new swisseducast record handler.
   * 
   * @param site
   *          the site
   * @param contentRepository
   *          the content repository
   */
  public SwisseducastRecordHandler(Site site,
      WritableContentRepository contentRepository) {
    super(site, contentRepository);
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
   * @see ch.entwine.weblounge.bridge.oaipmh.WebloungeRecordHandler#prepareRecord(org.w3c.dom.Node)
   */
  protected void prepareRecord(Node record) {
    title = xpathString(createXPath(), record, "//oai20:metadata/oai20:title/text()");
    creator = xpathString(createXPath(), record, "//oai20:metadata/oai20:creator/text()");
    licence = xpathString(createXPath(), record, "//oai20:metadata/oai20:licence/text()");
    source = xpathString(createXPath(), record, "//oai20:metadata/oai20:source/text()");
    discipline = xpathString(createXPath(), record, "//oai20:metadata/oai20:discipline/text()");
    contributor = xpathString(createXPath(), record, "//oai20:metadata/oai20:contributor/text()");
    publisher = xpathString(createXPath(), record, "//oai20:metadata/oai20:publisher/text()");
    subject = xpathString(createXPath(), record, "//oai20:metadata/oai20:subject/text()");
    description = xpathString(createXPath(), record, "//oai20:metadata/oai20:description/text()");
    languageCode = xpathString(createXPath(), record, "//oai20:metadata/oai20:language/text()");
    alternativeTitle = xpathString(createXPath(), record, "//oai20:metadata/oai20:alternative/text()");
    extent = xpathString(createXPath(), record, "//oai20:metadata/oai20:extent/text()");
    type = xpathString(createXPath(), record, "//oai20:metadata/oai20:type/text()");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.WebloungeRecordHandler#parseResource()
   */
  protected Resource<?> parseResource(Node record) {
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
   * @see ch.entwine.weblounge.bridge.oaipmh.WebloungeRecordHandler#parseResourceContent()
   */
  protected ResourceContent parseResourceContent(Node record) {
    Language language = getISO3Language(languageCode);

    // Set Content
    MovieContent content = new MovieContentImpl(source, language, "video/");
    return content;
  }

}
