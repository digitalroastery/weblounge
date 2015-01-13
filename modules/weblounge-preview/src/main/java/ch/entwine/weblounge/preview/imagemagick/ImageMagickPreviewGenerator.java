/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
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

package ch.entwine.weblounge.preview.imagemagick;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.process.OutputConsumer;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used for dealing with images and image styles.
 */
public final class ImageMagickPreviewGenerator implements ImagePreviewGenerator {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageMagickPreviewGenerator.class);

  /** List of supported formats (cached) */
  private final Map<String, Boolean> supportedFormats = new HashMap<String, Boolean>();

  /** Flag to indicate whether format detection is supported */
  private boolean formatDecetionSupported = true;

  /** The image magic temp directory */
  private File imageMagickDir = null;

  /**
   * Called by the {@link ImageMagickActivator} on service activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    try {
      prepareDirectory();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Called by the {@link ImageMagickActivator} on service inactivation.
   */
  void deactivate() {
    FileUtils.deleteQuietly(imageMagickDir);
  }

  @Override
  public boolean supports(Resource<?> resource, Language language) {
    return (resource instanceof ImageResource);
  }

  @Override
  public boolean supports(String format) {
    if (format == null)
      throw new IllegalArgumentException("Format cannot be null");

    if (!formatDecetionSupported)
      return true;

    // Check for verified support
    if (supportedFormats.containsKey(format))
      return supportedFormats.get(format);

    // Reach out to ImageMagick
    ConvertCmd imageMagick = new ConvertCmd();
    IMOperation op = new IMOperation();
    op.identify().list("format");
    try {
      final Pattern p = Pattern.compile("[\\s]+" + format.toUpperCase() + "[\\s]+rw");
      final Boolean[] supported = new Boolean[1];
      imageMagick.setOutputConsumer(new OutputConsumer() {
        public void consumeOutput(InputStream is) throws IOException {
          String output = IOUtils.toString(is);
          Matcher m = p.matcher(output);
          supported[0] = new Boolean(m.find());
        }
      });
      imageMagick.run(op);

      // Cache the result
      supportedFormats.put(format, supported[0]);

      return supported[0];
    } catch (Throwable t) {
      logger.warn("Error looking up formats supported by ImageMagick: {}", t.getMessage());
      formatDecetionSupported = false;
      logger.info("ImageMagick format lookup failed, assuming support for all formats");
      return true;
    }
  }

  @Override
  public void createPreview(Resource<?> resource, Environment environment,
      Language language, ImageStyle style, String format, InputStream is,
      OutputStream os) throws IOException {

    if (format == null) {
      if (resource == null)
        throw new IllegalArgumentException("Resource cannot be null");
      if (resource.getContent(language) == null) {
        logger.warn("Skipping creation of preview for {} in language '{}': no content", resource, language.getIdentifier());
        return;
      }
      String mimetype = resource.getContent(language).getMimetype();
      logger.debug("Image preview of {} is generated using the resource's mimetype '{}'", resource.getIdentifier(), mimetype);
      format = mimetype.substring(mimetype.indexOf("/") + 1);
    }
    style(is, os, format, style);
  }

  @Override
  public void createPreview(File imageFile, Environment environment,
      Language language, ImageStyle style, String format, InputStream is,
      OutputStream os) throws IOException {

    if (format == null) {
      if (imageFile == null)
        throw new IllegalArgumentException("Image file cannot be null");
      format = FilenameUtils.getExtension(imageFile.getName());
      logger.debug("Image preview of {} is generated as '{}'", imageFile.getAbsolutePath(), format);
    }

    style(is, os, format, style);
  }

  @Override
  public String getContentType(Resource<?> resource, Language language,
      ImageStyle style) {
    String mimetype = resource.getContent(language).getMimetype();
    return mimetype;
  }

  @Override
  public String getSuffix(Resource<?> resource, Language language,
      ImageStyle style) {
    // Load the resource
    ResourceContent content = resource.getContent(language);
    if (content == null) {
      content = resource.getOriginalContent();
      if (content == null) {
        logger.warn("Trying to get filename suffix for {}, which has no content", resource);
        return null;
      }
    }

    // Get the file name
    String filename = content.getFilename();
    if (StringUtils.isBlank(filename)) {
      logger.warn("Trying to get filename suffix for {}, which has no filename", resource);
      return null;
    }

    // Add the file identifier name
    if (StringUtils.isNotBlank(style.getIdentifier())) {
      filename += "-" + style.getIdentifier();
    }

    return FilenameUtils.getExtension(filename);
  }

  @Override
  public int getPriority() {
    return 100;
  }

  /**
   * Resizes the given image to what is defined by the image style and writes
   * the result to the output stream.
   * 
   * @param is
   *          the input stream
   * @param os
   *          the output stream
   * @param format
   *          the image format
   * @param style
   *          the style
   * @throws IllegalArgumentException
   *           if the image is in an unsupported format
   * @throws IllegalArgumentException
   *           if the input stream is empty
   * @throws IOException
   *           if reading from or writing to the stream fails
   * @throws OutOfMemoryError
   *           if the image is too large to be processed in memory
   */
  @SuppressWarnings("cast")
  private void style(InputStream is, OutputStream os, String format,
      ImageStyle style) throws IllegalArgumentException, IOException,
      OutOfMemoryError {

    // Does the input stream contain any data?
    if (is.available() == 0)
      throw new IllegalArgumentException("Empty input stream was passed to image styling");

    // Do we need to do any work at all?
    if (style == null || ImageScalingMode.None.equals(style.getScalingMode())) {
      logger.trace("No scaling needed, performing a noop stream copy");
      IOUtils.copy(is, os);
      return;
    }

    String uuid = UUID.randomUUID().toString();
    File originalFile = new File(imageMagickDir, "image-" + uuid + "." + format);
    File scaledFile = new File(imageMagickDir, "image-scaled-" + uuid + "." + format);
    File croppedFile = new File(imageMagickDir, "image-cropped-" + uuid + "." + format);

    try {

      File finalFile = null;
      FileOutputStream fos = new FileOutputStream(originalFile);
      IOUtils.copy(is, fos);
      IOUtils.closeQuietly(fos);
      IOUtils.closeQuietly(is);

      // Load the image from the temporary file
      Info imageInfo = new Info(originalFile.getAbsolutePath(), true);

      // Get the original image size
      int imageWidth = imageInfo.getImageWidth();
      int imageHeight = imageInfo.getImageHeight();

      // Prepare for processing
      ConvertCmd imageMagick = new ConvertCmd();

      // Resizing
      float scale = ImageStyleUtils.getScale(imageWidth, imageHeight, style);

      int scaledWidth = Math.round(scale * imageWidth);
      int scaledHeight = Math.round(scale * imageHeight);
      int cropX = 0;
      int cropY = 0;

      // If either one of scaledWidth or scaledHeight is < 1.0, then
      // the scale needs to be adapted to scale to 1.0 exactly and accomplish
      // the rest by cropping.

      if (scaledWidth < 1.0f) {
        scale = 1.0f / imageWidth;
        scaledWidth = 1;
        cropY = imageHeight - scaledHeight;
        scaledHeight = Math.round(imageHeight * scale);
      } else if (scaledHeight < 1.0f) {
        scale = 1.0f / imageHeight;
        scaledHeight = 1;
        cropX = imageWidth - scaledWidth;
        scaledWidth = Math.round(imageWidth * scale);
      }

      // Do the scaling
      IMOperation scaleOp = new IMOperation();
      scaleOp.addImage(originalFile.getAbsolutePath());
      scaleOp.resize((int) scaledWidth, (int) scaledHeight);
      scaleOp.addImage(scaledFile.getAbsolutePath());
      imageMagick.run(scaleOp);
      finalFile = scaledFile;

      // Cropping
      cropX = (int) Math.max(cropX, Math.ceil(ImageStyleUtils.getCropX(scaledWidth, scaledHeight, style)));
      cropY = (int) Math.max(cropY, Math.ceil(ImageStyleUtils.getCropY(scaledWidth, scaledHeight, style)));

      if ((cropX > 0 && Math.floor(cropX / 2.0f) > 0) || (cropY > 0 && Math.floor(cropY / 2.0f) > 0)) {

        int croppedLeft = (int) (cropX > 0 ? ((float) Math.floor(cropX / 2.0f)) : 0.0f);
        int croppedTop = (int) (cropY > 0 ? ((float) Math.floor(cropY / 2.0f)) : 0.0f);
        int croppedWidth = (int) (scaledWidth - Math.max(cropX, 0.0f));
        int croppedHeight = (int) (scaledHeight - Math.max(cropY, 0.0f));

        // Do the cropping
        IMOperation cropOperation = new IMOperation();
        cropOperation.addImage(scaledFile.getAbsolutePath());
        cropOperation.crop(croppedWidth, croppedHeight, croppedLeft, croppedTop);
        cropOperation.p_repage(); // Reset the page canvas and position to match
        // the actual cropped image
        cropOperation.addImage(croppedFile.getAbsolutePath());
        imageMagick.run(cropOperation);
        finalFile = croppedFile;
      }

      // Write resized/cropped image encoded as JPEG to the output stream
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(finalFile);
        IOUtils.copy(fis, os);
      } finally {
        IOUtils.closeQuietly(fis);
      }

    } catch (Throwable t) {
      throw new IllegalArgumentException(t.getMessage());
    } finally {
      FileUtils.deleteQuietly(originalFile);
      FileUtils.deleteQuietly(scaledFile);
      FileUtils.deleteQuietly(croppedFile);
    }
  }

  /**
   * Makes sure that a temp directory exists.
   * 
   * @throws IOException
   *           if the directory cannot be created
   */
  private void prepareDirectory() throws IOException {
    imageMagickDir = new File(FileUtils.getTempDirectory(), "imagemagick");
    if (!imageMagickDir.isDirectory() && !imageMagickDir.mkdirs()) {
      logger.error("Unable to create temp directory for ImageMagick at {}", imageMagickDir);
      throw new IOException("Unable to create temp directory for ImageMagick at " + imageMagickDir);
    }

  }

}
