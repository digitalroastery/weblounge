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

package ch.o2it.weblounge.mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

/**
 * Mock implementation of the Servlet 2.4 API
 * {@link javax.servlet.http.HttpSession} interface.
 */
@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

  public static final String SESSION_COOKIE_NAME = "JSESSION";

  /** Next session id */
  private static int nextId = 1;

  /** The current session id */
  private final String id;

  /** Time of creation */
  private final long creationTime = System.currentTimeMillis();

  /** Session timeout */
  private int maxInactiveInterval;

  /** Timestamp for the last access to this session */
  private long lastAccessedTime = System.currentTimeMillis();

  /** The servlet context */
  private final ServletContext servletContext;

  /** Session attributes */
  private final Hashtable<String, Object> attributes = new Hashtable<String, Object>();

  /** Is this session still valid? */
  private boolean invalid = false;

  /** Is this session new? */
  private boolean isNew = true;

  /**
   * Create a new MockHttpSession with a default {@link MockServletContext}.
   * 
   * @see MockServletContext
   */
  public MockHttpSession() {
    this(null);
  }

  /**
   * Create a new MockHttpSession.
   * 
   * @param servletContext
   *          the ServletContext that the session runs in
   */
  public MockHttpSession(ServletContext servletContext) {
    this(servletContext, null);
  }

  /**
   * Create a new MockHttpSession.
   * 
   * @param servletContext
   *          the ServletContext that the session runs in
   * @param id
   *          a unique identifier for this session
   */
  public MockHttpSession(ServletContext servletContext, String id) {
    this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
    this.id = (id != null ? id : Integer.toString(nextId++));
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getCreationTime()
   */
  public long getCreationTime() {
    return this.creationTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getId()
   */
  public String getId() {
    return this.id;
  }

  /**
   * Simulates access to this session by adjusting the last access time and by
   * marking the session as not new.
   */
  public void access() {
    this.lastAccessedTime = System.currentTimeMillis();
    this.isNew = false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getLastAccessedTime()
   */
  public long getLastAccessedTime() {
    return this.lastAccessedTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getServletContext()
   */
  public ServletContext getServletContext() {
    return this.servletContext;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
   */
  public void setMaxInactiveInterval(int interval) {
    this.maxInactiveInterval = interval;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
   */
  public int getMaxInactiveInterval() {
    return this.maxInactiveInterval;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getSessionContext()
   */
  public HttpSessionContext getSessionContext() {
    throw new UnsupportedOperationException("getSessionContext");
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) {
    assertNotNull("Attribute name must not be null", name);
    return this.attributes.get(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
   */
  public Object getValue(String name) {
    return getAttribute(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getAttributeNames()
   */
  @SuppressWarnings("unchecked")
  public Enumeration getAttributeNames() {
    return this.attributes.keys();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#getValueNames()
   */
  public String[] getValueNames() {
    return this.attributes.keySet().toArray(new String[this.attributes.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String,
   *      java.lang.Object)
   */
  public void setAttribute(String name, Object value) {
    assertNotNull(name, "Attribute name must not be null");
    if (value != null) {
      this.attributes.put(name, value);
      if (value instanceof HttpSessionBindingListener) {
        ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
      }
    } else {
      removeAttribute(name);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#putValue(java.lang.String,
   *      java.lang.Object)
   */
  public void putValue(String name, Object value) {
    setAttribute(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
   */
  public void removeAttribute(String name) {
    assertNotNull("Attribute name must not be null", name);
    Object value = this.attributes.remove(name);
    if (value instanceof HttpSessionBindingListener) {
      ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
   */
  public void removeValue(String name) {
    removeAttribute(name);
  }

  /**
   * Clear all of this session's attributes.
   */
  public void clearAttributes() {
    for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Object> entry = it.next();
      String name = entry.getKey();
      Object value = entry.getValue();
      it.remove();
      if (value instanceof HttpSessionBindingListener) {
        ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#invalidate()
   */
  public void invalidate() {
    this.invalid = true;
    clearAttributes();
  }

  /**
   * Returns <code>true</code> if this session has been invalidated.
   * 
   * @return <code>true</code> if the session was invalidated
   */
  public boolean isInvalid() {
    return this.invalid;
  }

  /**
   * Marks this session as new.
   * 
   * @param value
   *          the <code>new</code> value
   */
  public void setNew(boolean value) {
    this.isNew = value;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpSession#isNew()
   */
  public boolean isNew() {
    return this.isNew;
  }

  /**
   * Serialize the attributes of this session into an object that can be turned
   * into a byte array with standard Java serialization.
   * 
   * @return a representation of this session's serialized state
   */
  public Serializable serializeState() {
    HashMap<String, Object> state = new HashMap<String, Object>();
    for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Object> entry = it.next();
      String name = entry.getKey();
      Object value = entry.getValue();
      it.remove();
      if (value instanceof Serializable) {
        state.put(name, value);
      } else {
        // Not serializable... Servlet containers usually automatically
        // unbind the attribute in this case.
        if (value instanceof HttpSessionBindingListener) {
          ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
      }
    }
    return state;
  }

  /**
   * Deserialize the attributes of this session from a state object created by
   * {@link #serializeState()}.
   * 
   * @param state
   *          a representation of this session's serialized state
   */
  @SuppressWarnings("unchecked")
  public void deserializeState(Serializable state) {
    assertTrue("Serialized state needs to be of type [java.util.Map]", state instanceof Map<?, ?>);
    this.attributes.putAll((Map<String, Object>) state);
  }

}
