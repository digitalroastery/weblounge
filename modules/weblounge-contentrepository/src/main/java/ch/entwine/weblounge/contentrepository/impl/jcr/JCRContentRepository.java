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
package ch.entwine.weblounge.contentrepository.impl.jcr;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ResourceSerializer;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.apache.jackrabbit.value.BinaryValue;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeType;

/**
 * TODO: Comment JCRContentRepository
 */
public class JCRContentRepository extends AbstractWritableContentRepository implements ManagedService {

  /** The repository type */
  public static final String TYPE = "ch.entwine.weblounge.contentrepository.jcr";

  /** Prefix for repository configuration keys */
  private static final String CONF_PREFIX = "contentrepository.jcr.";

  /** Configuration key for the repository's url */
  public static final String OPT_URL = CONF_PREFIX + "url";

  /** Configuration key for the repository's url */
  public static final String OPT_USER = CONF_PREFIX + "user";

  /** Configuration key for the repository's url */
  public static final String OPT_PASSWORD = CONF_PREFIX + "password";

  /** The repository storage root directory */
  protected File repositoryRoot = null;

  /** The repository root directory */
  protected File repositorySiteRoot = null;

  /** URL to the HTTP RMI endpoint of the JCR repository */
  private URL repositoryUrl = null;

  /** Credentials to connect to the repository */
  private Credentials repositoryCred = null;

  /** The JCR repository */
  private Repository repository = null;

  /** The session to the JCR repository */
  private Session session = null;

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(JCRContentRepository.class);

