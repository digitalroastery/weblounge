/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://entwinemedia.com/weblounge
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.content.image.ImageStyle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The <code>ImageStyleTracker</code> watches instances of {@link ImageStyle} in
 * the OSGi registry and registers and unregisters them with the content
 * repository.
 */
public class ImageStyleTracker extends ServiceTracker {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(ImageStyleTracker.class);

  /** The list of image styles */
  protected List<ImageStyle> styles = new ArrayList<ImageStyle>();

  /**
   * Creates a new tracker for {@link ImageStyle} instances.
   * 
   * @param context
   *          the bundle context
   */
  ImageStyleTracker(BundleContext context) {
    super(context, ImageStyle.class.getName(), null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    ImageStyle style = (ImageStyle) context.getService(reference);
    logger.debug("Found image style '{}'", style.getIdentifier());
    styles.add(style);
    return style;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(ServiceReference reference, Object service) {
    ImageStyle style = (ImageStyle) service;
    logger.debug("Image style '{}' went away", style.getIdentifier());
    styles.remove(style);
    super.removedService(reference, service);
  }

  /**
   * Returns the list of currently registered image styles.
   * 
   * @return the image styles
   */
  public Collection<ImageStyle> getImageStyles() {
    return Collections.unmodifiableCollection(styles);
  }

}
