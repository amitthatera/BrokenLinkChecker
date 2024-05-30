package com.rchilli.assignment.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class CrawlReport {

    private final Map<String, Integer> brokenLinks;
    private final AtomicInteger totalAnchorsTags;
    private final AtomicInteger totalVisitedLinks;
    private final AtomicInteger totalErrorsLinks;

    public CrawlReport() {
        this.brokenLinks = new ConcurrentHashMap<>();
        this.totalAnchorsTags = new AtomicInteger();
        this.totalVisitedLinks = new AtomicInteger();
        this.totalErrorsLinks = new AtomicInteger();
    }

    public Map<String, Integer> getBrokenLinks() {
        return brokenLinks;
    }

    public int getTotalAnchors() {
        return totalAnchorsTags.get();
    }

    public int getTotalVisited() {
        return totalVisitedLinks.get();
    }

    public int getTotalErrors() {
        return totalErrorsLinks.get();
    }

    public void addBrokenLink(String url, int statusCode) {
        this.brokenLinks.put(url, statusCode);
    }

    public void incrementTotalAnchors() {
        this.totalAnchorsTags.incrementAndGet();
    }

    public void incrementTotalVisited() {
        this.totalVisitedLinks.incrementAndGet();
    }

    public void incrementTotalErrors() {
        this.totalErrorsLinks.incrementAndGet();
    }
}