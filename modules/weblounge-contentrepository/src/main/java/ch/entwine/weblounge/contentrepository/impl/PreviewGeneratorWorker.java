/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Worker implementation that creates a preview in a separate thread.
 */
class PreviewGeneratorWorker implements Runnable {

  /** The content repository */
  private AbstractWritableContentRepository contentRepository = null;

  /** The resource to render */
  private Resource<?> resource = null;

  /** The list of image styles to produce */
  private List<ImageStyle> styles = null;

  /** The environment */
  private Environment environment = null;

  /** The languages to render */
  private List<Language> languages = null;

  /** The format */
  private String format = null;

  /**
   * Creates a new preview worker who will create the corresponding previews for
   * the given resource and style.
   * 
   * @param resource
   *          the resource
   * @param environment
   *          the current environment
   * @param languages
   *          the languages
   * @param styles
   *          the image styles
   */
  public PreviewGeneratorWorker(AbstractWritableContentRepository repository,
      Resource<?> resource, Environment environment, List<Language> languages,
      List<ImageStyle> styles, String format) {
    if (languages == null || languages.size() == 0)
      throw new IllegalArgumentException("At least one language must be provided");
    if (styles == null || styles.size() == 0)
      throw new IllegalArgumentException("At least one preview style must be provided");
    this.contentRepository = repository;
    this.resource = resource;
    this.environment = environment;
    this.languages = languages;
    this.styles = styles;
    this.format = format;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
    ResourceURI resourceURI = resource.getURI();
    String resourceType = resourceURI.getType();

    try {

      // Find the resource serializer
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
      if (serializer == null) {
        AbstractWritableContentRepository.logger.warn("Unable to index resources of type '{}': no resource serializer found", resourceType);
        return;
      }

      // Does the serializer come with a preview generator?
      PreviewGenerator previewGenerator = serializer.getPreviewGenerator(resource);
      if (previewGenerator == null) {
        AbstractWritableContentRepository.logger.debug("Resource type '{}' does not support previews", resourceType);
        return;
      }

      // Create the scaled images
      String mimeType = "image/" + format;
      ResourceSerializer<?, ?> s = ResourceSerializerFactory.getSerializerByMimeType(mimeType);
      if (s == null) {
        AbstractWritableContentRepository.logger.warn("No resource serializer is capable of dealing with resources of format '{}'", mimeType);
        return;
      } else if (!(s instanceof ImageResourceSerializer)) {
        AbstractWritableContentRepository.logger.warn("Resource serializer lookup for format '{}' returned {}", format, s.getClass());
        return;
      }

      // Find us an image serializer
      ImageResourceSerializer irs = (ImageResourceSerializer) s;
      ImagePreviewGenerator imagePreviewGenerator = (ImagePreviewGenerator) irs.getPreviewGenerator(format);
      if (imagePreviewGenerator == null) {
        AbstractWritableContentRepository.logger.warn("Image resource serializer {} does not provide support for '{}'", irs, format);
        return;
      }

      // Now scale the original preview according to the existing styles
      for (Language l : languages) {
        if (!resource.supportsContentLanguage(l))
          continue;

        // Create the original preview image for every language
        ImageStyle originalStyle = new ImageStyleImpl("original", ImageScalingMode.None);
        File originalPreview = null;
        if (!resource.supportsContentLanguage(l))
          continue;
        originalPreview = createPreview(resource, originalStyle, l, previewGenerator, format);
        if (originalPreview == null || !originalPreview.exists() || originalPreview.length() == 0) {
          AbstractWritableContentRepository.logger.debug("Preview generation for {} failed", resource);
          return;
        }

        // Create the remaining styles
        for (ImageStyle style : styles) {

          // The original has been produced already
          if (ImageScalingMode.None.equals(style.getScalingMode()))
            continue;

          FileInputStream fis = null;
          FileOutputStream fos = null;
          try {
            fis = new FileInputStream(originalPreview);
            File scaledFile = ImageStyleUtils.createScaledFile(resourceURI, originalPreview.getName(), l, style);
            fos = new FileOutputStream(scaledFile);
            imagePreviewGenerator.createPreview(originalPreview, environment, l, style, resourceType, fis, fos);
          } catch (Throwable t) {
            AbstractWritableContentRepository.logger.error("Error scaling {}: {}", originalPreview, t.getMessage());
            continue;
          } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
          }
        }
      }

    } finally {
      contentRepository.previewCreated(resource);
    }
  }

  /**
   * Creates the actual preview.
   * 
   * @param resource
   *          the resource
   * @param style
   *          the image style
   * @param language
   *          the language
   * @param previewGenerator
   *          the preview generator
   * @param the
   *          preview format
   * @return returns the preview file
   */
  private File createPreview(Resource<?> resource, ImageStyle style,
      Language language, PreviewGenerator previewGenerator, String format) {

    ResourceURI resourceURI = resource.getURI();
    String resourceType = resourceURI.getType();

    // Create the filename
    ResourceContent content = resource.getContent(language);
    String filename = content != null ? content.getFilename() : resource.getIdentifier();
    String suffix = previewGenerator.getSuffix(resource, language, style);
    filename = FilenameUtils.getBaseName(filename) + "." + suffix;

    // Initiate creation of previews
    InputStream resourceInputStream = null;
    InputStream contentRepositoryIs = null;
    FileOutputStream fos = null;
    File scaledResourceFile = null;

    try {
      scaledResourceFile = ImageStyleUtils.createScaledFile(resourceURI, filename.toString(), language, style);

      // Find the modification date
      long lastModified = ResourceUtils.getModificationDate(resource, language).getTime();

      // Create the file if it doesn't exist or if it is out dated. Note that
      // the last modified date of a file has a precision of seconds
      if (!scaledResourceFile.isFile() || FileUtils.isFileOlder(scaledResourceFile, new Date(lastModified))) {
        contentRepositoryIs = contentRepository.getContent(resourceURI, language);

        // Is this local content?
        if (contentRepositoryIs == null && content != null && content.getExternalLocation() != null) {
          contentRepositoryIs = content.getExternalLocation().openStream();
        }

        fos = new FileOutputStream(scaledResourceFile);
        AbstractWritableContentRepository.logger.debug("Creating preview of '{}' at {}", resource, scaledResourceFile);

        previewGenerator.createPreview(resource, environment, language, style, format, contentRepositoryIs, fos);
        if (scaledResourceFile.length() > 0) {
          scaledResourceFile.setLastModified(lastModified);
        } else {
          File f = scaledResourceFile;
          while (f != null && f.isDirectory() && f.listFiles().length == 0) {
            FileUtils.deleteQuietly(f);
            f = f.getParentFile();
          }
        }
      }

    } catch (ContentRepositoryException e) {
      AbstractWritableContentRepository.logger.error("Error loading {} {} '{}' from {}: {}", new Object[] {
          language,
          resourceType,
          resource,
          this,
          e.getMessage() });
      AbstractWritableContentRepository.logger.error(e.getMessage(), e);
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

    } catch (IOException e) {
      AbstractWritableContentRepository.logger.warn("Error creating preview for {} '{}': {}", new Object[] {
          resourceType,
          resourceURI,
          e.getMessage() });
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

    } catch (Throwable t) {
      AbstractWritableContentRepository.logger.warn("Error creating preview for {} '{}': {}", new Object[] {
          resourceType,
          resourceURI,
          t.getMessage() });
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

    } finally {
      IOUtils.closeQuietly(contentRepositoryIs);
      IOUtils.closeQuietly(fos);
    }

    return scaledResourceFile;
  }

}