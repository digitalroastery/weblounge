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

package ch.o2it.weblounge.cache.impl.command;

import ch.o2it.weblounge.cache.impl.CacheConfigurationFactory;
import ch.o2it.weblounge.cache.impl.CacheServiceImpl;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * OSGi shell command implementation for the cache.
 */
public class CacheCommand {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(CacheCommand.class);

  /** The cache configuration factory */
  private CacheConfigurationFactory configFactory = null;

  /**
   * Command signature that allows to do
   * <ul>
   * <li><code>cache list</code></li>
   * <li><code>cache &lt;id&gt; status</code></li>
   * <li><code>cache &lt;id&gt; enable</code></li>
   * <li><code>cache &lt;id&gt; disable</code></li>
   * <li><code>cache &lt;id&gt; clear</code></li>
   * <li><code>cache &lt;id&gt; get &lt;property&gt;</code></li>
   * <li><code>cache &lt;id&gt; set &lt;property&gt; &lt;value&gt;</code></li>
   * <li><code>cache &lt;id&gt; get &lt;property&gt;</code></li>
   * </ul>
   * 
   * @param args
   *          the list of arguments to this command
   */
  public synchronized void cache(String[] args) {
    if (configFactory == null) {
      System.out.println("Cache service not found");
      return;
    }

    if (args.length == 0) {
      list();
      return;
    } else if (args.length == 1) {
      if ("list".equals(args[0])) {
        list();
      } else {
        printUsage();
      }
    } else if (args.length > 1) {
      String id = args[0];

      // Look up the cache
      Configuration config = getCache(id);
      if (config == null) {
        try {
          config = getCache(Integer.parseInt(id));
          if (config == null) {
            System.out.println("Unknown cache: " + id);
            return;
          }
        } catch (NumberFormatException e) {
          System.out.println("Unknown cache: " + id);
          return;
        }
      }

      // Process the command
      if ("enable".equals(args[1]))
        enable(config);
      else if ("disable".equals(args[1]))
        disable(config);
      else if ("clear".equals(args[1]))
        clear(config);
      else if ("status".equals(args[1]))
        status(config);
      else if ("get".equals(args[1]))
        get(config, Arrays.copyOfRange(args, 2, args.length));
      else if ("set".equals(args[1])) {
        set(config, Arrays.copyOfRange(args, 2, args.length));
      } else {
        System.out.println("Unknown command: " + args[1]);
        return;
      }
    } else {
      printUsage();
    }
  }

  /**
   * Prints a list of currently registered cache configurations.
   */
  private void list() {
    Configuration[] configurations = configFactory.getConfigurations();

    // Are there any cache configurations?
    if (configurations.length == 0) {
      System.out.println("No cache instances found");
      return;
    }

    // Setup the number formatter
    int digits = 1 + (int) (Math.log(configurations.length + 1) / Math.log(10));
    StringBuffer format = new StringBuffer();
    for (int i = 0; i < digits; i++)
      format.append("#");
    DecimalFormat formatter = new DecimalFormat(format.toString());

    // Display the cache list
    for (int i = 0; i < configurations.length; i++) {
      Configuration configuration = configurations[i];
      Dictionary<Object, Object> properties = getProperties(configuration);

      String id = (String) properties.get(CacheServiceImpl.OPT_ID);
      String name = (String) properties.get(CacheServiceImpl.OPT_NAME);
      boolean enabled = !ConfigurationUtils.isFalse((String) properties.get(CacheServiceImpl.OPT_ENABLE));

      StringBuffer buf = new StringBuffer();
      buf.append("[ ").append(formatter.format(i + 1)).append(" ] ");
      while (buf.length() < 8)
        buf.append(" ");
      buf.append(name != null ? name : id);
      buf.append(" ");
      int descriptionLength = buf.length();
      for (int j = 0; j < 64 - descriptionLength; j++)
        buf.append(".");
      buf.append(enabled ? " ENABLED " : " DISABLED");
      while (buf.length() < 22)
        buf.append(" ");
      System.out.println(buf.toString());
    }
  }

  /**
   * Prints out information about the cache.
   * 
   * @param cache
   *          the cache
   */
  private void status(Configuration cache) {
    Dictionary<?, ?> properties = cache.getProperties();
    for (Enumeration<?> keyEnum = properties.keys(); keyEnum.hasMoreElements();) {
      String key = keyEnum.nextElement().toString();
      String object = cache.getProperties().get(key).toString();
      pad(key, object);
    }
  }

  /**
   * Returns the cache configuration property identified by the first (and only)
   * item in <code>args</code>.
   * 
   * @param configuration
   *          the cache configuration
   * @param args
   *          the arguments
   */
  private void get(Configuration configuration, String[] args) {
    if (args.length != 1) {
      System.out.println("Please specify a configuration tkey");
      System.err.println("Usage: cache <id> get <key>");
      return;
    }

    Dictionary<Object, Object> properties = getProperties(configuration);
    String key = args[0];
    String value = (String) properties.get(key);

    pad(key, value != null ? value : "-");
  }

