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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used for dealing with images and image styles.
 */
public final class ImageStyleUtils {

  public static final String DEFAULT_PREVIEW_FORMAT = "png";

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
  public static int getStyledWidth(ImageContent image, ImageStyle style) {
    return getStyledWidth(image.getWidth(), image.getHeight(), style);
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
  public static int getStyledHeight(ImageContent image, ImageStyle style) {
    return getStyledHeight(image.getWidth(), image.getHeight(), style);
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
        if (imageWidth <= style.getWidth() && imageHeight <= style.getHeight())
          scale = 1.0f;
        break;
      case Cover:
        if (scaleX >= 1f || scaleY >= 1f)
          scale = Math.max(scaleX, scaleY);
        else if (scaleX < 0f || scaleY < 1f)
          scale = Math.max(scaleX, scaleY);
        else
          scale = Math.min(scaleX, scaleY);
        break;
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
  public static int getCropX(int imageWidth, int imageHeight, ImageStyle style) {
    int cropX = 0;
    int styledWidth = getStyledWidth(imageWidth, imageHeight, style);
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
      case Box:
      case Cover:
      case Width:
      case Height:
        if (styledWidth > -1 && styledWidth < scale * imageWidth)
          cropX = Math.round(scale * imageWidth) - styledWidth;
        break;
      case None:
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
  public static int getCropY(int imageWidth, int imageHeight, ImageStyle style) {
    int cropY = 0;
    int styledHeight = getStyledHeight(imageWidth, imageHeight, style);
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
      case Box:
      case Cover:
      case Height:
      case Width:
        if (styledHeight > -1 && styledHeight < scale * imageHeight)
          cropY = Math.round(scale * imageHeight) - styledHeight;
        break;
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
    int width = imageWidth;
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
      case Width:
        width = style.getWidth();
        break;
      case Box:
        if (scale < 1)
          width = Math.round(scale * imageWidth);
        break;
      case Cover:
        width = Math.round(scale * imageWidth);
        break;
      case Height:
        width = Math.round(scale * imageWidth);
        if (width > style.getWidth())
          width = style.getWidth();
        break;
      case None:
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
    int height = imageHeight;
    float scale = getScale(imageWidth, imageHeight, style);
    switch (style.getScalingMode()) {
      case Fill:
      case Height:
        height = style.getHeight();
        break;
      case Box:
        if (scale < 1)
          height = Math.round(scale * imageHeight);
        break;
      case Cover:
        height = Math.round(scale * imageHeight);
        break;
      case Width:
        height = Math.round(scale * imageHeight);
        if (height > style.getHeight())
          height = style.getHeight();
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
   * Creates a file and its parent directories for the scaled image that is
   * identified by <code>filename</code>, <code>language</code> and
   * <code>style</code>.
   * <p>
   * If no filename is specified, the resource's identifier is used.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @param style
   *          the image style
   * @throws IOException
   *           if creating the file fails
   * @throws IllegalStateException
   *           if a file is found at the parent directory location
   * @return the file
   */
  public static File createScaledFile(Resource<?> resource, Language language,
      ImageStyle style) throws IOException, IllegalStateException {
    return createScaledFile(resource, null, language, style);
  }

  /**
   * Creates a file and its parent directories for the scaled image that is
   * identified by <code>filename</code>, <code>language</code> and
   * <code>style</code>.
   * <p>
   * If no filename is specified, the resource's identifier is used.
   * 
   * @param resource
   *          the resource
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
   * @return the file
   */
  public static File createScaledFile(Resource<?> resource, String filename,
      Language language, ImageStyle style) throws IOException,
      IllegalStateException {

    File scaledFile = getScaledFile(resource, language, style);
    File dir = scaledFile.getParentFile();

    if (dir.exists() && !dir.isDirectory())
      throw new IllegalStateException("Found a file at " + dir + " instead of a directory");
    if (!dir.isDirectory())
      FileUtils.forceMkdir(dir);

    return scaledFile;
  }

  /**
   * Returns the file for the scaled image that is identified by
   * <code>filename</code>, <code>language</code> and <code>style</code>.
   * <p>
   * If no filename is specified, the resource's identifier is used.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @param style
   *          the image style
   * @return the file on the filesystem
   */
  public static File getScaledFile(Resource<?> resource, Language language,
      ImageStyle style) {
    return getScaledFile(resource, null, language, style);
  }

  /**
   * Returns the original file for the scaled image that is identified by
   * <code>filename</code> and <code>language</code>.
   * <p>
   * If no filename is specified, the resource's identifier is used.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @return the file on the filesystem
   */
  public static File getUnscaledFile(Resource<?> resource, Language language) {
    return getScaledFile(resource, null, language, null);
  }

  /**
   * Returns the file for the scaled image that is identified by
   * <code>filename</code>, <code>language</code> and <code>style</code>.
   * <p>
   * If no filename is specified, the resource's identifier is used.
   * <p>
   * If <code>null</code> is provided as the image style, the
   * <code>original</code> style will be used.
   * 
   * @param resource
   *          the resource
   * @param filename
   *          the file name
   * @param language
   *          the language
   * @param style
   *          the image style
   * @return the path to the scaled image
   */
  public static File getScaledFile(Resource<?> resource, String filename,
      Language language, ImageStyle style) {

    if (StringUtils.isBlank(filename) && resource.getContent(language) != null)
      filename = resource.getContent(language).getFilename();
    if (StringUtils.isBlank(filename))
      filename = resource.getURI().getIdentifier();

    if (style == null) {
      style = new ImageStyleImpl("original", ImageScalingMode.None);
    }

    String suffix = FilenameUtils.getExtension(filename);
    if (StringUtils.isBlank(suffix))
      suffix = DEFAULT_PREVIEW_FORMAT;

    // If needed, create the scaled file's parent directory
    Site site = resource.getURI().getSite();
    File dir = new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images", style.getIdentifier(), resource.getIdentifier(), Long.toString(resource.getVersion()), language.getIdentifier()));

    // Create the filename
    StringBuffer scaledFilename = new StringBuffer(FilenameUtils.getBaseName(filename));
    scaledFilename.append("-").append(style.getIdentifier());

    if (StringUtils.isNotBlank(suffix))
      scaledFilename.append(".").append(suffix);

    return new File(dir, scaledFilename.toString());
  }

  /**
   * Returns the file for the original image that is identified by
   * <code>filename</code> and <code>language</code>.
   * 
   * @param resource
   *          the resource
   * @param filename
   *          the file name
   * @param language
   *          the language
   * @return the path to the preview image
   */
  public static File getUnscaledFile(Resource<?> resource, String filename,
      Language language) {
    return getScaledFile(resource, filename, language, null);
  }

  /**
   * Returns the base directory for the site's preview images.
   * 
   * @param site
   *          the site
   * @return the directory holding the site's preview images
   * @throws IllegalArgumentException
   *           if <code>site</code> is null
   */
  public static File getDirectory(Site site) {
    if (site == null)
      throw new IllegalArgumentException("site must not be null");
    return new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images"));
  }

  /**
   * Returns the base directory for the site's preview images.
   * 
   * @param site
   *          the site
   * @param style
   *          the image style
   * @return the directory holding the site's preview images
   * @throws IllegalArgumentException
   *           if either one of <code>site</code>, <code>style</code> is null
   */
  public static File getDirectory(Site site, ImageStyle style) {
    if (site == null)
      throw new IllegalArgumentException("site must not be null");
    if (style == null)
      throw new IllegalArgumentException("style must not be null");
    return new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images", style.getIdentifier()));
  }

}
