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

package ch.entwine.weblounge.aws;

import ch.entwine.weblounge.common.content.ResourceLocator;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.HTMLInclude;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.contentrepository.ResourceListener;

import java.net.URL;

/**
 * Implementation of a resource listener that will make sure each resource's
 * life cycle is reflected on the amazon platform, especially with respect to
 * content serialization and delivery.
 */
public class AWSImageResourceSerializer implements ResourceListener<ImageResource>, ResourceLocator<ImageResource> {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#created(ch.entwine.weblounge.common.content.Resource)
   */
  public void created(ImageResource resource) {
    // TODO: Push image
    // TODO: Push all preview styles of image
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#updated(ch.entwine.weblounge.common.content.Resource)
   */
  public void updated(ImageResource resource) {
    // TODO: Push image
    // TODO: Push all preview styles of image
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#published(ch.entwine.weblounge.common.content.Resource)
   */
  public void published(ImageResource resource) {
    // Either push image and image styles or tweak access rights
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#unpublished(ch.entwine.weblounge.common.content.Resource)
   */
  public void unpublished(ImageResource resource) {
    // Either pull image and image styles or tweak access rights
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceListener#deleted(ch.entwine.weblounge.common.content.Resource)
   */
  public void deleted(ImageResource resource) {
    // TODO: Remove image
    // TODO: Remove all preview styles of image
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceLocator#getLocation(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public URL getLocation(ImageResource resource, Language language) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceLocator#getLocation(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public URL getLocation(ImageResource resource, Language language,
      ImageStyle imageStyle) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceLocator#getLocation(ch.entwine.weblounge.common.content.page.HTMLInclude)
   */
  public URL getLocation(HTMLInclude include) {
    // TODO Auto-generated method stub
    return null;
  }

}
