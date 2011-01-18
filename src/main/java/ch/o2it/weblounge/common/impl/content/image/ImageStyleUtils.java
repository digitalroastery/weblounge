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

package ch.o2it.weblounge.common.impl.content.image;

import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.site.ImageScalingMode;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * Utility class used for dealing with images and image styles.
 */
public final class ImageStyleUtils {

  /** JPEG image quality */
  private static final float JPEG_IMAGE_QUALITY = 0.75f;

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
    return (int)width;
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
    return (int)height;
  }

  /**
   * Returns the scaling factor needed for the image in order to
   * comply with the image style.
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
        width = Math.min(Math.round(scale * imageWidth), style.getWidth());
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
        height = Math.min(Math.round(scale * imageHeight), style.getHeight());
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
   * @throws IOException
   *           if reading from or writing to the stream fails
   * @throws OutOfMemoryError
   *           if the image is too large to be processed in memory
   */
  public static void style(InputStream is, OutputStream os, String format,
      ImageStyle style) throws IOException, OutOfMemoryError {
    if (style == null)
      throw new IllegalArgumentException("Image style cannot be null");

    RenderedOp image = null;
    try {

      // Do we need to do any work at all?
      if (ImageScalingMode.None.equals(style.getScalingMode())) {
        IOUtils.copy(is, os);
        return;
      }

      // Load the image from the given input stream
      SeekableStream seekableInputStream = new MemoryCacheSeekableStream(is);
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

      if (scale > 1.0) {
        ParameterBlock scaleParams = new ParameterBlock();
        scaleParams.addSource(image);
        scaleParams.add(scale).add(scale).add(0.0f).add(0.0f);
        scaleParams.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
        image = JAI.create("scale", scaleParams, scaleHints);
      } else if (scale < 1.0) {
        ParameterBlock scaleParams = new ParameterBlock();
        scaleParams.addSource(image);
        scaleParams.add(Double.valueOf(scale)).add(Double.valueOf(scale)).add(0.0f).add(0.0f);
        scaleParams.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
        image = JAI.create("SubsampleAverage", scaleParams, scaleHints);
      }

      float scaledWidth = image.getWidth();
      float scaledHeight = image.getHeight();

      // Cropping
      float cropX = (float) Math.ceil(getCropX(scaledWidth, scaledHeight, style));
      float cropY = (float) Math.ceil(getCropY(scaledWidth, scaledHeight, style));

      if (cropX > 0 || cropY > 0) {

        ParameterBlock cropTopLeftParams = new ParameterBlock();
        cropTopLeftParams.addSource(image);
        cropTopLeftParams.add(cropX > 0 ? ((float) Math.floor(cropX / 2.0f)) : 0.0f);
        cropTopLeftParams.add(cropY > 0 ? ((float) Math.floor(cropY / 2.0f)) : 0.0f);
        cropTopLeftParams.add(scaledWidth - Math.max(cropX, 0.0f)); // width
        cropTopLeftParams.add(scaledHeight - Math.max(cropY, 0.0f)); // height

        RenderingHints croppingHints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));

        image = JAI.create("crop", cropTopLeftParams, croppingHints);
      }

      JPEGEncodeParam jpegEncodeParam = new JPEGEncodeParam();
      jpegEncodeParam.setQuality(JPEG_IMAGE_QUALITY);
      jpegEncodeParam.setHorizontalSubsampling(0, 1);
      jpegEncodeParam.setHorizontalSubsampling(1, 1);
      jpegEncodeParam.setHorizontalSubsampling(2, 1);
      jpegEncodeParam.setVerticalSubsampling(0, 1);
      jpegEncodeParam.setVerticalSubsampling(1, 1);
      jpegEncodeParam.setVerticalSubsampling(2, 1);

      ImageEncoder encoder = ImageCodec.createImageEncoder("JPEG", os, jpegEncodeParam);
      encoder.encode(image.getAsBufferedImage());

    } catch (OutOfMemoryError t) {
      throw new IOException(t);
    } finally {
      if (image != null)
        image.dispose();
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
   * Creates a file for the scaled image that is identified by
   * <code>image</code>, <code>contents</code>, <code>site</code> and
   * <code>style</code>.
   * 
   * @param resource
   *          the image resource
   * @param image
   *          the image contents
   * @param site
   *          the site
   * @param style
   *          the image style
   * @throws IOException
   *           if creating the file fails
   * @throws IllegalStateException
   *           if a file is found at the parent directory location
   * @return
   */
  public static File getScaledImageFile(ImageResource resource,
      ImageContent image, Site site, ImageStyle style) throws IOException,
      IllegalStateException {

    // If needed, create the scaled file's parent directory
    File dir = new File(PathUtils.concat(
        System.getProperty("java.io.tmpdir"),
        "sites",
        site.getIdentifier(),
        "images",
        style.getIdentifier(),
        resource.getIdentifier(),
        image.getLanguage().getIdentifier()
    ));

    if (dir.exists() && !dir.isDirectory())
      throw new IllegalStateException("Found a file at " + dir + " instead of a directory");
    if (!dir.isDirectory())
      FileUtils.forceMkdir(dir);

    // Get scaled width and height
    float scale = ImageStyleUtils.getScale(image.getWidth(), image.getHeight(), style);
    float styledWidth = image.getWidth() * scale - ImageStyleUtils.getCropX(image.getWidth(), image.getHeight(), style);
    float styledHeight = image.getHeight() * scale - ImageStyleUtils.getCropY(image.getWidth(), image.getHeight(), style);

    // Create the filename
    StringBuffer filename = new StringBuffer(FilenameUtils.getBaseName(image.getFilename()));
    filename.append("_").append((int) styledWidth).append("x").append((int) styledHeight);
    filename.append(".").append(FilenameUtils.getExtension(image.getFilename()));

    return new File(dir, filename.toString());
  }

}
