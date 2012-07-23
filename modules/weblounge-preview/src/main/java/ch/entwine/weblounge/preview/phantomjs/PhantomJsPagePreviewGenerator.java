/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.preview.phantomjs;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PagePreviewGenerator;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.process.ProcessExcecutorException;
import ch.entwine.weblounge.common.impl.util.process.ProcessExecutor;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A <code>PreviewGenerator</code> that will generate previews for pages.
 */
public class PhantomJsPagePreviewGenerator implements PagePreviewGenerator {

  /** Logger factory */
  private static final Logger logger = LoggerFactory.getLogger(PhantomJsPagePreviewGenerator.class);

  /** Page request handler path prefix */
  protected static final String PAGE_HANDLER_PREFIX = "/weblounge-pages/";

  /** Format for the preview images */
  private static final String PREVIEW_FORMAT = "png";

  /** Format for the preview images */
  private static final String PREVIEW_CONTENT_TYPE = "image/png";

  /** Name of the script parameter */
  private static final String PARAM_PREPARE_SCRIPT = "prepare.js";

  /** Name of the script */
  private static final String SCRIPT_FILE = "/phantomjs/render.js";

  /** The preview generators */
  private final List<ImagePreviewGenerator> previewGenerators = new ArrayList<ImagePreviewGenerator>();

  /** The preview generator service tracker */
  private ServiceTracker previewGeneratorTracker = null;

  /** The script template */
  private String scriptTemplate = null;

  /** Directory containing temporary files */
  private File phantomTmpDir = null;

  /** The script */
  private File scriptFile = null;

