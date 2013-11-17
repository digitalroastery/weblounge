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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceRepresentation;
import ch.entwine.weblounge.common.content.ResourceRepresentationCharacteristic;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

/**
 * This is a base class for all kind of resource specific repository
 * implementations.
 */
public abstract class AbstractResourceRepository {

  /** URI of the Weblounge JCR namespace */
  private static final String WEBLOUNGE_JCR_NS_URI = "http://entwine.ch/weblounge/jcr";

  /** Name of the Weblounge JCR namespace */
  private static final String WEBLOUNGE_JCR_NS_NAME = "webl";

  /** The underlying JCR content repository */
  protected Repository repository = null;

  /** Credentials needed to connect to the JCR content repository */
  protected Credentials cred = null;

  /** Flag which indicates if repository is already initialized */
  private boolean repositoryInitialized = false;

  /** Holds a list of all sites this repository is able to serve */
  protected List<Site> sites = new ArrayList<Site>();

  /** The resource serializer registry */
  protected JCRResourceSerializerRegistry serializerRegistry = null;

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(AbstractResourceRepository.class);

  /**
   * OSGi callback to set the underlying JCR content repository
   * 
   * @param repository
   *          the JCR repository
   */
  protected void bindRepository(Repository repository) {
    this.repository = repository;
    log.info("Set '{}' as JCR repository", repository.toString());

    // TODO Set credentials by configuration
    this.cred = new SimpleCredentials("admin", "admin".toCharArray());
    log.info("Credentials for JCR user 'admin' set");

    try {
      initRepository();
    } catch (ContentRepositoryException e) {
      log.error("Initializing the repository failed: {}", e.getMessage());
    }
  }

  /**
   * OSGi callback to bind a {@link JCRResourceSerializerRegistry} to this
   * repository.
   * 
   * @param serializerRegistry
   *          the registry
   */
  protected void bindJCRResourceSerializerRegistry(
      JCRResourceSerializerRegistry serializerRegistry) {
    this.serializerRegistry = serializerRegistry;
    log.info("Resource serializer registry set ({})", serializerRegistry);
  }

  /**
   * OSGi callback to bind a site to this repository.
   * 
   * @param site
   *          the site to bind to this repository
   */
  protected void bindSite(Site site) {
    if (sites.contains(site)) {
      log.warn("Site '{}' is already bound to this repository", site.getIdentifier());
      return;
    }

    log.info("Binding new site '{}' to this repository", site.getIdentifier());
    try {
      initRepositoryForSite(site);
      sites.add(site);
      log.info("Site '{}' successfully bound to this repository", site.getIdentifier());
    } catch (ContentRepositoryException e) {
      log.error("Initializing the repository for the site '{}' failed: {}", site.getIdentifier(), e.getMessage());
    }
  }

  /**
   * OSGi callback to unbind a site from this repository.
   * 
   * @param site
   *          the site to unbind
   */
  protected void unbindSite(Site site) {
    if (!sites.contains(site))
      log.warn("Tryed to unbind site '{}' which has never been bound to this repository", site.getIdentifier());

    sites.remove(site);
    log.info("Site '{}' unbound from this repository", site.getIdentifier());
  }

  public Resource<?> addResource(ResourceURI uri, Resource<?> resource)
      throws ContentRepositoryException {

    if (uri == null || resource == null)
      throw new IllegalArgumentException("Parameters uri and resource must not be null");

    try {
      Session session = getSession();

      // We need to check, if there's already a resource at the parents path
      // since each node (resource) needs to have a direct parent node
      String absPathParentNode = JCRResourceUtils.getAbsParentNodePath(resource.getURI());
      if (!session.nodeExists(absPathParentNode)) {
        log.warn("Tried to add resource with path '{}', but parent resource does not exist.", resource.getPath());
        throw new ContentRepositoryException("Tried to add resource wit path '" + resource.getPath() + "', but parent resource does not exist.");
      }
      Node parentNode = session.getNode(absPathParentNode);
      Node pageNode = parentNode.addNode(JCRResourceUtils.getNodeName(resource.getURI()), JcrConstants.NT_UNSTRUCTURED);

      // FIXME How can we get type of resource?
      JCRResourceSerializer serializer = serializerRegistry.getSerializer(resource.getClass());
      serializer.store(pageNode, resource);

      // Make node versionable
      pageNode.addMixin(JcrConstants.MIX_VERSIONABLE);

      session.save();

      // Check-in resource (setting version to 1.0)
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      versionManager.checkin(pageNode.getPath());

      // We need to set the identifier of the resource to the newly generated
      // identifier of the node
      resource.setIdentifier(pageNode.getIdentifier());
      log.info("Identifier of new resource '{}' set to '{}'", resource.getPath(), resource.getIdentifier());
    } catch (RepositoryException e) {
      log.warn("Resource could not be added: {}", e.getMessage());
      throw new ContentRepositoryException("Resource could not be added", e);
    }

    return resource;
  }

