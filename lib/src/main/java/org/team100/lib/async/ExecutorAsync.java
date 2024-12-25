package org.team100.lib.async;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.team100.lib.util.Util;

/**
 * Runs low-priority, timing-insensitive stuff asynchronously.
 * 
 * Instead of a WPILib Notifier, this uses a java executor with a single thread,
 * in order to set the thread priority really low and avoid the notifier interrupt.
 */

public class ExecutorAsync implements Async {
    private final ScheduledExecutorService m_scheduler;

    /** Run in t sec and every t sec thereafter. */
    @Override
    public void addPeriodic(Runnable runnable, double periodS, String name) {
        long periodMS = (long) (periodS * 1000);
        m_scheduler.scheduleAtFixedRate(
                new CrashWrapper(runnable), periodMS, periodMS, TimeUnit.MILLISECONDS);
    }

    ExecutorAsync() {
        m_scheduler = Executors.newSingleThreadScheduledExecutor(
                new MinPriorityThreads());
    }

    private static class MinPriorityThreads implements ThreadFactory {
        private final AtomicInteger id;

        private MinPriorityThreads() {
            id = new AtomicInteger();
        }

        /**
         * Should only ever be called once but just in case, there's an incrementing
         * id so we can tell what's happening.
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(1);
            thread.setDaemon(true);
            thread.setName("Async Thread " + id.getAndIncrement());
            return thread;
        }

    }

    private static class CrashWrapper implements Runnable {
        private final Runnable m_runnable;

        private CrashWrapper(Runnable runnable) {
            m_runnable = runnable;
        }

        @Override
        public void run() {
            try {
                m_runnable.run();
            } catch (Throwable e) {
                Util.warn(e.toString());
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                Util.warn(writer.toString());
            }
        }
    }
}