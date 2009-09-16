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

package ch.o2it.weblounge.common.impl.util.pool;

import ch.o2it.weblounge.common.impl.Lease;
import ch.o2it.weblounge.common.impl.util.Arguments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * <code>Pool</code> is used to pool leases so that they are ready to use when
 * needed.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class Pool<T extends Lease> {

  /** Minimum number of leases to create by default */
  public static final int MIN_LEASES = 1;

  /** Minimum number of leases to keep by default */
  public static final int KEEP_LEASES = 2;

  /** Minimum number of leases to create in maximum */
  public static final int MAX_LEASES = Integer.MAX_VALUE;

  /** Default number of leases to create when a new one is needed */
  public static final int DEFAULT_STEP_SIZE = 2;

  /** The default frequency for the pool cleanup thread */
  public static final int DEFAULT_CLEANUP_FREQUENCY = 30;

  /** The pool identifier */
  private String id_;

  /** Lease counter */
  protected int leases_;

  /** leases that are currently inside the pool */
  protected Stack<T> pool_;

  /** Factory to create leases */
  private LeaseFactory<T> factory_;

  /** Minimum number of leases to create initially */
  private int min_;

  /** Number of leases to hold */
  private int keep_;

  /** Maximum number of leases to pool */
  private int max_;

  /** Maximum number of leases */
  private int leasedMax_;

  /** The amount of leases to create at once when needed */
  private int step_;

  /** Time to leave between two reconcilations */
  private long reconcileTime_ = 5L * 1000L;

  /** The last time that a lease has been removed */
  private long lastReconcilation_ = System.currentTimeMillis();

  /** True to clean pool on lease return */
  private boolean cleanOnReturn_;

  /** The pool cleanup thread */
  private PoolCleanupThread cleaner_;

  /** The listener list */
  protected List<PoolListener> listeners;

  /** Frequency of the pool cleanup thread */
  protected long frequency_;

  /** True if the pool has been closed */
  private boolean isClosed_;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = Pool.class.getName();

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new pool with the given parameters.
   * 
   * @param id
   *          the pool identifier
   * @param factory
   *          the lease factory
   * @param min
   *          number of leases initially created
   * @param keep
   *          number of leases to keep
   * @param max
   *          maximum number of leases
   * @param step
   *          number of leases to create at once
   */
  public Pool(String id, LeaseFactory<T> factory, int min, int keep, int max,
      int step) {
    if (min < 0)
      throw new IllegalArgumentException("Minimum pool size must be >= 0!");
    if (max < 0)
      throw new IllegalArgumentException("Maximum pool size must be >= 0!");
    if (keep < 0)
      throw new IllegalArgumentException("Number of leases to keep must be >= 0!");
    if (step < 1)
      throw new IllegalArgumentException("Step size must be >= 1!");
    id_ = id;
    factory_ = factory;
    min_ = min;
    keep_ = keep;
    max_ = max;
    cleanOnReturn_ = false;
    step_ = step;
    isClosed_ = false;
    frequency_ = DEFAULT_CLEANUP_FREQUENCY;

    pool_ = new Stack<T>();
    leases_ = 0;
    listeners = new ArrayList<PoolListener>();
    cleaner_ = new PoolCleanupThread(this);
  }

  /**
   * Creates a new pool with the given parameters and a default step size of
   * <code>DEFAULT_STEP_SIZE</code>.
   * 
   * @param id
   *          the pool identifier
   * @param factory
   *          the lease factory
   * @param min
   *          number of leases initially created
   * @param keep
   *          number of leases to keep
   * @param max
   *          maximum number of leases
   */
  public Pool(String id, LeaseFactory<T> factory, int min, int keep, int max) {
    this(id, factory, min, keep, max, DEFAULT_STEP_SIZE);
  }

  /**
   * Creates a new pool with the given parameters a maximum number of renderers
   * of <code>Integer.MAX_VALUE</code> and a default step size of
   * <code>DEFAULT_STEP_SIZE</code>.
   * 
   * @param id
   *          the pool identifier
   * @param factory
   *          the lease factory
   * @param min
   *          number of leases initially created
   * @param keep
   *          number of leases to keep
   */
  public Pool(String id, LeaseFactory<T> factory, int min, int keep) {
    this(id, factory, min, keep, MAX_LEASES, DEFAULT_STEP_SIZE);
  }

  /**
   * Creates a new pool with the default sizes for minimum, maximum, number of
   * leases to keep and step size.
   * 
   * @param id
   *          the pool identifier
   * @param factory
   *          the lease factory
   */
  public Pool(String id, LeaseFactory<T> factory) {
    this(id, factory, MIN_LEASES, KEEP_LEASES, MAX_LEASES, DEFAULT_STEP_SIZE);
  }

  /**
   * Closes this pool and stops the pool cleanup thread.
   * <p>
   * <b>Note:</b> After calling this method, no leases may be requested from
   * this pool, since the respective call will result in an
   * <code>IllegalStateException</code>.
   */
  public void close() {
    isClosed_ = true;
    cleaner_.stop();
    synchronized (this) {
      Iterator li = pool_.iterator();
      while (li.hasNext()) {
        ((Lease) li.next()).retired();
      }
    }
  }

  /**
   * Returns a lease from the pool or <code>null</code> if the maximum number of
   * leases has been reached.
   * 
   * @return a lease
   */
  public T getLease() {
    if (isClosed_) {
      throw new IllegalStateException("This pool has already been closed!");
    }
    T lease = null;
    synchronized (this) {
      if (!pool_.isEmpty()) {
        lease = pool_.pop();
      } else if (leases_ < max_) {
        createLease(step_);
        lease = pool_.pop();
      }
      if (lease != null) {
        leases_++;
        if (leases_ > leasedMax_) {
          leasedMax_ = leases_;
          log_.info("New lease maximum reached for pool '" + id_ + "'");
        }
      }
    }
    return lease;
  }

  /**
   * Returns a lease to the pool. If the lease is not marked as
   * <code>dispose</code> then the lease is put back to the pool and is
   * available for the next lease. Otherwise, the <code>LeaseFactory</code> is
   * asked to dispose the lease.
   * 
   * @param lease
   *          the returned lease
   */
  public void returnLease(T lease) {
    if (lease == null)
      return;
    long time = System.currentTimeMillis() - lastReconcilation_;
    boolean disposed = false;
    synchronized (this) {
      if (leases_ == 0) {
        log_.warn("Inconsistency in pool '" + this + "': leases are being returned that were never leased!");
        return;
      }
      leases_--;
      if (!lease.dispose() && (!cleanOnReturn_ || (pool_.size() <= keep_ || time < reconcileTime_))) {
        lease.returned();
        pool_.push(lease);
      } else {
        factory_.disposeLease(lease);
        lease.retired();
        disposed = true;
        if (time > reconcileTime_) {
          lastReconcilation_ = System.currentTimeMillis();
        }
      }
    }
    if (disposed) {
      log_.debug("Pool '" + id_ + "' disposed lease " + leases() + 1);
      firePoolsizeChanged(-1);
    }
  }

  /**
   * If set to <code>true</code> then the pool checks on most incoming leases
   * whether the poolsize is currently larger the preferred size.
   *<p>
   * This check is not performed on every lease that is being returned but
   * every 5 seconds. This is to avoid poolsize thrashing.
   * <p>
   * Regardless of this parameter, there is a thread running every now and then
   * to adjust the poolsize as needed.
   * 
   * @param clean
   *          <code>true</code> to turn cleaning on
   */
  public void setCleanOnReturn(boolean clean) {
    cleanOnReturn_ = clean;
  }

  /**
   * Sets the frequency of the cleanup thread which is run to decrease the pool
   * size, in case the number of leases is bigger than the preferred pool size.
   * <p>
   * <b>Note:<b> The parameter <code>seconds</code> has to be larger than zero.
   * 
   * @param seconds
   *          frequency in seconds
   * @see #getCleanupFrequency()
   */
  public void setCleanupFrequency(long seconds) {
    Arguments.checkValue(seconds > 0, "seconds");
    frequency_ = seconds;
  }

  /**
   * Returns the frequency of the cleanup thread in seconds.
   * 
   * @return the frequency in seconds
   * @see #setCleanupFrequency(long)
   */
  public long getCleanupFrequency() {
    return frequency_;
  }

  /**
   * Returns <code>true</code> if poolsize should be checked when leases are
   * being returned.
   * 
   * @return <code>true</code> if poolsize is being checked
   */
  public boolean getCleanOnReturn() {
    return cleanOnReturn_;
  }

  /**
   * Returns the number of leases that are currently being pooled and are
   * therefore available and ready to be leased.
   * 
   * @return the number of pooled leases
   */
  public int pooled() {
    return pool_.size();
  }

  /**
   * Returns the number of leases that are currently being leased and therefore
   * are not available.
   * 
   * @return the number of leased leases
   */
  public int leased() {
    return leases_;
  }

  /**
   * Returns the number of leases that are initially being pooled.
   * 
   * @return the minimum
   */
  public int getMinimum() {
    return min_;
  }

  /**
   * Returns the number of leases that are preferably been kept.
   * 
   * @return the preferred pool size
   */
  public int getPreferredSize() {
    return keep_;
  }

  /**
   * Sets the number of leases that are created in case that one new lease is
   * needed.
   * <p>
   * <b>Note:<b> The parameter <code>seconds</code> has to be larger than zero.
   * 
   * @param step
   *          the number of leases to create at once
   */
  public void setStep(int step) {
    Arguments.checkValue(step > 0, "step");
    step_ = step;
  }

  /**
   * Returns the number of leases that are created in case that one new lease is
   * needed.
   * 
   * @return the number of leases to create at once
   */
  public int getStep() {
    return step_;
  }

  /**
   * Returns the maximum number of leases that are available through this pool.
   * 
   * @return the maximum number of leases
   */
  public int getMaximum() {
    return max_;
  }

  /**
   * Sets the maximum number of leases that are available through this pool.
   * 
   * @param maximum
   *          the maximum number of leases
   */
  public void setMaximum(int maximum) {
    max_ = maximum;
  }

  /**
   * Returns the maximum number of leases that have ever been leased from this
   * pool..
   * 
   * @return the pooled minimum
   */
  public int getLeasedMaximum() {
    return leasedMax_;
  }

  /**
   * Returns the number of available leases.
   * 
   * @return the number of leases
   */
  public int leases() {
    return leases_ + pool_.size();
  }

  /**
   * Adds <code>listener</code> to the list of pool listeners.
   * 
   * @param listener
   *          the pool listener
   */
  public void addPoolListener(PoolListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes <code>listener</code> from the list of pool listeners.
   * 
   * @param listener
   *          the pool listener
   */
  public void removePoolListener(PoolListener listener) {
    listeners.remove(listener);
  }

  /**
   * Returns the pool identifier.
   * 
   * @return the pool identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return id_;
  }

  /**
   * This method is run at finalization time and will stop the cleanup thread.
   * 
   * @see java.lang.Object#finalize()
   */
  protected void finalize() {
    if (cleaner_ != null)
      cleaner_.stop();
  }

  /**
   * This method notifies the listeners of a poolsize change.
   * 
   * @param amount
   *          increase or decrease
   */
  protected void firePoolsizeChanged(int amount) {
    int size = pool_.size() + leases_;
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).poolSizeChanged(this, amount, size);
    }
  }

  /**
   * Sets up the pool by creating the minum leases.
   */
  public void setup() {
    createLease(min_);
  }

  /**
   * Creates a new lease and puts it into the pool.
   * 
   * @param count
   *          the number of leases to create
   */
  private void createLease(int count) {
    for (int i = 0; i < count; i++) {
      T lease = factory_.createLease();
      if (lease != null) {
        pool_.push(lease);
        log_.debug("Pool '" + id_ + "' created lease " + leases());
      }
    }
    firePoolsizeChanged(step_);
  }

  /**
   * The <code>PoolCleanupThread</code> is used to periodically cleanup the
   * renderer pools. Cleanup in this sense means reducing the number of pooled
   * renderer instances to a minimum of <code>RendererPool.MIN</code> instances
   * per method.
   * 
   * @author Tobias Wunden
   * @version 1.0
   * @since Weblounge 2.0
   */
  private class PoolCleanupThread {

    /** The pool to cleanup */
    Pool<T> pool_;

    /** True if the thread should continue its work */
    boolean continue_;

    /**
     * Creates a new thread that will periodically call the cleanup method.
     */
    PoolCleanupThread(Pool<T> pool) {
      pool_ = pool;
      continue_ = true;
      Thread cleanupWorker = new Thread() {
        public void run() {
          try {
            while (continue_) {
              Thread.sleep(1000 * frequency_);
              cleanup();
            }
          } catch (InterruptedException e) {
          }
          log_.debug("Cleanup thread for pool '" + pool_ + "' is stopped.");
        }
      };
      cleanupWorker.start();
    }

    /**
     * Does the cleanup on all registered pools by removing an instance from
     * those pools that have more than the minimum number of renderers pooled.
     */
    public void cleanup() {
      synchronized (pool_) {
        log_.debug("Current poolsize for '" + pool_ + "' is " + pool_.pool_.size());
        if (pool_.pool_.size() > pool_.getPreferredSize() && !pool_.pool_.isEmpty()) {
          Lease lease = pool_.pool_.pop();
          lease.retired();
          firePoolsizeChanged(-1);
        }
      }
    }

    /**
     * Stops the cleanup thread.
     */
    void stop() {
      continue_ = false;
    }

  }

}