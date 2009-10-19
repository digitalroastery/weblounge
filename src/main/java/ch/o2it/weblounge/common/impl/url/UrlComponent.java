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

import ch.o2it.weblounge.common.Lease;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.pool.LeaseFactory;
import ch.o2it.weblounge.common.impl.util.pool.Pool;
import ch.o2it.weblounge.common.url.Url;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A url component represents a path element within a url, e. g. <tt>news</tt>
 * in <tt>/news/late/index.html</tt>.
 * 
 * TODO: Check what happens with encoded urls
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class UrlComponent implements Lease {

  /** The component path */
  private String path_;

  /** The url with this component as its tail */
  private Url url_;

  /** The parent component */
  private UrlComponent previous_;

  /** The component children */
  private Map<String, UrlComponent> children_;

  /** The object pool */
  private static Pool<UrlComponent> objectPool_ = new Pool<UrlComponent>("urlcomponent", new UrlComponentFactory());

  /**
   * This constructor is being used by the object pool.
   */
  UrlComponent() {
    children_ = new HashMap<String, UrlComponent>();
  }

  /**
   * Returns a new url component with <code>previous</code> as then parent.
   * 
   * @param path
   *          the url component path
   * @param previous
   *          the parent component
   * @return the url component
   */
  public static UrlComponent getLease(String path, UrlComponent previous) {
    UrlComponent c = objectPool_.getLease();
    c.path_ = path;
    c.previous_ = previous;
    return c;
  }

  /**
   * Returns the leased url component.
   * 
   * @param c
   *          the component
   * @return the url component
   */
  public static void returnLease(UrlComponent c) {
    objectPool_.returnLease(c);
  }

  /**
   * Sets the component path. Calling this method sends a
   * <code>componentChanged</code> to the url that has this component as its
   * tail and all subsequent urls.
   * <p>
   * Note that <code>null</code> is not a valid path.
   * 
   * @param path
   *          the new path
   */
  public void setPath(String path) {
    Arguments.checkNull(path, "path");
    path_ = path;
    // fireComponentChanged(this);
  }

  /**
   * Returns the path element that is represented by this url component.
   * <p>
   * For example, if this url component represents the head of the url
   * <code>/home/news</code> then this method will return <code>home</code>.
   * 
   * @return the path of this url component
   */
  public String getPath() {
    return path_;
  }

  /**
   * Sets the url that has this url component as its tail. The url then consists
   * of this component and all its ancestors. Note that <code>null</code> is not
   * allowed as a url.
   * 
   * @param url
   *          the enclosing url
   */
  public void setUrl(Url url) {
    Arguments.checkNull(url, "url");
    url_ = url;
  }

  /**
   * Returns the url that has this component as its tail.
   * <p>
   * <b>Note:</b> If the {@link #setUrl(Url)} method has not yet been called,
   * then this method will return <code>null</code>
   * 
   * @return the associated url
   * @see #isTail()
   */
  public Url getUrl() {
    return url_;
  }

  /**
   * Returns <code>true</code> if this component has at least one parent
   * component. If this method returns <code>true</code>, then {@link #isHead()}
   * will return <code>false</code>.
   * 
   * @return </code>true</code> if this is not the url head component
   */
  public boolean hasPrevious() {
    return previous_ != null;
  }

  /**
   * Sets the preceeding component. Calling this method sends a
   * <code>componentChanged</code> to the url that has this component as its
   * tail and all subsequent urls.
   * 
   * @param previous
   *          the new preceeding component
   */
  public void setPrevious(UrlComponent previous) {
    previous_ = previous;
    // fireComponentChanged(this);
  }

  /**
   * Returns the preceeding url comonent. Note that this method may return
   * <code>null</code> if the component represents the site root component.
   * 
   * @return the preceeding url component
   */
  public UrlComponent getPrevious() {
    return previous_;
  }

  /**
   * Returns <code>true</code> if this component has at least one child
   * component.
   * 
   * @return </code>true</code> if this is not the url tail component
   * @see #hasNext(String)
   */
  public boolean hasNext() {
    return children_.size() > 0;
  }

  /**
   * Returns <code>true</code> if this component has a child component with path
   * <code>path</code>.
   * 
   * @return </code>true</code> if the child component exists
   * @see #hasNext()
   */
  public boolean hasNext(String path) {
    return children_.get(path) != null;
  }

  /**
   * Returns <code>true</code> if this component has no parent url component.
   * 
   * @return <code>true</code> if this is the first component in the url
   */
  boolean isHead() {
    return (previous_ == null);
  }

  /**
   * Returns <code>true</code> if no extension exists to the url that has this
   * url component as its tail. In this case, the component marks a leaf in the
   * url tree.
   * 
   * @return <code>true</code> if this is a leaf component in the url tree
   */
  boolean isTail() {
    return children_.size() == 0;
  }

  /**
   * Sets a subsequent url component, which will become a child of this one.
   * 
   * @param next
   *          a child path component in the url
   */
  public void addChild(UrlComponent next) {
    children_.put(next.getPath(), next);
  }

  /**
   * Removes the url component with path <code>path</code>.
   * 
   * @param path
   *          the url component path
   */
  public void removeChild(String path) {
    children_.remove(path);
  }

  /**
   * Returns an iteration of all subsequent path components.
   * 
   * @return the children of this component
   */
  public Iterator children() {
    return children_.values().iterator();
  }

  /**
   * Returns the hash code for this url component which is equal to the hash
   * code derived from the path.
   * 
   * @return the hash code
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (path_ != null) ? path_.hashCode() : super.hashCode();
  }

  /**
   * Returns <code>true</code> if <code>obj</code> is of type
   * <code>UrlComponent</code> object literally representing the same instance
   * than this one.
   * 
   * @param obj
   *          the object to test for equality
   * @return <code>true</code> if <code>obj</code> represents the same
   *         <code>UrlComponent</code>
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof UrlComponent) {
      UrlComponent o = (UrlComponent) obj;
      if (path_ == null || o.path_ == null) {
        return this == obj;
      } else {
        return o.path_.equals(path_) && ((previous_ == null && o.previous_ == null) || (previous_ == o.previous_) || o.getPrevious().equals(previous_));
      }
    }
    return false;
  }

  /**
   * Returns the string representation of this url component which is equal to
   * its path.
   * 
   * @return the url component path
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return (path_ != null) ? path_ : "<n/a>";
  }

  /**
   * Tells the enclosing url and all url extensions that the component
   * <code>c</code> has changed.
   * 
   * @param c
   *          the changing component
   */
  // protected void fireComponentChanged(UrlComponent c) {
  // if (url_ != null)
  // url_.componentChanged(c);
  //		
  // // This message also has to reach all url extensions:
  //		
  // Iterator ci = children();
  // while (ci.hasNext()) {
  // ((UrlComponent)ci.next()).fireComponentChanged(c);
  // }
  // }

  /**
   * @see ch.o2it.weblounge.common.Lease#leased()
   */
  public void leased() {
  }

  /**
   * @see ch.o2it.weblounge.common.Lease#returned()
   */
  public void returned() {
    path_ = null;
    previous_ = null;
    children_.clear();
    url_ = null;
  }

  /**
   * @see ch.o2it.weblounge.common.Lease#dispose()
   */
  public boolean dispose() {
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.Lease#retired()
   */
  public void retired() {
  }

  /**
   * Factory method wich produces UrlComponent leases.
   * 
   * @author Tobias Wunden
   */
  static class UrlComponentFactory implements LeaseFactory<UrlComponent> {

    /**
     * @see ch.o2it.weblounge.common.impl.util.pool.LeaseFactory#createLease()
     */
    public UrlComponent createLease() {
      return new UrlComponent();
    }

    /**
     * @see ch.o2it.weblounge.common.impl.util.pool.LeaseFactory#disposeLease(ch.o2it.weblounge.common.Lease)
     */
    public void disposeLease(UrlComponent lease) {
      lease = null;
    }

  }

}