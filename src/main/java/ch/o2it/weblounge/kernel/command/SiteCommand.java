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
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;

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
   * <li><code>site <id> index</code></li>
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
      else if ("index".equals(args[1]))
        index(site);
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
      for (int i = 0; i < digits; i++)
        format.append("#");
      DecimalFormat formatter = new DecimalFormat(format.toString());

      // Display the site list
      for (int i = 0; i < sites.size(); i++) {
        Site site = sites.get(i);
        StringBuffer buf = new StringBuffer();
        buf.append("[ ").append(formatter.format(i + 1)).append(" ] ");
        while (buf.length() < 8)
          buf.append(" ");
        buf.append(site.getName() != null ? site.getName() : site.getIdentifier());
        buf.append(" ");
        int descriptionLength = buf.length();
        for (int j = 0; j < 64 - descriptionLength; j++)
          buf.append(".");
        buf.append(site.isRunning() ? " STARTED " : " STOPPED");
        while (buf.length() < 22)
          buf.append(" ");
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
    if (site.getName() != null)
      status("description", site.getName());

    // Enabled
    status("autostart", (site.isStartedAutomatically() ? "yes" : "no"));

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
   * Triggers a rebuilding of the site index.
   * 
   * @param site
   *          the site
   */
  private void index(Site site) {
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    if (repository == null) {
      System.out.println("Site " + site + " has no content repository");
      return;
    } else if (!site.isRunning()) {
      System.out.println("Site " + site + " is not running");
      return;
    } else if (!(repository instanceof WritableContentRepository)) {
      System.out.println("Site " + site + " is read only");
      return;
    }
    
    System.out.println("Indexing site " + site);

    try {
      ((WritableContentRepository)repository).index();
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
    }

    System.out.println("Index for site " + site + " rebuilt");
  }

  /**
   * Returns a padded version of the text.
   * 
   * @param caption
   *          the caption
   * @param info
   *          the information
   */
  private void status(String caption, String[] info) {
    for (int i = 0; i < info.length; i++) {
      if (i == 0)
        status(caption, info[i]);
      else
        status(null, info[i]);
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
  private void status(String caption, String info) {
    if (caption == null)
      caption = "";
    for (int i = 0; i < (12 - caption.length()); i++)
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
      if (site.isRunning()) {
        site.stop();
        site.start();
      } else if (site.isStartedAutomatically()) {
        site.start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Prints the command usage to the commandline.
   */
  private void printUsage() {
    System.out.println("  Usage:");
    System.out.println("    site list");
    System.out.println("    site <id> enable|disable");
    System.out.println("    site <id> start|stop|restart");
    System.out.println("    site <id> index");
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
      index--;
      return (index < sites.size()) ? sites.get(index) : null;
    }
  }

  /**
   * Callback from the OSGi environment to activate the commands.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    log_.debug("Registering site commands");
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