  /**
   * Creates a new instance of the JCR content repository.
   */
  public JCRContentRepository() {
    super(TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void connect(Site site) throws ContentRepositoryException {

    if (repository != null) {
      logger.warn("JCR repository '{}' already seams to be connected", repository);
    }

    if (repositoryUrl == null || repositoryCred == null)
      throw new ContentRepositoryException("Repository not properly configured. Either url or credentials are missing.");

    repository = new URLRemoteRepository(repositoryUrl);
    try {
      session = repository.login(repositoryCred);
    } catch (LoginException e) {
      throw new ContentRepositoryException("Login to the repository failed", e);
    } catch (RepositoryException e) {
      throw new ContentRepositoryException(e);
    }

    logger.debug("Successfuly connected to repository as user '{}'", session.getUserID());

    // Tell the super implementation
    super.connect(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @Override
  public void updated(@SuppressWarnings("rawtypes") Dictionary properties)
      throws ConfigurationException {

    // TODO What should we do if repository/session is already setup?

    String url = (String) properties.get(OPT_URL);
    if (url == null)
      throw new ConfigurationException(OPT_URL, "Required configuration property seems not to be configured.");
    try {
      repositoryUrl = new URL(url);
    } catch (MalformedURLException e) {
      throw new ConfigurationException(OPT_URL, e.getMessage(), e);
    }

    String user = (String) properties.get(OPT_USER);
    if (user == null)
      throw new ConfigurationException(OPT_USER, "Required configuration property seems not to be configured.");
    String password = (String) properties.get(OPT_PASSWORD);
    if (password == null)
      throw new ConfigurationException(OPT_PASSWORD, "Required configuration property seems not to be configured.");
    repositoryCred = new SimpleCredentials(user, password.toCharArray());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResource(ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  protected Resource<?> storeResource(Resource<?> resource)
      throws ContentRepositoryException, IOException {

    String resNodePath = UrlUtils.concat(getBaseNodePath(resource.getType()), resource.getIdentifier(), String.valueOf(resource.getURI().getVersion()));

    try {
      // First of all, we have to make sure, the base node for the given
      // resource exists. For the moment, we want the nodes to be of
      // the node type 'nt:folder'
      Node resBaseNode = JcrUtils.getOrCreateByPath(resNodePath, NodeType.NT_FOLDER, session);

      // We want to store the resource in an own sub-node
      Node resNode = null;
      if (resBaseNode.hasNode("resource"))
        resNode = resBaseNode.getNode("resource");
      else
        resNode = resBaseNode.addNode("resource", NodeType.NT_FILE);

      // The serialized resource is stored as a file in a JCR resource node with
      // the name 'jcr:content'
      Node contentNode = null;
      if (resNode.hasNode(Node.JCR_CONTENT))
        contentNode = resNode.getNode(Node.JCR_CONTENT);
      else
        contentNode = resNode.addNode(Node.JCR_CONTENT, NodeType.NT_RESOURCE);

      contentNode.setProperty(Property.JCR_ENCODING, "UTF-8");
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTime(resource.getLastModified());
      contentNode.setProperty(Property.JCR_LAST_MODIFIED, lastModified);
      contentNode.setProperty(Property.JCR_DATA, new BinaryValue(resource.toXml().getBytes()));

      session.save();

    } catch (RepositoryException e) {
      throw new IOException(e);
    }

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  @Override
  protected ResourceContent storeResourceContent(ResourceURI uri,
      ResourceContent content, InputStream is)
      throws ContentRepositoryException, IOException {

    String resNodePath = UrlUtils.concat(getBaseNodePath(uri.getType()), uri.getIdentifier(), String.valueOf(uri.getVersion()));

    try {
      // First of all, we have to make sure, the base node for the given
      // resource exists. For the moment, we want the nodes to be of
      // the node type 'nt:folder'
      Node resBaseNode = JcrUtils.getOrCreateByPath(resNodePath, NodeType.NT_FOLDER, session);

      // We want to store the resource in an own sub-node
      Node resNode = null;
      if (resBaseNode.hasNode("resource_content"))
        resNode = resBaseNode.getNode("resource_content");
      else
        resNode = resBaseNode.addNode("resource_content", NodeType.NT_FOLDER);

      // Each language needs its own subnode
      Node resLangNode = null;
      String lang = content.getLanguage().getIdentifier();
      if (resNode.hasNode(lang))
        resLangNode = resNode.getNode(lang);
      else
        resLangNode = resNode.addNode(lang, NodeType.NT_FILE);

      // The serialized resource is stored as a file in a JCR resource node with
      // the name 'jcr:content'
      Node contentNode = null;
      if (resLangNode.hasNode(Node.JCR_CONTENT))
        contentNode = resLangNode.getNode(Node.JCR_CONTENT);
      else
        contentNode = resLangNode.addNode(Node.JCR_CONTENT, NodeType.NT_RESOURCE);
      contentNode.setProperty(Property.JCR_ENCODING, "UTF-8");
      contentNode.setProperty(Property.JCR_MIMETYPE, content.getMimetype());
      contentNode.setProperty(Property.JCR_DATA, new BinaryValue(is));

      session.save();

    } catch (RepositoryException e) {
      throw new IOException(e);
    }

    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResource(ch.entwine.weblounge.common.content.ResourceURI,
   *      long[])
   */
  @Override
  protected void deleteResource(ResourceURI uri, long[] revisions)
      throws ContentRepositoryException, IOException {

    String resNodePath = UrlUtils.concat(getBaseNodePath(uri.getType()), uri.getIdentifier());
    try {
      if (!session.nodeExists(resNodePath)) {
        logger.warn("Resource '{}' does not exist", uri.getIdentifier());
        return;
      }

      // Remove the resource versions
      for (long r : revisions) {
        String resVersNodePath = UrlUtils.concat(resNodePath, Long.toString(r));
        if (session.itemExists(resVersNodePath))
          session.removeItem(resVersNodePath);
        else
          logger.warn("Revision '{}' of resource '{}' does not exist", r, uri.getIdentifier());
      }

      // If all versions of the given resource were deleted, delete the resource
      // itself
      Node resNode = session.getNode(resNodePath);
      if (!resNode.hasNodes())
        resNode.remove();

      session.save();

    } catch (RepositoryException e) {
      throw new ContentRepositoryException(e);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  @Override
  protected void deleteResourceContent(ResourceURI uri, ResourceContent content)
      throws ContentRepositoryException, IOException {

    String resContentNodePath = UrlUtils.concat(getBaseNodePath(uri.getType()), uri.getIdentifier(), Long.toString(uri.getVersion()), "resource_content", content.getLanguage().getIdentifier());

    try {
      if (session.itemExists(resContentNodePath))
        session.removeItem(resContentNodePath);
    } catch (RepositoryException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#listResources()
   */
  @Override
  protected Collection<ResourceURI> listResources()
      throws ContentRepositoryException, IOException {
    List<ResourceURI> uris = new ArrayList<ResourceURI>();

    // TODO The current implementation does not yet support versions

    // Add all known resource types to the index
    for (ResourceSerializer<?, ?> serializer : getSerializers()) {

      String resRootNode = getBaseNodePath(serializer.getType());

      NodeIterator it;
      try {
        if (!session.itemExists(resRootNode)) {
          logger.debug("No node with path '{}' found", resRootNode);
          continue;
        }
        it = session.getNode(resRootNode).getNodes();

        // Iterate over all the resource nodes found for the given resource type
        while (it.hasNext()) {
          Node res = it.nextNode();

          // Iterate over all the version nodes of the given resource node
          NodeIterator versions = res.getNodes();
          while (versions.hasNext()) {
            long version;
            String versionName = null;
            try {
              versionName = versions.nextNode().getName();
              version = Long.parseLong(versionName);
            } catch (NumberFormatException e) {
              logger.warn("Invalid version name '{}' found for resource '{}'", versionName, res.getName());
              continue;
            }
            ResourceURI uri = new ResourceURIImpl(serializer.getType(), getSite(), null, res.getName(), version);
            uris.add(uri);
          }

        }
      } catch (PathNotFoundException e) {
        throw new ContentRepositoryException(e);
      } catch (RepositoryException e) {
        throw new ContentRepositoryException(e);
      }

    }

    return uris;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadResource(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  protected InputStream loadResource(ResourceURI uri)
      throws ContentRepositoryException, IOException {

    String resNodePath = UrlUtils.concat(getBaseNodePath(uri.getType()), uri.getIdentifier(), String.valueOf(uri.getVersion()), "resource");
    InputStream res = null;
    try {
      Node resNode = session.getNode(resNodePath);
      Node resContent = resNode.getNode(Node.JCR_CONTENT);
      Property data = resContent.getProperty(Property.JCR_DATA);
      res = data.getBinary().getStream();
    } catch (PathNotFoundException e) {
      throw new ContentRepositoryException(e);
    } catch (RepositoryException e) {
      throw new IOException(e);
    }

    return res;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.language.Language)
   */
  @Override
  protected InputStream loadResourceContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException {
    String resNodePath = UrlUtils.concat(getBaseNodePath(uri.getType()), uri.getIdentifier(), String.valueOf(uri.getVersion()), "resource_content", language.getIdentifier());
    InputStream content = null;
    try {
      Node resNode = session.getNode(resNodePath);
      Node resContent = resNode.getNode(Node.JCR_CONTENT);
      Property data = resContent.getProperty(Property.JCR_DATA);
      content = data.getBinary().getStream();
    } catch (PathNotFoundException e) {
      throw new ContentRepositoryException(e);
    } catch (RepositoryException e) {
      throw new IOException(e);
    }

    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (repositoryUrl != null)
      return repositoryUrl.toString();
    else
      return super.toString();
  }

  /**
   * Builds the base node path for a given resource type.
   * <p>
   * 
   * If you pass <code>Page</code> as type and the identifier of the site this
   * repository is connected to is 'demo', the returning base node path will be
   * <code>/sites/demo/pages</code>
   * 
   * @param type
   *          the resource type
   * @return the base node path
   */
  private String getBaseNodePath(String type) {
    if (StringUtils.isBlank(type))
      return null;

    StringBuilder baseNodePath = new StringBuilder("/sites/");
    baseNodePath.append(getSite().getIdentifier());
    baseNodePath.append("/");
    baseNodePath.append(StringUtils.lowerCase(type));
    baseNodePath.append("s");

    return baseNodePath.toString();
  }

}
