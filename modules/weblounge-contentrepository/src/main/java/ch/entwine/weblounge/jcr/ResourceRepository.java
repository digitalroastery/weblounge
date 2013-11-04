/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package ch.entwine.weblounge.jcr;

import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.InvalidNodeTypeDefinitionException;
import javax.jcr.nodetype.NodeTypeExistsException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

/**
 * TODO: Comment ResourceRepository
 */
public abstract class ResourceRepository {

  private static final String SITES_ROOT_NODE_REL_PATH = "sites";

  /** The underlying JCR content repository */
  protected Repository repository = null;

  /** Credentials needed to connect to the JCR content repository */
  protected Credentials cred = null;

  // /** Flag which indicates if everything is ready */
  // private boolean isReady = false;

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(ResourceRepository.class);

  /**
   * OSGi callback to set the underlying JCR content repository
   * 
   * @param repository
   *          the content repository
   * @throws RepositoryException
   * @throws ContentRepositoryException
   */
  @SuppressWarnings("unchecked")
  protected void setRepository(Repository repository) {
    this.repository = repository;
    this.cred = new SimpleCredentials("admin", "admin".toCharArray());

    try {
      Session session = getSession();

      NamespaceRegistry nsRegistry = session.getWorkspace().getNamespaceRegistry();
      nsRegistry.registerNamespace("webl", "http://entwine.ch/weblounge/jcr");
      
//      NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
//      
//
//      /* Create node type */
//      NodeTypeTemplate nodeType = nodeTypeManager.createNodeTypeTemplate();
//      nodeType.setName("webl:Resource");
//
//      /* Create a new property */
//      PropertyDefinitionTemplate layoutProperty = nodeTypeManager.createPropertyDefinitionTemplate();
//      layoutProperty.setName("webl:layout");
//      layoutProperty.setRequiredType(PropertyType.STRING);
//      /* Add property to node type */
//      nodeType.getPropertyDefinitionTemplates().add(layoutProperty);
//
//      /* Create a new property */
//      PropertyDefinitionTemplate templateProperty = nodeTypeManager.createPropertyDefinitionTemplate();
//      templateProperty.setName("webl:template");
//      templateProperty.setRequiredType(PropertyType.STRING);
//      /* Add property to node type */
//      nodeType.getPropertyDefinitionTemplates().add(templateProperty);
//
//      nodeTypeManager.registerNodeType(nodeType, false);
    } catch (ContentRepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected Session getSession() throws ContentRepositoryException {
    if (repository == null)
      throw new ContentRepositoryException("Error while getting a session to the JCR repository because the repository is missing.");

    try {
      return repository.login(cred);
    } catch (LoginException e) {
      log.warn(e.getMessage());
      throw new ContentRepositoryException("Error while getting a session to the JCR repository", e);
    } catch (RepositoryException e) {
      log.warn(e.getMessage());
      throw new ContentRepositoryException("Error while getting a session to the JCR repository", e);
    }
  }

  protected Node getResourcesNode(Site site) throws ContentRepositoryException {
    Session session = getSession();

    Node siteNode;
    Node resourcesNode;

    try {
      siteNode = getSiteNode(site);
      if (!siteNode.hasNode("resources")) {
        siteNode.addNode("resources");
        session.save();
        log.info("Resources base node for site '{}' added to JCR repository", site.getIdentifier());
      }

      resourcesNode = siteNode.getNode("resources");
    } catch (RepositoryException e) {
      throw new ContentRepositoryException("There was a problem getting the base resources node of the site '" + site.getIdentifier() + "'", e);
    }

    return resourcesNode;
  }

  protected Node getSiteNode(Site site) throws ContentRepositoryException {
    Session session = getSession();

    Node sitesNode;
    Node siteNode;

    try {
      sitesNode = getSitesNode();
      if (!sitesNode.hasNode(site.getIdentifier())) {
        sitesNode.addNode(site.getIdentifier());
        session.save();
        log.info("Base node for site '{}' added to JCR repository", site.getIdentifier());
      }

      siteNode = sitesNode.getNode(site.getIdentifier());
    } catch (RepositoryException e) {
      throw new ContentRepositoryException("There was a problem getting the base node of the site '" + site.getIdentifier() + "'", e);
    }

    return siteNode;
  }

  private Node getSitesNode() throws ContentRepositoryException {
    Session session = getSession();

    Node rootNode;
    Node sitesRootNode;

    try {
      rootNode = session.getRootNode();
      if (!rootNode.hasNode(SITES_ROOT_NODE_REL_PATH)) {
        rootNode.addNode(SITES_ROOT_NODE_REL_PATH);
        session.save();
        log.info("Base node for sites added to JCR repository");
      }

      sitesRootNode = rootNode.getNode(SITES_ROOT_NODE_REL_PATH);
    } catch (RepositoryException e) {
      throw new ContentRepositoryException("There was a problem getting the root node of the sites", e);
    }

    return sitesRootNode;
  }

}
