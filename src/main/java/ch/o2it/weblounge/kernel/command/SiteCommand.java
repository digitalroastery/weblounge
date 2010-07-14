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

import ch.o2it.weblounge.common.content.Composer;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.SearchResultItem;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;

/**
 * OSGi shell command implementation for sites.
 */
public class SiteCommand {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(SiteCommand.class);

  /** The list of registered sites */
  private List<Site> sites = new ArrayList<Site>();

  /**
   * Command signature that allows to do
   * <ul>
   * <li><code>site list</code></li>
   * <li><code>site &lt;id&gt; start</code></li>
   * <li><code>site &lt;id&gt; stop</code></li>
   * <li><code>site &lt;id&gt; restart</code></li>
   * <li><code>site &lt;id&gt; enable</code></li>
   * <li><code>site &lt;id&gt; disable</code></li>
   * <li><code>site &lt;id&gt; index</code></li>
   * <li><code>site &lt;id&gt; status</code></li>
   * <li><code>site &lt;id&gt; inspect &lt;url&gt;</code></li>
   * <li><code>site &lt;id&gt; search &lt;terms&gt;</code></li>
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
    } else if (args.length > 1) {
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
      else if ("inspect".equals(args[1]))
        inspect(site, Arrays.copyOfRange(args, 2, args.length));
      else if ("search".equals(args[1])) {
        search(site, Arrays.copyOfRange(args, 2, args.length));
      } else {
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
    pad("identifier", site.getIdentifier());
    if (site.getName() != null)
      pad("description", site.getName());

    // Enabled
    pad("autostart", (site.isStartedAutomatically() ? "yes" : "no"));

    // Started / Stopped
    pad("running", (site.isRunning() ? "yes" : "no"));

    // Hostnames
    if (site.getHostNames().length > 0)
      pad("hosts", site.getHostNames());

    // Languages
    if (site.getLanguages().length > 0) {
      StringBuffer buf = new StringBuffer();
      if (site.getDefaultLanguage() != null) {
        buf.append(site.getDefaultLanguage());
        buf.append(" (default)");
      }
      for (Language language : site.getLanguages()) {
        if (language.equals(site.getDefaultLanguage()))
          continue;
        if (buf.length() > 0)
          buf.append(", ");
        buf.append(language);
      }
      pad("languages", buf.toString());
    }

    // Pages and revisions
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    long pages = repository != null ? repository.getPages() : -1;
    pad("pages", (pages >= 0 ? Long.toString(pages) : "n/a"));
    long revisions = repository != null ? repository.getVersions() : -1;
    pad("revisions", (revisions >= 0 ? Long.toString(revisions) : "n/a"));
  }

  /**
   * Prints out information about the given url or page identifier.
   * 
   * @param site
   *          the site
   * @param args
   *          arguments to this function
   */
  private void inspect(Site site, String[] args) {
    if (args.length == 0) {
      System.err.println("Please specify what to inspect");
      System.err.println("Usage: site <id> inspect <url>|<id>");
      return;
    }

    // What are we looking at?
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    Page page = null;

    // Is it a page?
    try {
      String objectId = args[0];
      if (objectId.startsWith("/"))
        page = repository.getPage(new PageURIImpl(site, args[0]));
      else
        page = repository.getPage(PageURIImpl.fromId(site, args[0]));
      if (page != null) {
        title("page");
        pad("id", page.getURI().getId().toString());
        pad("path", page.getURI().getPath());
        
        section("lifecycle");
        
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);

        // Created
        if (page.getCreationDate() != null) {
          StringBuffer buf = new StringBuffer();
          buf.append(df.format(page.getCreationDate()));
          if (page.getCreator() != null) {
            buf.append(" by ").append(page.getCreator().toString());
          }
          pad("created", buf.toString());
        }

        // Modified
        if (page.getModificationDate() != null) {
          StringBuffer buf = new StringBuffer();
          buf.append(df.format(page.getModificationDate()));
          if (page.getModifier() != null) {
            buf.append(" by ").append(page.getModifier().toString());
          }
          pad("modified", buf.toString());
        }

        // Published
        if (page.getPublishFrom() != null) {
          StringBuffer buf = new StringBuffer();
          buf.append(df.format(page.getPublishFrom()));
          if (page.getPublisher() != null) {
            buf.append(" by ").append(page.getPublisher().toString());
          }
          pad("published", buf.toString());
          if (page.getPublishTo() != null)
            pad("published until", df.format(page.getPublishTo()));
        }

        section("header");

        if (page.getTitle() != null)
          pad("title", page.getTitle());

        // subjects
        StringBuffer subjectList = new StringBuffer();
        for (String c : page.getSubjects()) {
          if (subjectList.length() > 0)
            subjectList.append(", ");
          subjectList.append(c);
        }
        pad("subjects", subjectList.toString());


        section("content");
        
        // composers
        StringBuffer composerList = new StringBuffer();
        for (Composer c : page.getComposers()) {
          if (composerList.length() > 0)
            composerList.append(", ");
          composerList.append(c.getIdentifier());
          composerList.append(" (").append(c.getPagelets().length).append(")");
        }
        pad("composers", composerList.toString());

      }
    } catch (ContentRepositoryException e) {
      System.err.println("Error trying to access the content repository");
      e.printStackTrace(System.err);
    }
  }

  /**
   * Does a search using the terms from the arguments and displays the search
   * hits on the console.
   * 
   * @param site
   *          the site
   * @param args
   *          search terms
   */
  private void search(Site site, String[] args) {
    StringBuffer text = new StringBuffer();
    if (args.length == 0) {
      System.out.println("Please specify a search term");
      System.err.println("Usage: site <id> search <terms>");
      return;
    } else {
      for (String s : args) {
        if (text.length() > 0)
          text.append(" ");
        text.append(s);
      }
    }

    // Get hold of the content repository
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    SearchQuery query = new SearchQueryImpl(site);
    query.withText(text.toString());

    // Is it a page?
    try {
      SearchResult result = repository.findPages(query);
      
      // Format the output
      Formatter formatter = new Formatter(System.out);
      StringBuffer format = new StringBuffer();
      int padding = Long.toString(result.getDocumentCount()).length();
      format.append("%1$#").append(padding).append("s. %2$s\n");
      
      // List results
      int i = 1;
      for (SearchResultItem item : result.getItems()) {
        formatter.format(format.toString(), i++, item.getUrl());
      }

      System.out.println("Found " + result.getDocumentCount() + " results (" + result.getSearchTime() + " ms)");
    } catch (ContentRepositoryException e) {
      System.err.println("Error trying to access the content repository");
      e.printStackTrace(System.err);
    }
  }

  /**
   * Triggers a rebuilding of the site index.
   * 
   * @param site
   *          the site
   */
  private void index(Site site) {
    boolean restart = site.isRunning();

    // Make sure we are in good shape before we index
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    if (repository == null) {
      System.out.println("Site " + site + " has no content repository");
      return;
    } else if (!(repository instanceof WritableContentRepository)) {
      System.out.println("Site " + site + " is read only");
      return;
    } else if (site.isRunning()) {
      while (true) {
        String answer = System.console().readLine("Can't index a running site! Stop now? [y/n] ");
        if ("y".equalsIgnoreCase(answer)) {
          stop(site);
          System.out.println();
          break;
        } else if ("n".equalsIgnoreCase(answer)) {
          return;
        } else {
          answer = null;
        }
      }
    }

    // Finally! Let's do the work
    System.out.println("Indexing site " + site);
    try {
      ((WritableContentRepository) repository).index();
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
    }

    // Restart the site if we shut it down previously
    System.out.println("Site " + site + " indexed");
    if (restart) {
      System.out.println();
      start(site);
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
  private void pad(String caption, String[] info) {
    for (int i = 0; i < info.length; i++) {
      if (i == 0)
        pad(caption, info[i]);
      else
        pad(null, info[i]);
    }
  }

  /**
   * Returns a padded version of the title.
   * 
   * @param title
   *          the title
   */
  private void title(String title) {
    for (int i = 0; i < (15 - title.length()); i++)
      System.out.print(" ");
    System.out.println(title);
  }

  /**
   * Returns a padded version of the section title.
   * 
   * @param title
   *          the title
   */
  private void section(String title) {
    System.out.println();
    for (int i = 0; i < (15 - title.length()); i++)
      System.out.print(" ");
    System.out.println(title.toUpperCase());
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
    System.out.println("    site <id> start|stop|restart");
    System.out.println("    site <id> index");
    System.out.println("    site <id> status");
    System.out.println("    site <id> inspect <url|id>");
    System.out.println("    site <id> search <terms>");
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
    logger.debug("Registering site commands");
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
