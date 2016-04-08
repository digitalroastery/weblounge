/*
 * Weblounge: Web Content Management System Copyright (c) 2014 The Weblounge
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
package ch.entwine.weblounge.kernel.runtime;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides information about the current Weblounge instance.
 */
public class InstanceInformation {

  /** The name of the current instance */
  private String name = null;

  /** Property name of the Weblounge instance */
  private static final String OPT_INSTANCE_NAME = "ch.entwine.weblounge.name";

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(InstanceInformation.class);

  /**
   * OSGi component activation method.
   * 
   * @param context
   *          the component context
   */
  void activate(ComponentContext context) {
    if (context == null)
      throw new IllegalArgumentException("Component context must not be null");

    name = StringUtils.trimToNull(context.getBundleContext().getProperty(OPT_INSTANCE_NAME));
    if (name != null)
      logger.info("Instance name is '{}'", name);
    else
      logger.debug("No explicit instance name has been set");
  }

  /**
   * Returns the name of the current Weblounge instance or {@code null} if no
   * explicit instance name has been set.
   * 
   * @return the instance name
   */
  public String getName() {
    return name;
  }

}
