package com.rnxmsg;

import org.json.JSONObject;
import java.util.concurrent.*;

public class ResponseWaiter {
    private static final ConcurrentHashMap<String, Waiter> waiters = new ConcurrentHashMap<>();

    public static JSONObject waitFor(String action, long timeoutMillis) throws Exception {
        Waiter waiter = new Waiter();
        waiters.put(action, waiter);
        boolean ok = waiter.latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        waiters.remove(action);
        if (!ok) throw new TimeoutException("Timeout waiting for action: " + action);
        return waiter.response;
    }

    public static void complete(String action, JSONObject response) {
        Waiter waiter = waiters.get(action);
        if (waiter != null) {
            waiter.response = response;
            waiter.latch.countDown();
        }
    }

    private static class Waiter {
        CountDownLatch latch = new CountDownLatch(1);
        JSONObject response;
    }
}