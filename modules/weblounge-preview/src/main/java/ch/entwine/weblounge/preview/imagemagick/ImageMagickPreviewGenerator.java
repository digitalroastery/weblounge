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

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class used for dealing with images and image styles.
 */
public final class ImageMagickPreviewGenerator implements PreviewGenerator {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageMagickPreviewGenerator.class);

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#supports(ch.entwine.weblounge.common.content.Resource)
   */
  public boolean supports(Resource<?> resource) {
    return (resource instanceof ImageResource);
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
    if (format == null) {
      String mimetype = resource.getContent(language).getMimetype();
      logger.trace("Image preview is generated using the resource's mimetype '{}'", mimetype);
      format = mimetype.substring(mimetype.indexOf("/") + 1);
    }
    style(is, os, format, style);
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
    String mimetype = resource.getContent(language).getMimetype();
    return mimetype;
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
    String filename = resource.getContent(language).getFilename();
    return FilenameUtils.getExtension(filename);
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
   * @throws IOException
   *           if reading from or writing to the stream fails
   * @throws OutOfMemoryError
   *           if the image is too large to be processed in memory
   */
  private void style(InputStream is, OutputStream os, String format,
      ImageStyle style) throws IllegalArgumentException, IOException,
      OutOfMemoryError {

    // Is an image style permitted?
    if (style == null)
      throw new IllegalArgumentException("Image style cannot be null");

    // Do we need to do any work at all?
    if (ImageScalingMode.None.equals(style.getScalingMode())) {
      logger.trace("No scaling needed, performing a noop stream copy");
      IOUtils.copy(is, os);
      return;
    }

    File originalFile = File.createTempFile("image-", "." + format);
    File scaledFile = File.createTempFile("image-scaled", "." + format);
    File croppedFile = File.createTempFile("image-cropped", "." + format);

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

      float scaledWidth = scale * imageWidth;
      float scaledHeight = scale * imageHeight;
      float cropX = 0.0f;
      float cropY = 0.0f;

      // If either one of scaledWidth or scaledHeight is < 1.0, then
      // the scale needs to be adapted to scale to 1.0 exactly and accomplish
      // the rest by cropping.

      if (scaledWidth < 1.0f) {
        scale = 1.0f / imageWidth;
        scaledWidth = 1.0f;
        cropY = imageHeight - scaledHeight;
        scaledHeight = imageHeight * scale;
      } else if (scaledHeight < 1.0f) {
        scale = 1.0f / imageHeight;
        scaledHeight = 1.0f;
        cropX = imageWidth - scaledWidth;
        scaledWidth = imageWidth * scale;
      }

      // Do the scaling
      IMOperation scaleOp = new IMOperation();
      scaleOp.addImage(originalFile.getAbsolutePath());
      scaleOp.resize((int) scaledWidth, (int) scaledHeight);
      scaleOp.addImage(scaledFile.getAbsolutePath());
      imageMagick.run(scaleOp);
      finalFile = scaledFile;

      // Cropping
      cropX = Math.max(cropX, (float) Math.ceil(ImageStyleUtils.getCropX(scaledWidth, scaledHeight, style)));
      cropY = Math.max(cropY, (float) Math.ceil(ImageStyleUtils.getCropY(scaledWidth, scaledHeight, style)));

      if ((cropX > 0 && Math.floor(cropX / 2.0f) > 0) || (cropY > 0 && Math.floor(cropY / 2.0f) > 0)) {

        int croppedLeft = (int) (cropX > 0 ? ((float) Math.floor(cropX / 2.0f)) : 0.0f);
        int croppedTop = (int) (cropY > 0 ? ((float) Math.floor(cropY / 2.0f)) : 0.0f);
        int croppedWidth = (int) (scaledWidth - Math.max(cropX, 0.0f));
        int croppedHeight = (int) (scaledHeight - Math.max(cropY, 0.0f));

        // Do the cropping
        IMOperation cropOperation = new IMOperation();
        cropOperation.addImage(scaledFile.getAbsolutePath());
        cropOperation.crop(croppedWidth, croppedHeight, croppedLeft, croppedTop);
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

}
