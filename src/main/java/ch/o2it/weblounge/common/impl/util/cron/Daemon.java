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

package ch.o2it.weblounge.common.impl.util.cron;

import ch.o2it.weblounge.common.Lease;
import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.datatype.PriorityQueue;
import ch.o2it.weblounge.common.impl.util.pool.LeaseFactory;
import ch.o2it.weblounge.common.impl.util.pool.Pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Implements a simple scheduler for periodic jobs.
 * 
 * @version $Revision: 1090 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */

public class Daemon implements Runnable {

  /** the only instance of the cron daemon */
  private static final Daemon instance = new Daemon();

  /** the thread that runs the daemon */
  private Thread daemon;

  /** the thread group for the cron subsystem */
  private ThreadGroup group;

  /** all scheduled jobs */
  private Map<Integer, ScheduledJob> jobs = new LinkedHashMap<Integer, ScheduledJob>();

  /** priority queue containing all waiting jobs */
  private PriorityQueue<ScheduledJob> waitingJobs = new PriorityQueue<ScheduledJob>();

  /** running flag for the daemon */
  private boolean keepRunning = false;

  /** the worker thread pools, arranged by thread group */
  private Pool<WorkerThread> threadPool = null;

  /** Logging facility provided by log4j */
  final static Logger log = LoggerFactory.getLogger(Daemon.class.getName());

  /**
   * Creates a new cron daemon and starts it.
   */
  private Daemon() {
    start();
  }

  /**
   * The cron daemon is a singleton. This method returns the only instance of
   * the <code>Daemon</code>.
   * 
   * @return the only instance of the cron daemon
   */
  public static Daemon getInstance() {
    return instance;
  }

  /**
   * Schedules a job for (periodic) execution.
   * 
   * @param job
   *          the job to be executed
   * @return <code>true</code> if the job could be scheduled
   */
  public boolean addJob(AbstractJob job) {
    ScheduledJob sj;
    synchronized (jobs) {
      if (jobs.get(job) != null)
        return false;
      sj = new ScheduledJob(job);
      jobs.put(job.id, sj);
    }
    synchronized (waitingJobs) {
      waitingJobs.insert(sj);
    }
    daemon.interrupt();
    return true;
  }

  /**
   * Method removeJob.
   * 
   * @param job
   * @return <code>true</code> if the job could be removed
   */
  public boolean removeJob(AbstractJob job) {
    return removeJob(job.id);
  }

  /**
   * Method removeJob.
   * 
   * @param id
   * @return <code>true</code> if the job could be removed
   */
  public boolean removeJob(Integer id) {
    ScheduledJob sj;
    synchronized (jobs) {
      sj = jobs.remove(id);
      if (sj == null)
        return false;
    }
    synchronized (sj) {
      sj.status = ScheduledJob.COMPLETED;
      synchronized (waitingJobs) {
        waitingJobs.remove(sj);
      }
      daemon.interrupt();
    }
    return false;
  }

  /**
   * Method activateJob.
   * 
   * @param job
   * @return <code>true</code> if the job could be activated
   */
  public boolean activateJob(AbstractJob job) {
    return activateJob(job.id);
  }

  /**
   * Method activateJob.
   * 
   * @param id
   * @return <code>true</code> if the job could be activated
   */
  public boolean activateJob(Integer id) {
    ScheduledJob sj;
    synchronized (jobs) {
      sj = jobs.get(id);
      if (sj == null)
        return false;
    }
    synchronized (sj) {
      if (sj.status != ScheduledJob.DEACTIVATED)
        return false;
      sj.status = ScheduledJob.WAITING;
      sj.nextRun = sj.job.getNextExecution();
      long cur = System.currentTimeMillis() + 1000;
      if (sj.nextRun < cur)
        sj.nextRun = cur;
      synchronized (waitingJobs) {
        waitingJobs.insert(sj);
      }
      daemon.interrupt();
    }
    return true;
  }

  /**
   * Method deactivateJob.
   * 
   * @param job
   * @return <code>true</code> if the job could be deactivated
   */
  public boolean deactivateJob(AbstractJob job) {
    return deactivateJob(job.id);
  }

  /**
   * Method deactivateJob.
   * 
   * @param id
   * @return <code>true</code> if the job could be deactivated
   */
  public boolean deactivateJob(Integer id) {
    ScheduledJob sj;
    synchronized (jobs) {
      sj = jobs.get(id);
      if (sj == null)
        return false;
    }
    synchronized (sj) {
      if (sj.status != ScheduledJob.WAITING)
        return false;
      sj.status = ScheduledJob.DEACTIVATED;
      synchronized (waitingJobs) {
        waitingJobs.remove(sj);
      }
      daemon.interrupt();
    }
    return true;
  }

