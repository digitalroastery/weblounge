package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.bridge.oaipmh.harvester.HarvesterException;
import ch.entwine.weblounge.bridge.oaipmh.harvester.ListRecordsResponse;
import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.movie.VideoStreamImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opencastproject.mediapackage.AudioStream;
import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageBuilder;
import org.opencastproject.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.Stream;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.VideoStream;
import org.w3c.dom.Node;

import java.net.MalformedURLException;

/**
 * A Matterhorn specified implementation of a record handler. This class is not
 * thread safe.
 */
public class MatterhornRecordHandler extends AbstractWebloungeRecordHandler implements RecordHandler {

  /** User for resource creation */
  private User harvesterUser = new UserImpl("harvester", "videolounge", "Harvester");

  /** Name of the oai pmh prefix */
  private static final String MATTERHORN_REPOSITORY_PREFIX = "matterhorn";

  /** The media package builder */
  private MediaPackageBuilder mediaPackageBuilder;

  /**
   * Creates a new matterhorn record handler
   * 
   * @param site
   *          the site
   * @param contentRepository
   *          the content repository
   * @param presentationTrackFlavor
   *          the presentation track flavor
   * @param presenterTrackFlavor
   *          the presenter track flavor
   * @param dcEpisodeFlavor
   *          the dublin core episode flavor
   * @param dcSeriesFlavor
   *          the dublin core series flavor
   */
  public MatterhornRecordHandler(Site site,
      WritableContentRepository contentRepository,
      String presentationTrackFlavor, String presenterTrackFlavor,
      String dcEpisodeFlavor, String dcSeriesFlavor) {
    super(site, contentRepository, presentationTrackFlavor, presenterTrackFlavor, dcEpisodeFlavor, dcSeriesFlavor);
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
   * @see ch.entwine.weblounge.bridge.oaipmh.AbstractWebloungeRecordHandler#parseResource()
   */
  protected Resource<?> parseResource(Node record) {
    Node mediaPackageNode = ListRecordsResponse.metadataOfRecord(record);
    MediaPackage mediaPackage;
    try {
      mediaPackage = mediaPackageBuilder.loadFromXml(mediaPackageNode);
    } catch (MediaPackageException e) {
      logger.warn("Error loading mediapackage from record");
      throw new RuntimeException(e);
    }

    Language language = getISO3Language(mediaPackage.getLanguage());

    MovieResourceImpl movieResource = new MovieResourceImpl(new MovieResourceURIImpl(site));
    movieResource.setCreated(harvesterUser, mediaPackage.getDate());

    // Set Header Metadata
    movieResource.setTitle(mediaPackage.getTitle(), language);
    for (String subject : mediaPackage.getSubjects()) {
      movieResource.addSubject(subject);
    }

    if (StringUtils.isNotBlank(mediaPackage.getSeries()))
      movieResource.addSeries(mediaPackage.getSeries());
    if (StringUtils.isNotBlank(mediaPackage.getSeriesTitle()))
      movieResource.setDescription(mediaPackage.getSeriesTitle(), language);

    Catalog episodeCatalog = mediaPackage.getCatalog(dcEpisodeFlavor);

    Catalog seriesCatalog = mediaPackage.getCatalog(dcSeriesFlavor);
    // movieResource.addSeries(series)
    // getRightsHolder
    // movieResource.setCoverage(coverage, language)
    // movieResource.setRights(rights, language)
    return movieResource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.AbstractWebloungeRecordHandler#parseResourceContent()
   */
  protected ResourceContent parseResourceContent(Node record) {
    Node mediaPackageNode = ListRecordsResponse.metadataOfRecord(record);
    MediaPackage mediaPackage;
    try {
      mediaPackage = mediaPackageBuilder.loadFromXml(mediaPackageNode);
    } catch (MediaPackageException e) {
      logger.warn("Error loading mediapackage from record");
      throw new RuntimeException(e);
    }

    Language language = getISO3Language(mediaPackage.getLanguage());

    // TODO: Use tracks with correct flavor for movie
    // ??? presenterTrackFlavor
    String[] presentationTrack = presentationTrackFlavor.split("/");
    MediaPackageElementFlavor elementFlavor = new MediaPackageElementFlavor(presentationTrack[0], presentationTrack[1]);

    // Set Content
    MediaPackageElement[] mediaPackageElements = mediaPackage.getElementsByFlavor(elementFlavor);
    if (mediaPackageElements.length < 1)
      return null;
    MediaPackageElement element = mediaPackageElements[0];
    MovieContent content = new MovieContentImpl(FilenameUtils.getBaseName(element.getURI().toString()), language, element.getMimeType().asString());
    StringBuilder author = new StringBuilder();
    String[] creators = mediaPackage.getCreators();
    for (int i = 0; i < creators.length; i++) {
      if (i != 0)
        author.append(", ");
      author.append(creators[i]);
    }

    try {
      content.setExternalLocation(element.getURI().toURL());
    } catch (MalformedURLException e) {
      logger.debug("No record url for element {}", element.getIdentifier());
      throw new HarvesterException("No record url for element: " + element.getIdentifier());
    }
    content.setAuthor(author.toString());
    if (element.getSize() != -1)
      content.setSize(element.getSize());
    if (mediaPackage.getDuration() != -1)
      content.setDuration(mediaPackage.getDuration());

    Track[] tracks = mediaPackage.getTracks(new MediaPackageElementFlavor(presentationTrack[0], presentationTrack[1]));
    if (tracks.length < 1)
      return null;
    Track track = tracks[0];
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
    content.setCreationDate(mediaPackage.getDate());
    return content;
  }
}
