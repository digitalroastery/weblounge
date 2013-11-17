package ch.entwine.weblounge.jcr.impl.serializer;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.page.Page;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JCRPageResourceSerializer extends AbstractJCRResourceSerializer {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer#getSerializableTypes()
   */
  @Override
  public String[] getSerializableTypes() {
    return new String[] { Page.TYPE };
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.impl.serializer.AbstractJCRResourceSerializer#store(javax.jcr.Node,
   *      ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  public void store(Node node, Resource<?> resource) {
    super.store(node, resource);

    // TODO Finish!!!
    Page page = null;
    if (resource instanceof Page)
      page = (Page) resource;
    else
      return;

    try {
      // Set page specific properties
      node.setProperty("layout", page.getLayout());
      node.setProperty("template", page.getTemplate());
      node.setProperty("stationary", page.isStationary());
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    
    
    // TODO Move to JCRPageResourceRepresentationSerializer
//    try {
//      if (!node.hasNode("webl:composers"))
//        node.addNode("webl:composers");
//
//      Node composers = node.getNode("webl:composers");
//
//      for (Composer composer : page.getComposers()) {
//        if (!composers.hasNode(composer.getIdentifier()))
//          composers.addNode(composer.getIdentifier());
//        
//        Node composerNode = composers.getNode(composer.getIdentifier());
//        
//        for (Pagelet pagelet : composer.getPagelets()) {
//          if (!composerNode.hasNode(relPath))
//        }
//      }
//
//    } catch (ItemExistsException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (PathNotFoundException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (VersionException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (ConstraintViolationException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (LockException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (RepositoryException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.impl.serializer.AbstractJCRResourceSerializer#read(javax.jcr.Node,
   *      ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  public void read(Node node, Resource<?> resource) {
    // TODO Auto-generated method stub
    super.read(node, resource);
  };

}
