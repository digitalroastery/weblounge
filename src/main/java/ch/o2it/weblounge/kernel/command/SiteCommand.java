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

package ch.o2it.weblounge.kernel.command;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;

import org.osgi.framework.BundleContext;
import org.osgi.service.command.CommandProcessor;
import org.osgi.service.command.CommandSession;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * OSGi shell command implementation for sites.
 */
public class SiteCommand {

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(SiteCommand.class);
  
  /** The list of registered sites */
  private List<Site> sites = new ArrayList<Site>();

  /**
   * Command signature that allows to do
   * <ul>
   * <li><code>site list</code></li>
   * <li><code>site <id> start</code></li>
   * <li><code>site <id> stop</code></li>
   * <li><code>site <id> restart</code></li>
   * <li><code>site <id> enable</code></li>
   * <li><code>site <id> disable</code></li>
   * <li><code>site <id> status</code></li>
   * </ul>
   * 
   * @param session
   *          the command session
   * @param args
   *          the list of arguments to this command
   */
  public void site(CommandSession session, String[] args) {
    if (args.length == 0) {
      list();
      return;
    } else if (args.length == 1) {
      if ("list".equals(args[0])) {
        list();
      } else {
        printUsage();
      }
    } else if (args.length == 2) {
      String id = args[0];
      
      // Look up the site
      Site site = getSite(id);
      if (site == null) {
        try {
          site = getSite(Integer.parseInt(id));
          if (site == null) {
            System.out.println("Unknown site: " + id);
            return;
          }
        } catch (NumberFormatException e) {
          System.out.println("Unknown site: " + id);
          return;
        }
      }

      // Process the command
      if ("start".equals(args[1]))
        start(site);
      else if ("stop".equals(args[1]))
        stop(site);
      else if ("restart".equals(args[1]))
        restart(site);
      else if ("enable".equals(args[1]))
        enable(site);
      else if ("disable".equals(args[1]))
        disable(site);
      else if ("status".equals(args[1]))
        status(site);
      else {
        System.out.println("Unknown command: " + args[1]);
        return;
      }
    } else {
      printUsage();
    }
  }

  /**
   * Lists the registered sites.
   */
  public void sites() {
    list();
  }

  /**
   * Prints a list of currently registered sites.
   */
  private void list() {
    synchronized (sites) {
      
      // Are there any sites?
      if (sites.size() == 0) {
        System.out.println("No sites found");
        return;
      }
      
      // Setup the number formatter
      int digits = 1 + (int) (Math.log(sites.size() + 1) / Math.log(10));
      StringBuffer format = new StringBuffer();
      for (int i=0; i < digits; i++) format.append("#");
      DecimalFormat formatter = new DecimalFormat(format.toString());
      
      // Print the header
      StringBuffer header = new StringBuffer();
      for (int i=0; i < digits; i++)
        header.append(" ");
      header.append("Id    ");
      header.append(" State     ");
      header.append(" Name ");
      System.out.println(header.toString());
      
      // Display the site list
      for (int i=0; i < sites.size(); i++) {
        Site site = sites.get(i);
        StringBuffer buf = new StringBuffer();
        buf.append("[ ").append(formatter.format(i + 1)).append(" ] ");
        if (site.isEnabled())
          buf.append(site.isRunning() ? "[ started  ] " : "[ stopped  ] ");
        else
          buf.append(site.isEnabled() ? "[ enabled  ] " : "[ disabled ] ");
        buf.append(site.getDescription() != null ? site.getDescription() : site.getIdentifier());
        System.out.println(buf.toString());
      }
    }
  }

  /**
   * Prints out information about the site.
   * 
   * @param site
   *          the site
   */
  private void status(Site site) {
    status("identifier", site.getIdentifier());
    if (site.getDescription() != null)
      status("description", site.getDescription());
    
    // Enabled
    status("enabled", (site.isEnabled() ? "yes" : "no"));
 
    // Started / Stopped
    status("running", (site.isRunning() ? "yes" : "no"));

    // Hostnames
    if (site.getHostNames().length > 0)
      status("host", site.getHostNames());
    
    // Languages
    if (site.getLanguages().length > 0) {
      StringBuffer buf = new StringBuffer();
      for (Language language : site.getLanguages()) {
        if (buf.length() > 0)
          buf.append(", ");
        buf.append(language);
      }
      status("languages", buf.toString());
    }

    // Default language
    if (site.getDefaultLanguage() != null)
      status("default language", site.getDefaultLanguage().toString());
  }
  
