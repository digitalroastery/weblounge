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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;

/**
 * Main class that is coordinating support around Amazon Web Services (AWS).
 */
public class AmazonWebServices implements ManagedService {

  /** Value of the service pid, used for service configuration lookup */
  public static final String SERVICE_PID = "ch.entwine.weblounge.aws";

  /** Identifier used to access amazon web services */
  public static final String OPT_ACCESS_KEY_ID = "aws.accessKeyId";

  /** Secret used to access amazon web services */
  public static final String OPT_ACCESS_KEY_SECRET = "aws.accessKeySecret";

  /** The logging implementation */
  private static final Logger logger = LoggerFactory.getLogger(AmazonWebServices.class);
  
  /** Amazon web services access key id */
  private String accessKeyId = null;

  /** Amazon web services access key secret */
  private String accessKeySecret = null;
  
  /** Amazon web services access credentials */
  private AWSCredentials accessCredentials = null;
  
  /** The Amazon S3 resource serializer */
  private S3ResourceSerializer s3Serializer = null;

  /**
   * Activates the service implementation.
   * 
   * @param cc
   *          the component context
   * @throws ConfigurationException
   *           if service configuration fails
   */
  void activate(ComponentContext cc) throws ConfigurationException {
    BundleContext bundleCtx = cc.getBundleContext();
    ServiceReference ref = bundleCtx.getServiceReference(ConfigurationAdmin.class.getName());
    if (ref != null) {
      ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleCtx.getService(ref);
      Configuration config;
      try {
        config = configurationAdmin.getConfiguration(SERVICE_PID);
        if (config != null && config.getProperties() != null)
          updated(config.getProperties());
        else
          throw new IllegalStateException("Amazon web services need configuration");
      } catch (IOException e) {
        logger.error("Error trying to look up datasource configuration", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    
    // Read the access key id
    accessKeyId = (String)properties.get(OPT_ACCESS_KEY_ID);
    if (StringUtils.isBlank(accessKeyId))
      throw new ConfigurationException(OPT_ACCESS_KEY_ID, "not set");
    logger.debug("Amazon access key id is '{}'", accessKeyId);
      
    // Read the access key secret–
    accessKeySecret = (String)properties.get(OPT_ACCESS_KEY_SECRET);
    if (StringUtils.isBlank(accessKeySecret))
      throw new ConfigurationException(OPT_ACCESS_KEY_SECRET, "not set");
    logger.debug("Amazon access key secret is '{}'", accessKeySecret);
    
    // Create the credentials
    accessCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
    
    s3Serializer = new S3ResourceSerializer(accessCredentials);
  }

}
