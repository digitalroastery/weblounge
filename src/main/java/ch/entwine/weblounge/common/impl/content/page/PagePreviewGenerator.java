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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.language.Language;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A <code>PreviewGenerator</code> that will generate previews for pages.
 */
public class PagePreviewGenerator implements PreviewGenerator {

  /** Logger factory */
  private final static Logger logger = LoggerFactory.getLogger(PagePreviewGenerator.class);

  /** Format for the preview images */
  private static final String PREVIEW_FORMAT = "png";

  /** Format for the preview images */
  private static final String PREVIEW_CONTENT_TYPE = "image/png";

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#createPreview(ch.entwine.weblounge.common.content.Resource, ch.entwine.weblounge.common.language.Language, ch.entwine.weblounge.common.content.image.ImageStyle, java.io.InputStream, java.io.OutputStream)
   */
  public void createPreview(Resource<?> resource, Language language,
      ImageStyle style, InputStream is, OutputStream os) throws IOException {

    File f = null;
    FileOutputStream fos = null;
    is = new ByteArrayInputStream(((Page) resource).toXml().getBytes("UTF-8"));

    // Write the resource content to disk. This step is needed, as the preview
    // generator can only handle files.
    try {
      f = File.createTempFile("preview", null);
      fos = new FileOutputStream(f);
      IOUtils.copy(is, fos);
    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + f, e);
      IOUtils.closeQuietly(fos);
      FileUtils.deleteQuietly(f);
      throw e;
    } finally {
      IOUtils.closeQuietly(fos);
    }

    // Render the page and write back to client
    try {
      Java2DRenderer renderer = new Java2DRenderer(f, style.getWidth(), style.getHeight());
      renderer.getSharedContext().setInteractive(false);
      BufferedImage img = renderer.getImage();
      FSImageWriter imageWriter = new FSImageWriter(PREVIEW_FORMAT);
      imageWriter.write(img, os);
    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + f, e);
      throw e;
    } finally {
      FileUtils.deleteQuietly(f);
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

}
