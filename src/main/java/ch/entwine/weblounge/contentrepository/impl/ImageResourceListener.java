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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.impl.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.contentrepository.ResourceListener;

/**
 * A listener for the life cycle of image resources that will perform certain
 * actions such as preview image generation or <code>EXIF</code> metadata
 * extraction.
 */
public class ImageResourceListener implements ResourceListener<ImageResource> {
  
  /** The preview generator */
  protected PreviewGenerator previewGenerator = new ImagePreviewGenerator();

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#created(ch.entwine.weblounge.common.content.Resource)
   */
  public void created(ImageResource resource) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#updated(ch.entwine.weblounge.common.content.Resource)
   */
  public void updated(ImageResource resource) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#published(ch.entwine.weblounge.common.content.Resource)
   */
  public void published(ImageResource resource) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#unpublished(ch.entwine.weblounge.common.content.Resource)
   */
  public void unpublished(ImageResource resource) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#deleted(ch.entwine.weblounge.common.content.Resource)
   */
  public void deleted(ImageResource resource) {
    // TODO Auto-generated method stub

  }

  /**
   * Creates all previews of the given image resource.
   * 
   * @param resource
   *          the image resource
   */
  // private void createPreviews(ImageResource resource) {
  // Site site = resource.getURI().getSite();
  // List<ImageStyle> styles = new ArrayList<ImageStyle>();
  //
  // // Find all styles
  // for (Module m : site.getModules()) {
  // styles.addAll(Arrays.asList(m.getImageStyles()));
  // }
  //
  // // Create the preview images
  // for (Language language : resource.languages()) {
  // for (ImageStyle style : styles) {
  // InputStream is = null;
  // try {
  // ImageContent content = resource.getContent(language);
  // //previewGenerator.createPreview(null, style, null, null);
  // } finally {
  //
  // }
  // }
  // }
  // }

}
