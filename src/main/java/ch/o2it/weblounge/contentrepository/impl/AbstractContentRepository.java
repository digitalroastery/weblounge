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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceReader;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ResourceSerializer;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

/**
 * Abstract implementation for read-only content repositories.
 */
public abstract class AbstractContentRepository implements ContentRepository {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractContentRepository.class);

  /** Index into this repository */
  protected ContentRepositoryIndex index = null;

  /** The site */
  protected Site site = null;

  /** Flag indicating the connected state */
  protected boolean connected = false;

  /** Flag indicating the started state */
  protected boolean started = false;

  /** The document builder factory */
  protected final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

  /** The xml transformer factory */
  protected final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  /** Regular expression to match the resource id, path and version */
  protected static final Pattern resourceHeaderRegex = Pattern.compile(".*<\\s*([\\w]*) .*id=\"([a-z0-9-]*)\".*path=\"([^\"]*)\".*version=\"([^\"]*)\".*");

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#connect(java.util.Dictionary)
   */
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {

    site = (Site) properties.get(Site.class.getName());
    if (site == null)
      throw new ContentRepositoryException("Unable to connect content repository without site");

    connected = true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#disconnect()
   */
  public void disconnect() throws ContentRepositoryException {
    connected = false;
  }

  /**
   * {@inheritDoc}
   * 
   * This default implementation triggers loading of the index, so when
   * overwriting, make sure to invoke by calling <code>super.start()</code>.
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#start()
   */
  public void start() throws ContentRepositoryException {
    if (!connected)
      throw new ContentRepositoryException("Cannot start a disconnected content repository");
    if (started)
      throw new ContentRepositoryException("Content repository has already been started");
    try {
      index = loadIndex();
      started = true;
    } catch (IOException e) {
      throw new ContentRepositoryException("Error loading repository index", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * This implementation closes the index, so when overwriting, make sure to
   * invoke by calling <code>super.stop()</code>.
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#stop()
   */
  public void stop() throws ContentRepositoryException {
    if (!connected)
      throw new ContentRepositoryException("Cannot stop a disconnected content repository");
    if (!started)
      throw new ContentRepositoryException("Content repository is already stopped");
    try {
      started = false;
      index.close();
    } catch (IOException e) {
      throw new ContentRepositoryException("Error closing repository index", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    try {
      return index.exists(uri);
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#find(ch.o2it.weblounge.common.content.SearchQuery)
   */
  public SearchResult find(SearchQuery query) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return index.find(query);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public Resource<?> get(ResourceURI uri) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Check if the resource is available
    try {
      if (!index.exists(uri)) {
        return null;
      }
    } catch (IOException e) {
      logger.error("Error looking up uri {}: {}", uri, e.getMessage());
      throw new ContentRepositoryException(e);
    }

    // Make sure we have the correct resource type
    try {
      if (uri.getType() == null) {
        uri.setType(index.getType(uri));
      }
    } catch (IOException e) {
      logger.error("Error looking up type for {}: {}", uri, e.getMessage());
      throw new ContentRepositoryException(e);
    }

    // Load the resource
    InputStream is = null;
    try {
      is = openStreamToResource(uri);
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializer(uri.getType());
      ResourceReader<?, ?> reader = serializer.getReader();
      return reader.read(is, site);
    } catch (Exception e) {
      logger.error("Error loading {}: {}", uri, e.getMessage());
      throw new ContentRepositoryException(e);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException {
    return openStreamToResourceContent(uri, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getVersions(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    try {
      long[] revisions = index.getRevisions(uri);
      ResourceURI[] uris = new ResourceURI[revisions.length];
      int i = 0;
      for (long r : revisions) {
        uris[i++] = new ResourceURIImpl(uri, r);
      }
      return uris;
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getLanguages(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public Language[] getLanguages(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    try {
      if (uri.getVersion() == Resource.LIVE) {
        return index.getLanguages(uri);
      } else {
        Resource<?> r = loadResource(uri);
        if (r == null)
          throw new ContentRepositoryException("Resource " + uri + " cannot be loaded");
        Set<Language> languages = r.languages();
        return languages.toArray(new Language[languages.size()]);
      }
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public Iterator<ResourceURI> list(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return list(uri, Integer.MAX_VALUE, -1);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      long[])
   */
  public Iterator<ResourceURI> list(ResourceURI uri, long version)
      throws ContentRepositoryException {
    return list(uri, Integer.MAX_VALUE, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      int)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level)
      throws ContentRepositoryException {
    return list(uri, level, -1);
  }

  /**
   * {@inheritDoc}
   * 
   * This implementation uses the index to get the list.
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      int, long)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level, long version)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    return index.list(uri, level, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getResourceCount()
   */
  public long getResourceCount() {
    return index != null ? index.getResourceCount() : -1;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#getRevisionCount()
   */
  public long getRevisionCount() {
    return index != null ? index.getRevisionCount() : -1;
  }

  /**
   * Appends the identifier of the form <code>x-y-z-u-v</code> to
   * <code>path</code> as in <code>/&lt;int&gt;/&lt;int&gt;/id</code>, with the
   * "/" being the platform's file separator.
   * 
   * @param id
   *          the identifier
   * @param path
   *          the root path
   * @return the path
   */
  protected StringBuffer appendIdToPath(String id, StringBuffer path) {
    if (id == null)
      throw new IllegalArgumentException("Identifier must not be null");
    path.append(idToDirectory(id));
    return path;
  }

  /**
   * Returns the identifier of the form <code>x-y-z-u-v</code> as a path as in
   * <code>/&lt;int&gt;/&lt;int&gt;/id</code>, with the "/" being the platform's
   * file separator.
   * 
   * @param id
   *          the identifier
   * @return the path
   */
  protected String idToDirectory(String id) {
    if (id == null)
      throw new IllegalArgumentException("Identifier must not be null");
    String[] elements = id.split("-");
    StringBuffer path = new StringBuffer();

    // convert first part of uuid to long and apply modulo 100
    path.append(File.separatorChar);
    path.append(String.valueOf(Long.parseLong(elements[0], 16) % 100));

    // convert second part of uuid to long and apply modulo 10
    path.append(File.separatorChar);
    path.append(String.valueOf(Long.parseLong(elements[1], 16) % 10));

    // append the full uuid as the actual directory
    path.append(File.separatorChar);
    path.append(id);

    return path.toString();
  }

  /**
   * Returns the site that is associated with this repository.
   * 
   * @return the site
   */
  protected Site getSite() {
    return site;
  }

  /**
   * Returns <code>true</code> if the repository is connected.
   * 
   * @return <code>true</code> if the repository is connected
   */
  protected boolean isConnected() {
    return connected;
  }

  /**
   * Returns <code>true</code> if the repository is connected and started.
   * 
   * @return <code>true</code> if the repository is started
   */
  protected boolean isStarted() {
    return connected && started;
  }

  /**
   * Loads and returns the resource from the repository.
   * 
   * @param uri
   *          the resource uri
   * @return the resource
   * @throws IOException
   *           if the resource could not be loaded
   */
  protected abstract InputStream openStreamToResource(ResourceURI uri)
      throws IOException;

  /**
   * Returns the input stream to the resource content identified by
   * <code>uri</code> and <code>language</code> or <code>null</code> if no such
   * resource exists.
   * 
   * @param uri
   *          the resource uri
   * @param language
   *          the content language
   * @return the resource contents
   * @throws IOException
   *           if opening the stream to the resource failed
   */
  protected abstract InputStream openStreamToResourceContent(ResourceURI uri,
      Language language) throws IOException;

  /**
   * Loads the repository index. Depending on the concrete implementation, the
   * index might be located in the repository itself or at any other storage
   * location. It might even be an in-memory index, in which case the repository
   * implementation is in charge of populating the index.
   * 
   * @return the index
   * @throws IOException
   *           if reading or creating the index fails
   * @throws ContentRepositoryException
   *           if populating the index fails
   */
  protected abstract ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException;

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (site != null)
      return site.hashCode();
    else
      return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AbstractContentRepository) {
      AbstractContentRepository repo = (AbstractContentRepository) obj;
      if (site != null) {
        return site.equals(repo.getSite());
      } else {
        return super.equals(obj);
      }
    }
    return false;
  }

  /**
   * Returns the resource that is identified by the given uri.
   * 
   * @param uri
   *          the resource uri
   * @return the resource
   */
  protected Resource<?> loadResource(ResourceURI uri) throws IOException {
    InputStream is = new BufferedInputStream(openStreamToResource(uri));
    try {
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializer(uri.getType());
      ResourceReader<?, ?> reader = serializer.getReader();
      return reader.read(is, site);
    } catch (Exception e) {
      throw new IOException("Error reading resource from " + uri);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Returns the resource that is located at the indicated url.
   * 
   * @param uri
   *          the resource uri
   * @param contentUrl
   *          location of the resource file
   * @return the resource
   */
  protected Resource<?> loadResource(ResourceURI uri, URL contentUrl)
      throws IOException {
    InputStream is = new BufferedInputStream(contentUrl.openStream());
    try {
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializer(uri.getType());
      ResourceReader<?, ?> reader = serializer.getReader();
      return reader.read(is, site);
    } catch (Exception e) {
      throw new IOException("Error reading resource from " + contentUrl);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Returns the resource uri or <code>null</code> if no resource id and/or path
   * could be found on the specified document. This method is intended to serve
   * as a utility method when importing resources.
   * 
   * @param site
   *          the resource uri
   * @param contentUrl
   *          location of the resource file
   * @return the resource uri
   */
  protected ResourceURI loadResourceURI(Site site, URL contentUrl)
      throws IOException {

    InputStream is = null;
    InputStreamReader reader = null;
    try {
      is = new BufferedInputStream(contentUrl.openStream());
      reader = new InputStreamReader(is);
      CharBuffer buf = CharBuffer.allocate(1024);
      reader.read(buf);
      String s = new String(buf.array());
      s = s.replace('\n', ' ');
      Matcher m = resourceHeaderRegex.matcher(s);
      if (m.matches()) {
        long version = ResourceUtils.getVersion(m.group(4));
        return new ResourceURIImpl(m.group(1), site, m.group(3), m.group(2), version);
      }
      return null;
    } finally {
      if (reader != null)
        reader.close();
      IOUtils.closeQuietly(is);
    }
  }

}
