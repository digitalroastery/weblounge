/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.contentrepository.impl.bundle;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.user.WebloungeUser;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository;

import org.apache.commons.io.FilenameUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Represents a bundle repository that is writable. OSGi bundles are not
 * writable, so this content repository behaves like a
 * <code>FileSystemContentRepository</code>, while the initial content is copied
 * from the respective bundle.
 */
public class WritableBundleContentRepository extends FileSystemContentRepository {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WritableBundleContentRepository.class);

  /** Prefix into the bundle */
  protected String bundlePathPrefix = "/repository";

  /** Prefix into the bundle */
  protected String pagesPathPrefix = bundlePathPrefix + "/pages";

  /** The bundle */
  protected Bundle bundle = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository#connect(java.util.Dictionary)
   */
  @Override
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {

    super.connect(properties);

    // Determine the bundle
    this.bundle = (Bundle) properties.get(Bundle.class.getName());
    if (bundle == null)
      throw new ContentRepositoryException("Bundle was not found in connect properties");
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#start()
   */
  @Override
  public void start() throws ContentRepositoryException {
    super.start();

    // See if there are any pages. If that's the case, then we don't need to
    // do anything. If not, we need to copy everything that's currently in the
    // bundle.
    String pagesPath = UrlSupport.concat(repositoryRoot.getAbsolutePath(), REPOSITORY_PATH);
    File pagesDirectory = new File(pagesPath);
    if (pagesDirectory.isDirectory() && pagesDirectory.list().length > 0) {
      logger.debug("Found existing content for site '{}' at {}", site, pagesPath);
      return;
    }

    // If there is no content repository at the target location, copy the
    // initial bundle contents to the filesystem. Otherwise, keep working with
    // what's there already.
    logger.info("Loading pages for '{}' from bundle {}", site, bundle);
    WebloungeUser user = site.getAdministrator();
    for (Iterator<ResourceURI> pi = getURIsFromBundle(); pi.hasNext();) {
      ResourceURI uri = pi.next();
      try {
        Resource page = loadPageFromBundle(uri);
        put(page, user);
      } catch (SecurityException e) {
        logger.error("Security error reading page " + uri + ": " + e.getMessage(), e);
        throw new ContentRepositoryException(e);
      } catch (IOException e) {
        logger.error("Error reading page " + uri + ": " + e.getMessage(), e);
        throw new ContentRepositoryException(e);
      }
    }
  }

  /**
   * Loads all pages from the bundle and returns their uris as an iterator.
   * 
   * @return the page uris
   * @throws ContentRepositoryException
   *           if reading from the repository fails
   */
  @SuppressWarnings("unchecked")
  protected Iterator<ResourceURI> getURIsFromBundle()
      throws ContentRepositoryException {
    // TODO: Handle resource URIs
    ResourceURI homeURI = new PageURIImpl(getSite(), "/");
    String entryPath = UrlSupport.concat(pagesPathPrefix, homeURI.getPath());
    List<ResourceURI> pageURIs = new ArrayList<ResourceURI>();
    Enumeration<URL> entries = bundle.findEntries(entryPath, "*.xml", true);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        URL entry = entries.nextElement();
        String path = FilenameUtils.getPath(entry.getPath());
        path = path.substring(pagesPathPrefix.length() - 1);
        long v = ResourceUtils.getVersion(FilenameUtils.getBaseName(entry.getPath()));
        ResourceURI pageURI = new PageURIImpl(site, path, v);
        pageURIs.add(pageURI);
        logger.trace("Found revision '{}' of page {}", v, entry);
      }
    }
    return pageURIs.iterator();
  }

  /**
   * Loads the specified page from the bundle rather than from the content
   * repository.
   * 
   * @param uri
   *          the uri
   * @return the page
   * @throws IOException
   *           if reading the page fails
   */
  protected Page loadPageFromBundle(ResourceURI uri) throws IOException {
    String uriPath = uri.getPath();

    if (uriPath == null)
      throw new IllegalArgumentException("Page uri needs a path");

    String entryPath = UrlSupport.concat(new String[] {
        pagesPathPrefix,
        uriPath,
        ResourceUtils.getDocument(uri.getVersion()) });
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    try {
      PageReader pageReader = new PageReader();
      return pageReader.read(uri, url.openStream());
    } catch (SAXException e) {
      throw new RuntimeException("SAX error while reading page '" + uri + "'", e);
    } catch (IOException e) {
      throw new IOException("I/O error while reading page '" + uri + "'", e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Parser configuration error while reading page '" + uri + "'", e);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
