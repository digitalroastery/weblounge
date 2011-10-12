package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.bridge.oaipmh.harvester.ListRecordsResponse;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class SwisseducastRecordHandler extends WebloungeRecordHandler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(SwisseducastRecordHandler.class);

  /** User for resource creation */
  private User harvesterUser = new UserImpl("harvester");

  /** Name of the oai pmh prefix */
  private static final String SWISSEDUCAST_REPOSITORY_PREFIX = "swisseducast";

  public SwisseducastRecordHandler(Site site,
      WritableContentRepository contentRepository) {
    super(site, contentRepository);
  }

  public String getMetadataPrefix() {
    return SWISSEDUCAST_REPOSITORY_PREFIX;
  }

  public void handle(Node record) {
    String recordIdentifier = ListRecordsResponse.identifierOfRecord(record);
    boolean isDeleted = ListRecordsResponse.statusOfRecord(record);

    // TODO implement swisseducast parser
  }

}