  /**
   * Returns a padded version of the text.
   * 
   * @param caption the caption
   * @param info the information
   */
  private void status(String caption, String[] info) {
    for (int i=0; i < info.length; i++) {
      if (i == 0)
        status(caption, info[i]);
      else
        status(null, info[i]);
    }
  }
  
  /**
   * Returns a padded version of the text.
   * 
   * @param caption the caption
   * @param info the information
   */
  private void status(String caption, String info) {
    if (caption == null)
      caption = "";
    for (int i=0; i < (12 - caption.length()); i++)
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
   * Starts the site.
   * 
   * @param site
   *          the site to start
   */
  private void start(Site site) {
    if (site.isRunning()) {
      System.out.println("Site " + site + " is already running");
      return;
    } else if (!site.isEnabled()) {
      System.out.println("Cannot start disabled site " + site);
      return;
    }

    System.out.println("Starting site " + site);
    try {
      site.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Stops the site.
   * 
   * @param site
   *          the site to stop
   */
  private void stop(Site site) {
    if (!site.isRunning()) {
      System.out.println("Site " + site + " is already stopped");
      return;
    }
    System.out.println("Stopping site " + site);
    site.stop();
  }

  /**
   * Restarts the site.
   * 
   * @param site
   *          the site to restart
   */
  private void restart(Site site) {
    try {
      if (site.isRunning())
        site.stop();
      if (!site.isEnabled()) {
        System.out.println("Disabled site " + site + " cannot be started");
        return;
      }
      site.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Enables the site.
   * 
   * @param site
   *          the site to enable
   */
  private void enable(Site site) {
    if (site.isEnabled()) {
      System.out.println("Site " + site + " is already enabled");
      return;
    }
    System.out.println("Enabling site " + site);
    site.setEnabled(true);
  }

  /**
   * Stops the site.
   * 
   * @param site
   *          the site to stop
   */
  private void disable(Site site) {
    if (!site.isEnabled()) {
      System.out.println("Site " + site + " is already disable");
      return;
    }
    System.out.println("Disabling site " + site);
    site.setEnabled(false);
  }

  /**
   * Prints the command usage to the commandline.
   */
  private void printUsage() {
    System.out.println("  Usage:");
    System.out.println("    site list");
    System.out.println("    site <id> enable|disable");
    System.out.println("    site <id> start|stop|restart");
    System.out.println("    site <id> status");
  }

  /**
   * Returns the site with the given identifier or <code>null</code> if no such
   * site was registered.
   * 
   * @param id
   *          the site identifier
   * @return the site
   */
  private Site getSite(String id) {
    synchronized (sites) {
      for (Site site : sites) {
        if (site.getIdentifier().equals(id))
          return site;
      }
    }
    return null;
  }

  /**
   * Returns the site with the given index or <code>null</code> if a wrong index
   * is given.
   * 
   * @param index
   *          the site number
   * @return the site
   */
  private Site getSite(int index) {
    synchronized (sites) {
      index --;
      return (index < sites.size()) ? sites.get(index) : null;
    }
  }

  /**
   * Callback from the OSGi environment to activate the bundle.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    log_.info("Registering site shell commands");
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put(CommandProcessor.COMMAND_SCOPE, "weblounge");
    commands.put(CommandProcessor.COMMAND_FUNCTION, new String[] {
        "site",
        "sites" });
    bundleContext.registerService(getClass().getName(), this, commands);
  }

  /**
   * Adds a site to the list of registered sites.
   * 
   * @param site
   *          the site to add
   */
  public void addSite(Site site) {
    synchronized (sites) {
      sites.add(site);
    }
  }

  /**
   * Removes a site from the list of registered sites.
   * 
   * @param site
   *          the site to remove
   */
  public void removeSite(Site site) {
    synchronized (sites) {
      sites.remove(site);
    }
  }

}
