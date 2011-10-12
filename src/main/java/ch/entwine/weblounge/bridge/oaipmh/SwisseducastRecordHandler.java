package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
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

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.WebloungeRecordHandler#prepareRecord(org.w3c.dom.Node)
   */
  protected void prepareRecord(Node record) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.WebloungeRecordHandler#parseResource()
   */
  protected Resource<?> parseResource(Node record) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.bridge.oaipmh.WebloungeRecordHandler#parseResourceContent()
   */
  protected ResourceContent parseResourceContent(Node record) {
    // TODO Auto-generated method stub
    return null;
  }

}
