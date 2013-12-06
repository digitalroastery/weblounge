package ch.entwine.weblounge.jcr.impl.serializer;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;

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
    return new Class[] { Page.class, PageImpl.class };
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

    if (node == null || resource == null)
      throw new IllegalArgumentException("Neither node nor resource parameter must be null");

    Page page = null;
    if (resource instanceof Page)
      page = (Page) resource;
    else
      throw new IllegalArgumentException("This Resource Serializer only supports Resources of the type Page");

    storeResource(node, resource);

    try {
      // Set page specific properties
      node.setProperty("resource-type", "page");
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
   * @see ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer#read(javax.jcr.Node, ResourceURI)
   */
  @Override
  public Page read(Node node, ResourceURI uri) throws ContentRepositoryException {
    if (node == null || uri == null)
      throw new IllegalArgumentException("Node parameter must not be null");

    Page page = new PageImpl(uri);
    readResource(node, page);

    try {
      page.setLayout(node.getProperty("layout").getString());
      page.setTemplate(node.getProperty("template").getString());
      page.setStationary(node.getProperty("stationary").getBoolean());
    } catch (RepositoryException e) {
      log.warn("Error while trying to read page from JCR node '{}'", node);
      throw new ContentRepositoryException("Error while trying to read page from JCR node", e);
    }

    return page;
  };

}