  /**
   * Returns the cache configuration property identified by the first (and only)
   * item in <code>args</code>.
   * 
   * @param configuration
   *          the cache configuration
   * @param args
   *          the arguments
   */
  private void set(Configuration configuration, String[] args) {
    if (args.length != 2) {
      System.out.println("Please specify a configuration tkey");
      System.err.println("Usage: cache <id> set <key> <value>");
      return;
    }

    Dictionary<Object, Object> properties = getProperties(configuration);
    String id = (String) properties.get(CacheServiceImpl.OPT_ID);
    String key = args[0];
    String value = args[1];
    properties.put(key, value);

    // Tell the configuration admin service to update the service
    try {
      configuration.update(properties);
    } catch (IOException e) {
      System.out.println("Error updating cache '" + id + "': " + e.getMessage());
    }
  }

  /**
   * Clears the contents of the cache.
   * 
   * @param configuration
   *          the cache configuration
   */
  private void clear(Configuration configuration) {
    Dictionary<Object, Object> properties = getProperties(configuration);
    String id = (String) properties.get(CacheServiceImpl.OPT_ID);
    properties.put(CacheServiceImpl.OPT_CLEAR, "true");

    // Tell the configuration admin service to update the service
    try {
      configuration.update(properties);
      properties.remove(CacheServiceImpl.OPT_CLEAR);
      configuration.update(properties);
      System.out.println("Cache '" + id + "' cleared");
    } catch (IOException e) {
      System.out.println("Error updating cache '" + id + "': " + e.getMessage());
    }
  }

  /**
   * Returns a padded version of the text.
   * 
   * @param caption
   *          the caption
   * @param info
   *          the information
   */
  private void pad(String caption, String info) {
    if (caption == null)
      caption = "";
    for (int i = 0; i < (15 - caption.length()); i++)
      System.out.print(" ");
    if (!"".equals(caption)) {
      System.out.print(caption);
      System.out.print(": ");
    } else {
      System.out.print("  ");
    }
    System.out.print(info);
    System.out.println();
  }

  /**
   * Enables the cache.
   * 
   * @param configuration
   *          the cache to enable
   */
  private void enable(Configuration configuration) {
    Dictionary<Object, Object> properties = getProperties(configuration);

    if (!ConfigurationUtils.isFalse((String) properties.get(CacheServiceImpl.OPT_ENABLE))) {
      System.out.println("Cache " + configuration + " is already enabled");
      return;
    }

    System.out.println("Enabling cache " + configuration);
    try {
      properties.put(CacheServiceImpl.OPT_ENABLE, "true");
      configuration.update(properties);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Disables the cache.
   * 
   * @param configuration
   *          the cache to disable
   */
  private void disable(Configuration configuration) {
    Dictionary<Object, Object> properties = getProperties(configuration);

    if (ConfigurationUtils.isFalse((String) properties.get(CacheServiceImpl.OPT_ENABLE))) {
      System.out.println("Cache " + configuration + " is already disbled");
      return;
    }

    try {
      configuration.delete();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Prints the command usage to the commandline.
   */
  private void printUsage() {
    System.out.println("  Usage:");
    System.out.println("    cache list");
    System.out.println("    cache <id> enable | disable");
    System.out.println("    cache <id> clear");
    System.out.println("    cache <id> status");
    System.out.println("    cache <id> get <property>");
    System.out.println("    cache <id> set <property> <value>");
  }

  /**
   * Returns the configuration of the cache with the given identifier or
   * <code>null</code> if no such cache was registered.
   * 
   * @param id
   *          the cache identifier
   * @return the cache configuration
   */
  private Configuration getCache(String id) {
    return configFactory.getConfiguration(id);
  }

  /**
   * Returns the cache configuration with the given index or <code>null</code>
   * if a wrong index is given.
   * 
   * @param index
   *          the cache configuration number
   * @return the cache configuration
   */
  private Configuration getCache(int index) {
    index--;
    Configuration[] configurations = configFactory.getConfigurations();
    return (index < configurations.length) ? configurations[index] : null;
  }

  /**
   * Returns the configuration properties.
   * 
   * @param config
   *          the cache configuration
   * @return the properties
   */
  @SuppressWarnings("unchecked")
  private Dictionary<Object, Object> getProperties(Configuration config) {
    Dictionary<Object, Object> properties = config.getProperties();
    if (properties == null)
      properties = new Hashtable<Object, Object>();
    return properties;
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    logger.debug("Registering cache commands");
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "weblounge");
    commands.put("osgi.command.function", new String[] { "cache" });
    bundleContext.registerService(getClass().getName(), this, commands);
  }

  /**
   * Callback from the OSGi declarative services environment that will pass in a
   * reference to the cache configuration factory.
   * 
   * @param factory
   *          the configuration factory
   */
  synchronized void setCacheConfigurationFactory(
      CacheConfigurationFactory factory) {
    configFactory = factory;
  }

  /**
   * Callback from the OSGi declarative services environment to indicate that
   * the reference to the cache configuration factory is no longer valid.
   * 
   * @param factory
   *          the configuration factory
   */
  synchronized void removeCacheConfigurationFactory(
      CacheConfigurationFactory factory) {
    configFactory = null;
  }

}
