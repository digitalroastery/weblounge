package ch.entwine.weblounge.jcr.impl.serializer;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * This is a {@link JCRResourceSerializer} for the resource type {@link Page}
 */
public class JCRPageResourceSerializer extends AbstractJCRResourceSerializer {

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(JCRPageResourceSerializer.class);

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer#getSerializableTypes()
   */
  @Override
  public Class[] getSerializableTypes() {
    return new Class[] { Page.class };
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer#store(javax.jcr.Node,
   *      ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  public void store(Node node, Resource<?> resource)
      throws ContentRepositoryException {

    // TODO Finish!!!
    Page page = null;
    if (resource instanceof Page)
      page = (Page) resource;
    else
      return;

    storeResource(node, resource);

    try {
      // Set page specific properties
      node.setProperty("layout", page.getLayout());
      node.setProperty("template", page.getTemplate());
      node.setProperty("stationary", page.isStationary());
    } catch (RepositoryException e) {
      log.warn("Error while trying to store page '{}' in JCR node '{}'", resource, node);
      throw new ContentRepositoryException("Error while trying to store page in JCR node", e);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer#read(javax.jcr.Node)
   */
  @Override
  public Page read(Node node) throws ContentRepositoryException {
    // FIXME Add uri (from node)
    Page page = new PageImpl(null);
    readResource(node, page);

    try {
      page.setLayout(node.getProperty("layout").toString());
      page.setTemplate(node.getProperty("template").toString());
      page.setStationary(node.getProperty("stationary").getBoolean());
    } catch (RepositoryException e) {
      log.warn("Error while trying to read page from JCR node '{}'", node);
      throw new ContentRepositoryException("Error while trying to read page from JCR node", e);
    }

    return page;
  };

}
