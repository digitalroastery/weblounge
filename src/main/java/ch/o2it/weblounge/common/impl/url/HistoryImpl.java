/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
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

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.request.History;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The history object tracks the user's movement and can be used to reload the
 * last url, step back etc. A history object is attached to every session and
 * can be retreived by calling <code>RequestSupport.getHistory()</code>
 */
public class HistoryImpl implements History {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(HistoryImpl.class);

  /** The default history size */
  public final static int DEFAULT_SIZE = 5;

  /** the history container */
  private List<WebUrl> history_ = null;

  /** number of entries in the history */
  private int maxSize_ = 0;

  /** The associated site */
  private Site site_ = null;

  /**
   * Creates an empty <code>History</code> object with the default of 5 history
   * entries.
   */
  public HistoryImpl(Site site) {
    this(new WebUrlImpl(site, "/"));
  }

  /**
   * Creates an empty <code>History</code> object with the default of 5 history
   * entries.
   * 
   * @param url
   *          the base url
   */
  public HistoryImpl(WebUrl url) {
    maxSize_ = DEFAULT_SIZE;
    history_ = new ArrayList<WebUrl>(maxSize_);
    site_ = url.getSite();
    addEntry(url);
  }

  /**
   * Adds a navigation entry to this history object. If the history features
   * more then the maximum number of possible entries, then the least recent
   * entry is discarded.
   * <p>
   * Note that the history object refuses to historize urls of type wizard and
   * control center command.
   * 
   * @param url
   *          the entry
   */
  public void addEntry(WebUrl url) {
    if (!skipUrl(url)) {
      if (history_.size() >= maxSize_) {
        history_.remove(maxSize_ - 1);
        log_.debug("Removing oldest entry from history");
      }
      history_.add(0, url);
      log_.debug("Added " + url + " to history");
    }
  }

  /**
   * Returns <code>true</code> if this url should be skipped from history.
   * 
   * @param url
   *          the url
   */
  protected boolean skipUrl(WebUrl url) {
    if (url == null)
      return true;
    String path = url.getPath();
    if (path.endsWith("/"))
      path = path.substring(0, path.length() - 1);

    // Skip paths of wizard and control center calls
    if (path.endsWith(".wiz") || path.endsWith(".wbl"))
      return true;

    return false;
  }

  /**
   * Returns an entry from the navigation history. Note that the entry with the
   * lowest possible index <code>0</code> is the most recent.<br>
   * If no entry can be found for the given index, then the root page is
   * returned as the url.
   * 
   * @return an entry from the navigation history
   */
  public WebUrl getMove(int step) {
    if (step < history_.size()) {
      return history_.get(step);
    } else {
      log_.warn("Attempt to retreive unknown history entry [step=" + step + "; size=" + history_.size() + "]");
      throw new IndexOutOfBoundsException("History only has " + history_.size() + " entries!");
    }
  }

  /**
   * Return the most recent entry in the history table.
   * 
   * @return the most recent entry
   */
  public WebUrl getLatestMove() {
    WebUrl move = getMove(0);
    log_.debug("Last move was " + move);
    return move;
  }

  /**
   * Removes the most recent move from the navigation history.
   */
  public void discardLatestMove() {
    switch (history_.size()) {
    case 1:
      history_.remove(0);
    case 0:
      addEntry(new WebUrlImpl(site_, "/"));
      break;
    default:
      history_.remove(0);
      log_.debug("Discarding latest move in history");
      break;
    }
  }

  /**
   * Removes the <code>count</code> most recent moves from the navigation
   * history.
   * 
   * @param count
   *          remove this number of entries
   */
  public void discardLatestMoves(int count) {
    while ((history_.size() > 0) && count > 0) {
      history_.remove(0);
      count--;
      log_.debug("Discarding latest " + count + " moves in history");
    }
    if (history_.size() == 0) {
      addEntry(new WebUrlImpl(site_, "/"));
    }
  }

  /**
   * Returns the current number of entries in this history.
   * 
   * @return the number of history entries
   */
  public int size() {
    return history_.size();
  }

  /**
   * Returns the maximum number of entries
   */
  protected int getMaximumSize() {
    return maxSize_;
  }

  /**
   * Sets the number of entries that are kept in the history.
   * 
   * @param size
   *          number of history entries
   */
  protected void setMaximumSize(int size) {
    maxSize_ = size;
  }

}