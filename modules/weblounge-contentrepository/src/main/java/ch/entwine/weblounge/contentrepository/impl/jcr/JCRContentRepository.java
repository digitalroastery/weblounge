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
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository;

import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Dictionary;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

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
  
  /** The site */
  private Site site = null;

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
    this.site = site;
    
    if (repository != null) {
      logger.warn("JCR repository '' already seams to be connected", repository);
    }

    if (repositoryUrl == null || repositoryCred == null)
      throw new ContentRepositoryException("Repository not properly configured. Either url or credentials are missing.");

    repository = new URLRemoteRepository(repositoryUrl);
    try {
      session = repository.login(repositoryCred);
      logger.debug("Successfuly connected to repository as user '{}'", session.getUserID());
    } catch (LoginException e) {
      throw new ContentRepositoryException("Login to the repository failed", e);
    } catch (RepositoryException e) {
      throw new ContentRepositoryException(e);
    }
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
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#listResources()
   */
  @Override
  protected Collection<ResourceURI> listResources()
      throws ContentRepositoryException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadResource(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  protected InputStream loadResource(ResourceURI uri)
      throws ContentRepositoryException, IOException {
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (repositoryUrl != null)
      return MessageFormat.format("JCR repository({0})", repositoryUrl.toString());
    else
      return "JCR repository (not yet configured)";
  }

}