  public <T extends ResourceRepresentation> T createRepresentation(
      ResourceURI uri, Class<T> type) {
    return null;
  }

  public <T extends ResourceRepresentation> List<T> getRepresentation(
      ResourceURI uri, Class<T> type,
      ResourceRepresentationCharacteristic... characteristics) {
    return null;
  }

  public Map<Class<ResourceRepresentation>, List<ResourceRepresentationCharacteristic>> listRepresentations(
      ResourceURI uri) {
    return null;
  }

  public <T extends ResourceRepresentation> long[] getRepresentationSize(
      ResourceURI uri, Class<T> type,
      ResourceRepresentationCharacteristic... characteristics) {
    return null;
  }

  public <T extends ResourceRepresentation> T updateRepresentation(
      ResourceURI uri, T representation) {
    return null;
  }

  public <T extends ResourceRepresentation> boolean deleteRepresentation(
      ResourceURI uri, T representation) {
    return false;
  }

  public List<String> getVersions(ResourceURI uri)
      throws ContentRepositoryException {

    if (StringUtils.isEmpty(uri.getPath()))
      throw new IllegalArgumentException("The given ResourceURI must have a path set");

    Session session = getSession();

    try {
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      VersionHistory history = versionManager.getVersionHistory(JCRResourceUtils.getAbsNodePath(uri));
      VersionIterator versions = history.getAllVersions();

      List<String> versionsList = new ArrayList<String>();
      while (versions.hasNext()) {
        Version version = versions.nextVersion();
        if (version.getName().equals(JcrConstants.JCR_ROOTVERSION))
          continue;
        versionsList.add(version.getName());
      }

      return versionsList;
    } catch (RepositoryException e) {
      log.warn("Problem while reading the versions of the resource '{}'", uri);
      throw new ContentRepositoryException("Problem while reading the versions of the resource " + uri.toString(), e);
    }
  }

  public void publishResource(ResourceURI uri, String version) {

  }

  /**
   * Returns a valid session to the underlying JCR repository. The returned
   * session should only be used for one operation (e.g. saving a new resource)
   * and should not be cached nor pooled.
   * <p>
   * When the session is no longer used, it should be closed by calling
   * <code>session.logout()</code>
   * 
   * @return a JCR session
   * @throws ContentRepositoryException
   *           if logging into the JCR repository is not possible
   */
  protected Session getSession() throws ContentRepositoryException {
    if (repository == null) {
      log.error("The reference to the JCR repository is missing - this is a blocker!");
      throw new IllegalStateException("Error while getting a session to the JCR repository because the repository is missing.");
    }

    try {
      return repository.login(cred);
    } catch (LoginException e) {
      log.error("The given credentials ({}) are not valid: {}", cred, e.getMessage());
      throw new ContentRepositoryException("Error while getting a session to the JCR repository", e);
    } catch (RepositoryException e) {
      log.error("There was a general problem with the JCR repository: {}", e.getMessage());
      throw new ContentRepositoryException("Error while getting a session to the JCR repository", e);
    }
  }

  /**
   * Initializes the repository for the given site.
   * 
   * @param site
   *          the site
   * @throws ContentRepositoryException
   */
  private void initRepositoryForSite(Site site)
      throws ContentRepositoryException {

    log.info("Start initializing repository for site '{}'", site.getIdentifier());

    Session session = getSession();

    try {
      // Creating base node for given site
      Node sitesNode = getSitesNode(session);
      if (!sitesNode.hasNode(site.getIdentifier())) {
        log.info("Repository has never been used with site '{}'", site.getIdentifier());
        sitesNode.addNode(site.getIdentifier());
        session.save();
        log.info("Base node for site '{}' added to JCR repository", site.getIdentifier());
      }

    } catch (RepositoryException e) {
      log.error("There was a problem creating the base not for the site '{}': {}", site.getIdentifier(), e.getMessage());
      throw new ContentRepositoryException("There was a problem creating the base not for the site '" + site.getIdentifier() + "'", e);
    }

    // Creating homepage
    try {
      Node siteNode = getSiteNode(session, site);
      Node homepage;
      if (!siteNode.hasNode(JCRResourceConstants.RESOURCES_NODE_NAME)) {
        homepage = siteNode.addNode(JCRResourceConstants.RESOURCES_NODE_NAME);
        homepage.addMixin(JcrConstants.MIX_VERSIONABLE);
        session.save();
        log.info("Homepage for site '{}' added to JCR repository", site.getIdentifier());
      }
    } catch (RepositoryException e) {
      log.error("There was a problem creating the homepage of the site '{}': {}", site.getIdentifier(), e.getMessage());
      throw new ContentRepositoryException("There was a problem creating the homepage of the site '" + site.getIdentifier() + "'", e);
    }

  }

