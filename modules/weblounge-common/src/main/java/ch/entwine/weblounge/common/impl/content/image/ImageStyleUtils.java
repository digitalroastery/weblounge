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

package ch.entwine.weblounge.common.impl.content.image;

import static java.lang.Boolean.TRUE;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;

import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * Utility class used for dealing with images and image styles.
 */
public final class ImageStyleUtils {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageStyleUtils.class);

  /** True to use ImageMagick for rendering */
  private static boolean isImageMagickInstalled = true;

  /**
   * Check if ImageMagick is available.
   */
  static {
    try {
      if (TRUE.toString().equals(System.getProperty("weblounge.jai"))) {
        logger.info("Using Java Advanced Imaging for image operations");
      } else {
        ConvertCmd cmd = new ConvertCmd();
        IMOperation versionOp = new IMOperation();
        versionOp.version();
        cmd.run(versionOp);
        logger.info("Using ImageMagick for image operations");
      }
    } catch (Throwable e) {
      logger.info("Using Java Advanced Imaging for image operations ({})", e.getMessage());
      isImageMagickInstalled = false;
    }
  }

  /**
   * This class is not meant to be instantiated.
   */
  private ImageStyleUtils() {
    // Utility classes should not have a public or default constructor
  }

  /**
   * Returns the calculated width in pixels for the image with the given image
   * style applied to it.
   * 
   * @param image
   *          the image
   * @param style
   *          the image style
   * @return the width
   */
  public static int getWidth(ImageContent image, ImageStyle style) {
    float width = image.getWidth();
    float height = image.getHeight();
    width *= getScale(width, height, style);
    width -= getCropX(width, height, style);
    return (int) width;
  }

  /**
   * Returns the calculated height in pixels for the image with the given image
   * style applied to it.
   * 
   * @param image
   *          the image
   * @param style
   *          the image style
   * @return the height
   */
  public static int getHeight(ImageContent image, ImageStyle style) {
    float width = image.getWidth();
    float height = image.getHeight();
    height *= getScale(width, height, style);
    height -= getCropY(width, height, style);
    return (int) height;
  }

  /**
   * Returns the scaling factor needed for the image in order to comply with the
   * image style.
   * 
   * @param imageWidth
   *          width of the original image
   * @param imageHeight
   *          height of the original image
   * @param style
   *          the image style
   * @return the scaling factor
   */
  public static float getScale(float imageWidth, float imageHeight,
      ImageStyle style) {
    float scale = 1.0f;
    float scaleX = style.getWidth() / imageWidth;
    float scaleY = style.getHeight() / imageHeight;

    switch (style.getScalingMode()) {
      case Box:
        scale = Math.min(scaleX, scaleY);
        break;
      case Cover:
      case Fill:
        scale = Math.max(scaleX, scaleY);
        break;
      case Height:
        scale = scaleY;
        break;
      case None:
        scale = 1.0f;
        break;
      case Width:
        scale = scaleX;
        break;
      default:
        throw new IllegalStateException("Image style " + style + " contains an unknown scaling mode '" + style.getScalingMode() + "'");
    }

    return scale;
  }

  /**
   * Returns the cropping value in horizontal direction needed for the image in
   * order to comply with the image style.
   * 
   * @param imageWidth
   *          width of the original image
   * @param imageHeight
   *          height of the original image
   * @param style
   *          the image style
   * @return the horizontal cropping amount
   */
  public static float getCropX(float imageWidth, float imageHeight,
      ImageStyle style) {
    float cropX = 0;
    switch (style.getScalingMode()) {
      case Fill:
        cropX = imageWidth - style.getWidth();
        break;
      case Box:
      case Cover:
      case Width:
      case Height:
      case None:
        cropX = 0;
        break;
      default:
        throw new IllegalStateException("Image style " + style + " contains an unknown scaling mode '" + style.getScalingMode() + "'");
    }

    return cropX;
  }

  /**
   * Returns the cropping value in vertical direction needed for the image in
   * order to comply with the image style.
   * 
   * @param imageWidth
   *          width of the original image
   * @param imageHeight
   *          height of the original image
   * @param style
   *          the image style
   * @return the vertical cropping amount
   */
  public static float getCropY(float imageWidth, float imageHeight,
      ImageStyle style) {
    float cropY = 0;
    switch (style.getScalingMode()) {
      case Fill:
        cropY = imageHeight - style.getHeight();
        break;
      case Box:
      case Cover:
      case Height:
      case Width:
      case None:
        break;
      default:
        throw new IllegalStateException("Image style " + style + " contains an unknown scaling mode '" + style.getScalingMode() + "'");
    }

    return cropY;
  }

  /**
   * Returns the width of the image after scaling with the given style.
   * 
   * @param imageWidth
   *          width of the original image
   * @param imageHeight
   *          height of the original image
   * @param style
   *          the image style
   * @return the width of the scaled image
   */
  public static int getStyledWidth(int imageWidth, int imageHeight,
      ImageStyle style) {
    int width = 0;
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
      case Width:
        width = style.getWidth();
        break;
      case Box:
      case Cover:
        width = Math.round(scale * imageWidth);
        break;
      case Height:
        width = Math.round(scale * imageWidth);
        break;
      case None:
        width = imageWidth;
        break;
      default:
        throw new IllegalStateException("Image style " + style + " contains an unknown scaling mode '" + style.getScalingMode() + "'");
    }
    return width;
  }

  /**
   * Returns the height of the image after scaling with the given style.
   * 
   * @param imageWidth
   *          width of the original image
   * @param imageHeight
   *          height of the original image
   * @param style
   *          the image style
   * @return the width of the scaled image
   */
  public static int getStyledHeight(int imageWidth, int imageHeight,
      ImageStyle style) {
    int height = 0;
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
      case Height:
        height = style.getHeight();
        break;
      case Box:
      case Cover:
        height = Math.round(scale * imageHeight);
        break;
      case Width:
        height = Math.round(scale * imageHeight);
        break;
      case None:
        height = imageHeight;
        break;
      default:
        throw new IllegalStateException("Image style " + style + " contains an unknown scaling mode '" + style.getScalingMode() + "'");
    }
    return height;
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
  public static void style(InputStream is, OutputStream os, String format,
      ImageStyle style) throws IllegalArgumentException, IOException,
      OutOfMemoryError {

    // Is an image style permitted?
    if (style == null)
      throw new IllegalArgumentException("Image style cannot be null");

    // Do we need to do any work at all?
    if (ImageScalingMode.None.equals(style.getScalingMode())) {
      IOUtils.copy(is, os);
      return;
    }

    if (isImageMagickInstalled) {
      try {
        styleWithImageMagick(is, os, format, style);
      } catch (Throwable t) {
        logger.warn("Styling image using ImageMagick failed: {}", t.getMessage());
      }
    } else {
      try {
        styleWithJavaAdvancedImaging(is, os, format, style);
      } catch (Throwable t) {
        logger.warn("Styling image using Java Advanced Imaging failed: {}", t.getMessage());
      }
    }

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
  private static void styleWithJavaAdvancedImaging(InputStream is,
      OutputStream os, String format, ImageStyle style)
      throws IllegalArgumentException, IOException, OutOfMemoryError {

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
      float scale = getScale(imageWidth, imageHeight, style);

      RenderingHints scaleHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      scaleHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      scaleHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      scaleHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      float scaledWidth = scale * image.getWidth();
      float scaledHeight = scale * image.getHeight();
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
      cropX = Math.max(cropX, (float) Math.ceil(getCropX(scaledWidth, scaledHeight, style)));
      cropY = Math.max(cropY, (float) Math.ceil(getCropY(scaledWidth, scaledHeight, style)));

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
  private static void styleWithImageMagick(InputStream is, OutputStream os,
      String format, ImageStyle style) throws IllegalArgumentException,
      IOException, OutOfMemoryError {

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
      float scale = getScale(imageWidth, imageHeight, style);

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
      cropX = Math.max(cropX, (float) Math.ceil(getCropX(scaledWidth, scaledHeight, style)));
      cropY = Math.max(cropY, (float) Math.ceil(getCropY(scaledWidth, scaledHeight, style)));

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

  /**
   * Searches all modules of the given site for the image style identified by
   * <code>styleId</code> and returns it. If no such style was found,
   * <code>null</code> is returned.
   * 
   * @param styleId
   *          the image style identifier
   * @param site
   *          the site
   * @return the image style or <code>null</code> if the style was not found
   */
  public static ImageStyle findStyle(String styleId, Site site) {
    if (styleId == null)
      throw new IllegalArgumentException("Style identifier cannot be null");
    if (site == null)
      throw new IllegalArgumentException("Site cannot be null");
    for (Module m : site.getModules()) {
      ImageStyle style = m.getImageStyle(styleId);
      if (style != null)
        return style;
    }
    return null;
  }

  /**
   * Searches all modules of the given site for the image styles whose
   * identifier matches <code>regex</code>. If no such style was found, an empty
   * array is returned.
   * 
   * @param regex
   *          the image style identifier
   * @param composeableOnly
   *          if <code>true</code>, return composeable styles only
   * @param site
   *          the site
   * @return the image styles
   */
  public static ImageStyle[] findStyles(String regex, boolean composeableOnly,
      Site site) {
    if (regex == null)
      throw new IllegalArgumentException("Regular expression cannot be null");
    if (site == null)
      throw new IllegalArgumentException("Site cannot be null");
    List<ImageStyle> styles = new ArrayList<ImageStyle>();
    for (Module m : site.getModules()) {
      for (ImageStyle style : m.getImageStyles()) {
        if (composeableOnly && !style.isComposeable())
          continue;
        if (!style.getIdentifier().matches(regex))
          continue;
        styles.add(style);
      }
    }
    return styles.toArray(new ImageStyle[styles.size()]);
  }

  /**
   * Creates a file for the scaled image that is identified by
   * <code>filename</code>, <code>language</code> and <code>style</code>.
   * <p>
   * If no filename is specified, the resource's identifier is used.
   * 
   * @param uri
   *          the resource uri
   * @param filename
   *          the file name
   * @param language
   *          the language
   * @param style
   *          the image style
   * @throws IOException
   *           if creating the file fails
   * @throws IllegalStateException
   *           if a file is found at the parent directory location
   * @return
   */
  public static File createScaledFile(ResourceURI uri, String filename,
      Language language, ImageStyle style) throws IOException,
      IllegalStateException {

    if (filename == null)
      filename = uri.getIdentifier();
    String suffix = FilenameUtils.getExtension(filename);

    // If needed, create the scaled file's parent directory
    Site site = uri.getSite();
    File dir = new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images", style.getIdentifier(), uri.getIdentifier(), language.getIdentifier()));

    if (dir.exists() && !dir.isDirectory())
      throw new IllegalStateException("Found a file at " + dir + " instead of a directory");
    if (!dir.isDirectory())
      FileUtils.forceMkdir(dir);

    // Create the filename
    StringBuffer scaledFilename = new StringBuffer(FilenameUtils.getBaseName(filename));
    scaledFilename.append("-").append(style.getIdentifier());
    if (StringUtils.isNotBlank(suffix))
      scaledFilename.append(".").append(suffix);

    return new File(dir, scaledFilename.toString());
  }

}