  /**
   * Method getJob.
   * 
   * @param job
   * @return the job
   */
  public ScheduledJob getJob(AbstractJob job) {
    return getJob(job.id);
  }

  /**
   * Method getJob.
   * 
   * @param id
   * @return the job
   */
  public ScheduledJob getJob(Integer id) {
    synchronized (jobs) {
      return jobs.get(id);
    }
  }

  /**
   * Returns all scheduled jobs.
   * 
   * @return an iterator containing all scheduled jobs
   */
  public Iterator jobs() {
    return jobs.values().iterator();
  }

  /**
   * Runs a single scheduled job.
   * 
   * @param sj
   *          the job that needs to be executed
   */
  private void runJob(ScheduledJob sj) {
    if (sj == null)
      return;
    synchronized (sj) {
      if (sj.status != ScheduledJob.WAITING)
        /* job has been deactivated or removed */
        return;
      sj.status = ScheduledJob.RUNNING;
      ++sj.runCount;
      sj.lastRun = System.currentTimeMillis();
    }
    WorkerThread t = getWorkerThread(sj.job);
    t.interrupt();
  }

  /**
   * Ends the execution of a job and re-enlists it.
   * 
   * @param job
   *          the job that completed execution
   */
  protected void endJob(AbstractJob job) {
    ScheduledJob sj;
    synchronized (jobs) {
      sj = jobs.get(job.id);
      if (sj == null)
        return;
    }
    synchronized (sj) {
      if (sj.job instanceof PeriodicJob && sj.job.getNextExecution() != AbstractJob.NEVER) {
        if (sj.status == ScheduledJob.RUNNING) {
          sj.status = ScheduledJob.WAITING;
          sj.nextRun = sj.job.getNextExecution();
          synchronized (waitingJobs) {
            waitingJobs.insert(sj);
          }
          daemon.interrupt();
        }
      } else {
        sj.status = ScheduledJob.COMPLETED;
        sj.nextRun = 0;
      }
    }
  }

  /**
   * Returns a <code>WorkerThread</code> instance from the pool.
   * 
   * @param job
   *          the job
   * @return a worker thread
   */
  private WorkerThread getWorkerThread(AbstractJob job) {
    WorkerThread t = threadPool.getLease();
    t.setName("Cron Worker: Job " + job.id + " (" + job.getName() + ")");
    t.job = job;
    return t;
  }

  /**
   * Returns the worker thread to the pool.
   * 
   * @param t
   *          the thread
   */
  protected void returnWorkerThread(WorkerThread t) {
    t.setName("Pooled Cron Wroker");
    threadPool.returnLease(t);
  }

  /**
   * Starts the execution of the cron daemon.
   */
  public void start() {
    if (daemon == null || !daemon.isAlive()) {
      group = new ThreadGroup("Cron System");
      group.setDaemon(true);
      group.setMaxPriority(Thread.NORM_PRIORITY);
      daemon = new Thread(group, this, "Cron Daemon");
      daemon.setDaemon(true);
      keepRunning = true;
      threadPool = new Pool<WorkerThread>("Cron Daemon Thread Pool", new WorkerThreadFactory(group));
      daemon.start();
    }
  }

  /**
   * Stops the execution of the cron daemon.
   */
  public void stop() {
    keepRunning = false;
    if (daemon.isAlive())
      daemon.interrupt();
  }

