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

import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.site.ScalingMode;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.IOUtils;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
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

  /**
   * This class is not meant to be instantiated.
   */
  private ImageStyleUtils() {
    // Utility classes should not have a public or default constructor
  }

  /**
   * Returns the scaling factor in horizontal direction needed for the image in
   * order to comply the image style.
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
    boolean taller = imageWidth < imageHeight;
    float scale = 1.0f;

    switch (style.getScalingMode()) {
      case Box:
        if (taller)
          scale = style.getHeight() / imageHeight;
        else
          scale = style.getWidth() / imageWidth;
        break;
      case Cover:
      case Fill:
        if (taller)
          scale = style.getWidth() / imageWidth;
        else
          scale = style.getHeight() / imageHeight;
        break;
      case Height:
        scale = style.getHeight() / imageHeight;
        break;
      case None:
        scale = 1.0f;
        break;
      case Width:
        scale = style.getWidth() / imageWidth;
        break;
      default:
        throw new IllegalStateException("Image style " + style + " contains an unknown scaling mode '" + style.getScalingMode() + "'");
    }

    return scale;
  }

  /**
   * Returns the cropping value in horizontal direction needed for the image in
   * order to comply the image style.
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
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
        cropX = (imageWidth * scale) - style.getWidth();
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
   * order to comply the image style.
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
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
        cropY = (imageHeight * scale) - style.getHeight();
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
   * Resizes the given image to what is defined by the image style.
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
   *           if the image is too large to be processed in memoy
   */
  public static void style(InputStream is, OutputStream os, String format,
      ImageStyle style) throws IOException, OutOfMemoryError {
    if (style == null)
      throw new IllegalArgumentException("Image style cannot be null");

    RenderedOp image = null;
    try {

      // Do we need to do any work at all?
      if (ScalingMode.None.equals(style.getScalingMode())) {
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

      if (scale != 1.0) {
        ParameterBlock scaleParams = new ParameterBlock();
        scaleParams.addSource(image);
        scaleParams.add(scale).add(scale).add(0.0f).add(0.0f);
        scaleParams.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));

        // Quality related hints when scaling the image
        RenderingHints scalingHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        scalingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        scalingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        scalingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        // qualityHints.put(RenderingHints.KEY_INTERPOLATION,
        // RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        scalingHints.put(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));

        image = JAI.create("scale", scaleParams, scalingHints);
      }

      float scaledWidth = image.getWidth();
      float scaledHeight = image.getHeight();

      // Cropping

      float cropX = (float) Math.floor(getCropX(scaledWidth, scaledHeight, style));
      float cropY = (float) Math.floor(getCropY(scaledWidth, scaledHeight, style));

      if (cropX > 0 || cropY > 0) {
        ParameterBlock cropParams = new ParameterBlock();
        cropParams.addSource(image);
        cropParams.add(cropX > 0 ? cropX / 2.0f : 0.0f); // top left x
        cropParams.add(cropY > 0 ? cropY / 2.0f : 0.0f); // top left y
        cropParams.add(scaledWidth - cropX);
        cropParams.add(scaledHeight - cropY);

        RenderingHints croppingHints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));

        image = JAI.create("crop", cropParams, croppingHints);
      }

      // Create the cropped and resized image
      ParameterBlock encodeParams = new ParameterBlock();
      encodeParams.addSource(image).add(os).add(format);
      image = JAI.create("encode", encodeParams, null);

    } catch (OutOfMemoryError t) {
      throw new IOException(t);
    } finally {
      if (image != null)
        image.dispose();
    }
  }

}