  /**
   * Called by the {@link PhantomJsActivator} on service activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    try {
      prepareScript();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    previewGeneratorTracker = new ImagePreviewGeneratorTracker(ctx.getBundleContext());
    previewGeneratorTracker.open();
  }

  /**
   * Called by the {@link PhantomJsActivator} on service inactivation.
   */
  void deactivate() {
    if (previewGeneratorTracker != null) {
      previewGeneratorTracker.close();
    }
    FileUtils.deleteQuietly(phantomTmpDir);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#supports(ch.entwine.weblounge.common.content.Resource)
   */
  public boolean supports(Resource<?> resource) {
    return (resource instanceof Page);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#supports(java.lang.String)
   */
  public boolean supports(String format) {
    for (ImagePreviewGenerator generator : previewGenerators) {
      if (generator.supports(PREVIEW_FORMAT) && generator.supports(format))
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#createPreview(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.site.Environment,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle, String,
   *      java.io.InputStream, java.io.OutputStream)
   */
  public void createPreview(Resource<?> resource, Environment environment,
      Language language, ImageStyle style, String format, InputStream is,
      OutputStream os) throws IOException {

    // We don't need the input stream
    IOUtils.closeQuietly(is);

    // Find a suitable image preview generator for scaling
    ImagePreviewGenerator imagePreviewGenerator = null;
    synchronized (previewGenerators) {
      for (ImagePreviewGenerator generator : previewGenerators) {
        if (generator.supports(format)) {
          imagePreviewGenerator = generator;
          break;
        }
      }
      if (imagePreviewGenerator == null) {
        logger.debug("Unable to generate page previews since no image renderer is available");
        return;
      }
    }

    // Find the relevant metadata to start the request
    ResourceURI uri = resource.getURI();
    long version = resource.getVersion();
    Site site = uri.getSite();

    // Create the url
    URL pageURL = new URL(UrlUtils.concat(site.getHostname(environment).toExternalForm(), PAGE_HANDLER_PREFIX, uri.getIdentifier()));
    if (version == Resource.WORK) {
      pageURL = new URL(UrlUtils.concat(pageURL.toExternalForm(), "work_" + language.getIdentifier() + ".html"));
    } else {
      pageURL = new URL(UrlUtils.concat(pageURL.toExternalForm(), "index_" + language.getIdentifier() + ".html"));
    }

    // Create a temporary file
    File rendererdFile = File.createTempFile("phantomjs-", "." + format, phantomTmpDir);

    // Call PhantomJS to render the page
    try {
      PhantomJsProcessExecutor phantomjs = new PhantomJsProcessExecutor(scriptFile.getAbsolutePath(), pageURL.toExternalForm(), rendererdFile.getAbsolutePath());
      phantomjs.execute();
    } catch (ProcessExcecutorException e) {
      logger.warn("Error creating page preview of {}: {}", pageURL, e.getMessage());
      FileUtils.deleteQuietly(rendererdFile);
      throw new IOException(e);
    }

    FileInputStream imageIs = null;

    // Scale the image to the correct size
    try {
      imageIs = new FileInputStream(rendererdFile);
      imagePreviewGenerator.createPreview(resource, environment, language, style, PREVIEW_FORMAT, imageIs, os);
    } catch (IOException e) {
      logger.error("Error reading original page preview from " + rendererdFile, e);
      throw e;
    } catch (Throwable t) {
      logger.warn("Error scaling page preview at " + uri + ": " + t.getMessage(), t);
      throw new IOException(t);
    } finally {
      IOUtils.closeQuietly(imageIs);
      FileUtils.deleteQuietly(rendererdFile);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getContentType(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public String getContentType(Resource<?> resource, Language language,
      ImageStyle style) {
    return PREVIEW_CONTENT_TYPE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getSuffix(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public String getSuffix(Resource<?> resource, Language language,
      ImageStyle style) {
    return PREVIEW_FORMAT;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getPriority()
   */
  public int getPriority() {
    return 100;
  }

  /**
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(ImagePreviewGenerator generator) {
    synchronized (previewGenerators) {
      previewGenerators.add(generator);
      Collections.sort(previewGenerators, new Comparator<PreviewGenerator>() {
        public int compare(PreviewGenerator a, PreviewGenerator b) {
          return Integer.valueOf(b.getPriority()).compareTo(a.getPriority());
        }
      });
    }
  }

  /**
   * Removes the preview generator from the list of registered preview
   * generators.
   * 
   * @param generator
   *          the generator
   */
  void removePreviewGenerator(ImagePreviewGenerator generator) {
    synchronized (previewGenerators) {
      previewGenerators.remove(generator);
    }
  }

  /**
   * Reads the script from the resource, processes the variables and writes it
   * to the file system so it can be accessed by PhantomJS.
   * 
   * @throws IOException
   *           if reading the template or writing the file failed
   */
  private void prepareScript() throws IOException {
    InputStream is = null;
    InputStream fis = null;
    OutputStream os = null;
    try {
      // Create the temporary directory for everything PhantomJS
      phantomTmpDir = new File(FileUtils.getTempDirectory(), "phantomjs");
      if (!phantomTmpDir.isDirectory() && !phantomTmpDir.mkdirs()) {
        logger.error("Unable to create temp directory for PhantomJS at {}", phantomTmpDir);
        throw new IOException("Unable to create temp directory for PhantomJS at " + phantomTmpDir);
      }

      // Create the script
      is = PhantomJsPagePreviewGenerator.class.getResourceAsStream(SCRIPT_FILE);
      scriptTemplate = IOUtils.toString(is);
      scriptFile = new File(phantomTmpDir, "pagepreview.js");

      // Process templates
      Map<String, String> properties = new HashMap<String, String>();
      properties.put(PARAM_PREPARE_SCRIPT, "return true;");
      String script = ConfigurationUtils.processTemplate(scriptTemplate, properties);

      // Write the processed script to disk
      fis = IOUtils.toInputStream(script);
      os = new FileOutputStream(scriptFile);
      IOUtils.copy(fis, os);

    } catch (IOException e) {
      logger.error("Error reading phantomjs script template from " + SCRIPT_FILE, e);
      FileUtils.deleteQuietly(scriptFile);
      throw e;
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(fis);
      IOUtils.closeQuietly(os);
    }
  }

  /**
   * Implementation of a <code>ServiceTracker</code> that is tracking instances
   * of type {@link ImagePreviewGenerator} with an associated <code>site</code>
   * attribute.
   */
  private class ImagePreviewGeneratorTracker extends ServiceTracker {

    /**
     * Creates a new service tracker that is using the given bundle context to
     * look up service instances.
     * 
     * @param ctx
     *          the bundle context
     */
    ImagePreviewGeneratorTracker(BundleContext ctx) {
      super(ctx, ImagePreviewGenerator.class.getName(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      ImagePreviewGenerator previewGenerator = (ImagePreviewGenerator) super.addingService(reference);
      addPreviewGenerator(previewGenerator);
      return previewGenerator;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      removePreviewGenerator((ImagePreviewGenerator) service);
    }

  }

  /**
   * This process executor is used to run <code>PhantomJS</code> in the system
   * shell.
   */
  private static class PhantomJsProcessExecutor extends ProcessExecutor<IOException> {

    /**
     * Creates a process executor for the phantom JS rendering process
     * 
     * @param script
     *          path to the javascript
     * @param address
     *          the web page to connect to
     * @param file
     *          the file to write to
     */
    protected PhantomJsProcessExecutor(String script, String address,
        String file) {
      super("phantomjs", new String[] { script, address, file });
    }

  }

}