  /**
   * Returns the resources node of a site. The resources is the parent node of
   * all resources inside the repository.
   * 
   * @param session
   *          the JCR session to get the node
   * @param site
   *          the site
   * @return the resources node
   * @throws ContentRepositoryException
   *           if there's a problem getting the resources node
   */
  protected Node getResourcesNode(Session session, Site site)
      throws ContentRepositoryException {
    Node siteNode = getSiteNode(session, site);
    try {
      return siteNode.getNode(JCRResourceConstants.RESOURCES_NODE_NAME);
    } catch (PathNotFoundException e) {
      log.error("No homepage for site '{}' found", site.getIdentifier());
      throw new ContentRepositoryException(e);
    } catch (RepositoryException e) {
      log.error("Error while getting homepage for site '{}': {}", site.getIdentifier(), e.getMessage());
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * Returns the home node of a site.
   * 
   * @param session
   *          the JCR session to get the node
   * @param site
   *          the site
   * @return the home node
   * @throws ContentRepositoryException
   *           if there's a problem getting the home node
   */
  protected Node getSiteNode(Session session, Site site)
      throws ContentRepositoryException {
    try {
      return getSitesNode(session).getNode(site.getIdentifier());
    } catch (RepositoryException e) {
      log.error("There was a problem getting the base node of the site '{}': ", site.getIdentifier(), e.getMessage());
      throw new ContentRepositoryException("There was a problem getting the base node of the site '" + site.getIdentifier() + "'", e);
    }
  }

  /**
   * Does the basic initialization of the JCR repository. Registers namespaces
   * and node types.
   * 
   * @throws ContentRepositoryException
   *           if there's a problem initializing the repository
   */
  private void initRepository() throws ContentRepositoryException {
    if (repositoryInitialized) {
      log.debug("Repository is already properly initialized");
      return;
    }

    Session session = getSession();

    // Register Weblounge namespace
    try {
      NamespaceRegistry nsRegistry = session.getWorkspace().getNamespaceRegistry();
      nsRegistry.registerNamespace(WEBLOUNGE_JCR_NS_NAME, WEBLOUNGE_JCR_NS_URI);
      session.save();
      log.info("Registered namespace '{}' with uri '{}'", WEBLOUNGE_JCR_NS_NAME, WEBLOUNGE_JCR_NS_URI);
    } catch (RepositoryException e) {
      log.warn("Error while trying to register namespace '{}': {}", WEBLOUNGE_JCR_NS_NAME, e.getMessage());
      throw new ContentRepositoryException(e);
    }

    // Check, if base node for sites exists; create it otherwise
    try {
      Node rootNode = session.getRootNode();
      if (!rootNode.hasNode(JCRResourceConstants.SITES_ROOT_NODE_REL_PATH)) {
        log.info("No base node for sites found. JCR repository has never been used with Weblounge");
        Node sitesNode = rootNode.addNode(JCRResourceConstants.SITES_ROOT_NODE_REL_PATH);
        session.save();
        log.info("Base node '{}' for sites added to JCR repository", sitesNode.getPath());
      }
    } catch (RepositoryException e) {
      log.error("There was an error while creating the root node for the sites:  {}", e.getMessage());
      throw new ContentRepositoryException("There was an error while creating the root node for the sites", e);
    }

    log.info("Basic initialization of the JCR repository successfully finished");
    repositoryInitialized = true;
  }

  /**
   * Returns the base node of all sites.
   * 
   * @param session
   *          session used to access the repository
   * @return the base node of all sites
   * @throws ContentRepositoryException
   *           if the node can not be found
   */
  private Node getSitesNode(Session session) throws ContentRepositoryException {
    try {
      return session.getRootNode().getNode(JCRResourceConstants.SITES_ROOT_NODE_REL_PATH);
    } catch (RepositoryException e) {
      log.error("There was a problem getting the base node of the sites: {}", e.getMessage());
      throw new ContentRepositoryException("There was a problem getting the base node of the sites", e);
    }
  }

}
