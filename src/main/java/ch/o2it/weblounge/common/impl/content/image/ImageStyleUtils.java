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

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.OpImage;
import javax.media.jai.RenderedOp;

/**
 * Utility class used for dealing with images and image styles.
 */
public class ImageStyleUtils {

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
  public static float getScale(int imageWidth, int imageHeight, ImageStyle style) {
    boolean taller = imageWidth < imageHeight;
    float scale = 1.0f;

    switch (style.getScalingMode()) {
      case Box:
        if (taller)
          scale = (float)style.getHeight() / imageHeight;
        else
          scale = (float)style.getWidth() / imageWidth;
        break;
      case Cover:
      case Fill:
        if (taller)
          scale = (float)style.getWidth() / imageWidth;
        else
          scale = (float)style.getHeight() / imageHeight;
        break;
      case Height:
        scale = (float)style.getHeight() / imageHeight;
        break;
      case None:
        break;
      case Width:
        scale = (float)style.getWidth() / imageWidth;
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
  public static float getCropX(int imageWidth, int imageHeight, ImageStyle style) {
    int cropX = 0;
    
    switch (style.getScalingMode()) {
      case Fill:
        break;
      case Box:
      case Cover:
      case Height:
      case None:
      case Width:
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
  public static float getCropY(int imageWidth, int imageHeight, ImageStyle style) {
    int cropY = 0;
    
    switch (style.getScalingMode()) {
      case Fill:
        break;
      case Box:
      case Cover:
      case Height:
      case None:
      case Width:
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
   */
  public static void style(InputStream is, OutputStream os, String format,
      ImageStyle style) throws IOException {
    if (style == null)
      throw new IllegalArgumentException("Image style cannot be null");
    try {
      
      // Load the image from the given input stream
      SeekableStream seekableInputStream = new MemoryCacheSeekableStream(is);
      RenderedOp image = JAI.create("stream", seekableInputStream);
      if (image == null)
        throw new IOException("Error reading image from input stream");

      ((OpImage) image.getRendering()).setTileCache(null);

      // Get the original image size
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      
      // Calculate scaling and cropping parameters
      float scale = getScale(imageWidth, imageHeight, style);
      float cropX = getCropX(imageWidth, imageHeight, style);
      float cropY = getCropY(imageWidth, imageHeight, style);
      
      RenderedOp processedImage = image;
      
      // Resizing
      if (scale != 1.0) {
        ParameterBlock scaleParams = new ParameterBlock();
        scaleParams.addSource(image);
        scaleParams.add(scale).add(scale).add(0.0f).add(0.0f);
        scaleParams.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
        
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        qualityHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        qualityHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        qualityHints.put(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));
        
        processedImage = JAI.create("scale", scaleParams, qualityHints);
      }
      
      // Cropping
      if (cropX > 0 || cropY > 0) {
        ParameterBlock cropParams = new ParameterBlock();
        cropParams.addSource(processedImage);
        cropParams.add(cropX > 0 ? cropX/2 : 0); // top left x
        cropParams.add(cropY > 0 ? cropY/2 : 0); // top left y
        cropParams.add(cropX > 0 ? imageWidth - cropX : imageWidth); // width
        cropParams.add(cropY > 0 ? imageHeight - cropY : imageHeight); // height
        processedImage = JAI.create("crop", cropParams);
      }

      // Create the cropped and resized image
      ParameterBlock encodeParams = new ParameterBlock();
      encodeParams.addSource(processedImage).add(os).add(format);
      JAI.create("encode", encodeParams, null);

    } catch (Throwable t) {
      throw new IOException(t);
    }
  }

}
