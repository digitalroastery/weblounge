/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.preview.imagemagick;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator that will register the image preview generator depending on the
 * availability of the <code>ImageMagick</code> binaries.
 */
public class ImageMagickActivator {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(ImageMagickActivator.class);

  /** The page preview generator */
  private ImageMagickPreviewGenerator previewGenerator = null;

  /** The service reference */
  private ServiceRegistration service = null;

  /**
   * Callback from the OSGi container on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    if (imageMagickAvailable()) {
      logger.debug("Registering ImageMagick image preview generator");
      String[] classes = new String[] {
          PreviewGenerator.class.getName(),
          ImagePreviewGenerator.class.getName() };
      previewGenerator = new ImageMagickPreviewGenerator();
      service = ctx.getBundleContext().registerService(classes, previewGenerator, null);
    } else {
      logger.debug("Skipping registration of ImageMagick image preview generator");
    }
  }

  /**
   * Callback from the OSGi container on component inactivation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    try {
      logger.debug("Unregistering ImageMagick image preview generator");
      ctx.getBundleContext().ungetService(service.getReference());
    } catch (Throwable t) {
      // Never mind, seems like we are shutting down
    } finally {
      previewGenerator = null;
      service = null;
    }
  }

  /**
   * Returns <code>true</code> if the <code>ImageMagick</code> library is
   * available.
   * 
   * @return <code>true</code> if the library is available
   */
  private boolean imageMagickAvailable() {
    try {
      ConvertCmd cmd = new ConvertCmd();
      IMOperation versionOp = new IMOperation();
      versionOp.version();
      cmd.run(versionOp);
      logger.info("Using ImageMagick for image operations");
      return true;
    } catch (Throwable e) {
      logger.info("Using Java Advanced Imaging for image operations ({})", e.getMessage());
      return false;
    }
  }

}
