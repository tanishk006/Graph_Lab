package com.graphengine.api.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks how many times the API has built/run a graph.
 * In-memory only — counter resets on server restart.
 * If persistence across restarts is ever needed, this is the class
 * to swap over to writing into a database instead of an AtomicLong.
 */
@Service
public class StatsService {

    private final AtomicLong runCount = new AtomicLong(0);

    public long incrementAndGet() {
        return runCount.incrementAndGet();
    }

    public long getRunCount() {
        return runCount.get();
    }
}
