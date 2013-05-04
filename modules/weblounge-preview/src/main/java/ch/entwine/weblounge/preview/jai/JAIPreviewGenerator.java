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

package ch.entwine.weblounge.preview.jai;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * Utility class used for dealing with images and image styles.
 */
public final class JAIPreviewGenerator implements ImagePreviewGenerator {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(JAIPreviewGenerator.class);

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
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#supports(java.lang.String)
   */
  public boolean supports(String format) {
    if (format == null)
      throw new IllegalArgumentException("Format cannot be null");
    return ImageIO.getImageWritersBySuffix(format.toLowerCase()).hasNext();
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
      if (resource == null)
        throw new IllegalArgumentException("Resource cannot be null");
      if (resource.getContent(language) == null) {
        logger.warn("Skipping creation of preview for {} in language '{}': no content", resource, language.getIdentifier());
        return;
      }
      String mimetype = resource.getContent(language).getMimetype();
      logger.trace("Image preview is generated using the resource's mimetype '{}'", mimetype);
      format = mimetype.substring(mimetype.indexOf("/") + 1);
    }
    style(is, os, format, style);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImagePreviewGenerator#createPreview(java.io.File,
   *      ch.entwine.weblounge.common.site.Environment,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle,
   *      java.lang.String, java.io.InputStream, java.io.OutputStream)
   */
  public void createPreview(File imageFile, Environment environment,
      Language language, ImageStyle style, String format, InputStream is,
      OutputStream os) throws IOException {

    if (format == null) {
      if (imageFile == null)
        throw new IllegalArgumentException("Image file cannot be null");
      format = FilenameUtils.getExtension(imageFile.getName());
      logger.trace("Image preview is generated as '{}'", format);
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

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getPriority()
   */
  public int getPriority() {
    return 0;
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

    SeekableStream seekableInputStream = null;
    RenderedOp image = null;
    try {
      // Load the image from the given input stream
      seekableInputStream = new FileCacheSeekableStream(is);
      image = JAI.create("stream", seekableInputStream);
      if (image == null)
        throw new IOException("Error reading image from input stream");

      // Get the original image size
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();

      // Resizing
      float scale = ImageStyleUtils.getScale(imageWidth, imageHeight, style);

      RenderingHints scaleHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      scaleHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      scaleHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      scaleHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      int scaledWidth = Math.round(scale * image.getWidth());
      int scaledHeight = Math.round(scale * image.getHeight());
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

      if (scale > 1.0) {
        ParameterBlock scaleParams = new ParameterBlock();
        scaleParams.addSource(image);
        scaleParams.add(scale).add(scale).add(0.0f).add(0.0f);
        scaleParams.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
        image = JAI.create("scale", scaleParams, scaleHints);
      } else if (scale < 1.0) {
        ParameterBlock subsampleAverageParams = new ParameterBlock();
        subsampleAverageParams.addSource(image);
        subsampleAverageParams.add(Double.valueOf(scale));
        subsampleAverageParams.add(Double.valueOf(scale));
        image = JAI.create("subsampleaverage", subsampleAverageParams, scaleHints);
      }

      // Cropping
      cropX = (int) Math.max(cropX, (float) Math.ceil(ImageStyleUtils.getCropX(scaledWidth, scaledHeight, style)));
      cropY = (int) Math.max(cropY, (float) Math.ceil(ImageStyleUtils.getCropY(scaledWidth, scaledHeight, style)));

      if ((cropX > 0 && Math.floor(cropX / 2.0f) > 0) || (cropY > 0 && Math.floor(cropY / 2.0f) > 0)) {

        ParameterBlock cropTopLeftParams = new ParameterBlock();
        cropTopLeftParams.addSource(image);
        cropTopLeftParams.add(cropX > 0 ? ((float) Math.floor(cropX / 2.0f)) : 0.0f);
        cropTopLeftParams.add(cropY > 0 ? ((float) Math.floor(cropY / 2.0f)) : 0.0f);
        cropTopLeftParams.add(scaledWidth - Math.max(cropX, 0.0f)); // width
        cropTopLeftParams.add(scaledHeight - Math.max(cropY, 0.0f)); // height

        RenderingHints croppingHints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));

        image = JAI.create("crop", cropTopLeftParams, croppingHints);
      }

      // Write resized/cropped image encoded as JPEG to the output stream
      ParameterBlock encodeParams = new ParameterBlock();
      encodeParams.addSource(image);
      encodeParams.add(os);
      encodeParams.add("jpeg");
      JAI.create("encode", encodeParams);

    } catch (Throwable t) {
      if (t.getClass().getName().contains("ImageFormat")) {
        throw new IllegalArgumentException(t.getMessage());
      }
    } finally {
      IOUtils.closeQuietly(seekableInputStream);
      if (image != null)
        image.dispose();
    }
  }

}
