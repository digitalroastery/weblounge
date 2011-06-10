package ch.entwine.weblounge.cache.impl;

import org.osgi.service.cm.Configuration;

import java.util.Dictionary;

/**
 * Class that keeps the information about a cache configuration object in sync
 * with its actual state.
 */
public class CacheConfiguration {

  /** The configuration admin service object */
  private Configuration config = null;

  /** The cache identifier */
  private String id = null;

  /** The cache name */
  private String name = null;

  /** True if the configuration is enabled */
  private boolean enabled = true;

  /** The configuration properties */
  private Dictionary<Object, Object> properties = null;

  /**
   * Creates a new cache configuration for the cache with identifier
   * <code>id</code>, name <code>name</code> and the given, initial
   * configuration.
   * 
   * @param id
   *          the cache identifier
   * @param name
   *          the cache name
   */
  public CacheConfiguration(String id, String name) {
    this.id = id;
    this.name = name;
    this.enabled = false;
  }

  /**
   * Returns the configuration created by the configuration admin service.
   * 
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return config;
  }

  /**
   * Updates the configuration object.
   * 
   * @param config
   *          the updated configuration
   */
  public void setConfiguration(Configuration config) {
    this.config = config;
    enabled = config != null;
  }

  /**
   * Returns the cache identifier.
   * 
   * @return the identifier
   */
  public String getIdentifier() {
    return id;
  }

  /**
   * Returns the cache name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns <code>true</code> if this configuration is currently active.
   * 
   * @return <code>true</code> if the configuration is active
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns the configuration properties.
   * 
   * @return the properties
   */
  public Dictionary<Object, Object> getProperties() {
    return properties;
  }

  /**
   * Sets the configuration properties.
   * 
   * @param properties
   *          the properties
   */
  public void setProperties(Dictionary<Object, Object> properties) {
    this.properties = properties;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return id.hashCode();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CacheConfiguration) {
      return id.equals(((CacheConfiguration)obj).getIdentifier());
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}