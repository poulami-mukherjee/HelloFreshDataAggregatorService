package com.hellofresh.service.util;

import com.hellofresh.service.model.EventData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
public class EventDataStore {

    private final ConcurrentHashMap<Long, List<EventData>> dataMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private final ReentrantLock lock = new ReentrantLock();
    public void store(EventData data) {
        lock.lock();
        try {
            dataMap.computeIfAbsent(data.timestamp(), k -> new ArrayList<>()).add(data);
        } finally {
            lock.unlock();
        }
        executorService.schedule(() -> {
            lock.lock();
            try {
                dataMap.remove(data.timestamp());
            } finally {
                lock.unlock();
            }
        }, 60000, TimeUnit.MILLISECONDS);
    }

    public List<EventData> getDataFromLast60Seconds() {
        lock.lock();
        try {
            List<EventData> allEventData = new ArrayList<>();
            for (List<EventData> values : dataMap.values()) {
                allEventData.addAll(values);
            }
            return allEventData;
        } finally {
            lock.unlock();
        }
    }

}