  /**
   * The main loop of the cron daemon.
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
    long timeout;
    Comparable c;
    ScheduledJob job;
    while (keepRunning) {
      timeout = 0;
      job = null;
      try {
        synchronized (waitingJobs) {
          while (true) {
            c = waitingJobs.getMin();
            if ((c instanceof ScheduledJob)) {
              job = (ScheduledJob) c;
              timeout = job.nextRun - System.currentTimeMillis();
              break;
            }
            waitingJobs.removeMin();
          }
        }
      } catch (NoSuchElementException e) {
        timeout = Long.MAX_VALUE;
      }

      if (timeout > 1000) {
        try {
          Thread.sleep(timeout);
        } catch (InterruptedException e) {
          /* someone interupted us, recheck condions */
          continue;
        }
      }
      synchronized (waitingJobs) {
        waitingJobs.removeMin();
        runJob(job);
      }
    }
  }

  /**
   * Describes a scheduled job.
   * 
   * @version $Revision: 1090 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep
   *          2009) $
   * @author Daniel Steiner
   */
  public static class ScheduledJob implements Comparable {

    public static final int WAITING = 0;
    public static final int RUNNING = 1;
    public static final int DEACTIVATED = 2;
    public static final int COMPLETED = 3;

    public static final String STATUS_NAMES[] = { "waiting", "running", "deacivated", "completed" };

    protected AbstractJob job;

    protected long nextRun = 0;
    protected long lastRun = 0;
    protected int status = WAITING;
    protected int runCount = 0;
    protected long duration = 0;

    protected ScheduledJob(AbstractJob job) {
      if (job == null)
        throw new NullPointerException();
      this.job = job;
      nextRun = job.getNextExecution();
      long cur = System.currentTimeMillis() + 1000;
      if (nextRun < cur && nextRun != AbstractJob.NEVER)
        nextRun = cur;
    }

    public String getLastRun() {
      return (lastRun == 0) ? "never" : WebloungeDateFormat.formatStatic(lastRun);
    }

    public String getNextRun() {
      return (nextRun == 0) ? "never" : WebloungeDateFormat.formatStatic(nextRun);
    }

    public String getRunCount() {
      switch (runCount) {
      case 0:
        return "never";
      case 1:
        return "once";
      case 2:
        return "twice";
      default:
        return runCount + " times";
      }
    }

    public String getStatus() {
      return (status < 0 && status >= STATUS_NAMES.length) ? "unknown" : STATUS_NAMES[status];
    }

    public String getName() {
      String name = job.getName();
      return (name == null) ? "<unnamed>" : name;
    }

    public String getDescription() {
      String desc = job.getDescription();
      return (desc == null) ? "" : desc;
    }

    public String getDuration() {
      if (duration == 0)
        return "-";
      long dur = duration;
      long ms = dur % 1000;
      dur /= 1000;
      long s = dur % 60;
      long m = dur / 60;
      return m + "m " + s + "s " + ms + "ms";
    }

    public int getId() {
      return job.id.intValue();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
      /*
       * Note: the following statement may throw a NullPointerException or a
       * ClassCastException. This is by design.
       */
      long val = nextRun - ((ScheduledJob) o).nextRun;
      return (val < 0) ? -1 : (val == 0) ? 0 : 1;
    }
  }

  /**
   * A worker thread that actually executes schedules jobs.
   * 
   * @version $Revision: 1090 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep
   *          2009) $
   * @author Daniel Steiner
   */
  private static class WorkerThread extends Thread implements Lease {

    /** the schedules job that is executed in this thread */
    protected AbstractJob job;

    /** the daemon that executes the job */
    private Daemon daemon;

    /** true if this lease is part of a pool */
    private boolean poolMember = true;

    /**
     * Creates a new <code>WorkerThread</code>.
     * 
     * @param daemon
     *          the cron daemon
     * @param group
     *          the thread group the worker runs in
     * @param job
     *          the scheduled job to run
     */
    protected WorkerThread(Daemon daemon, ThreadGroup group, AbstractJob job) {
      super(group, "Cron Worker: Job " + job.id + " (" + job.getName() + ")");
      setDaemon(true);
      this.job = job;
      this.daemon = daemon;
    }

    /**
     * Creates a new <code>WorkerThread</code> for the thread pool.
     * 
     * @param daemon
     *          the cron daemon
     * @param group
     *          the thread group the worker runs in
     */
    WorkerThread(Daemon daemon, ThreadGroup group) {
      super(group, "Pooled Cron Worker");
      setDaemon(true);
      this.daemon = daemon;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
      while (poolMember) {
        while (job == null && poolMember) {
          try {
            sleep(Long.MAX_VALUE);
          } catch (InterruptedException e) {
          }
        }
        if (job != null) {
          try {
            job.work();
          } catch (Throwable t) {
            // TODO: Depending on job, mail error to sysadmin and/or site admin
            log.error("Cron job '" + job + "' finished with error: " + t.getMessage());
          } finally {
            daemon.endJob(job);
            daemon.returnWorkerThread(this);
          }
        }
      }
    }

    /**
     * @see ch.o2it.weblounge.common.Lease#leased()
     */
    public void leased() {
    }

    /**
     * @see ch.o2it.weblounge.common.Lease#returned()
     */
    public void returned() {
      job = null;
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
      poolMember = false;
      interrupt();
    }

  }

  /**
   * Creates new worker threads if needed by the thread pool.
   * 
   * @author Tobias Wunden
   */
  private class WorkerThreadFactory implements LeaseFactory<WorkerThread> {

    ThreadGroup group;

    public WorkerThreadFactory(ThreadGroup group) {
      this.group = group;
    }

    /**
     * @see ch.o2it.weblounge.common.impl.util.pool.LeaseFactory#createLease()
     */
    public WorkerThread createLease() {
      WorkerThread t = new WorkerThread(Daemon.this, group);
      t.start();
      return t;
    }

    /**
     * @see ch.o2it.weblounge.common.impl.util.pool.LeaseFactory#disposeLease(ch.o2it.weblounge.common.Lease)
     */
    public void disposeLease(WorkerThread lease) {
      lease = null;
    }

  }

}