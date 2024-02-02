package com.UmiUni.shop.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InventoryLockService {

    private final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    public ReentrantLock getLocalLock(String skuCode) {
        return localLocks.computeIfAbsent(skuCode, k -> new ReentrantLock());
    }

    public void localLock(String skuCode) {
        ReentrantLock lock = getLocalLock(skuCode);
        lock.lock();
    }

    public void localUnlock(String skuCode) {
        ReentrantLock lock = localLocks.get(skuCode);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public boolean globalLock(String skuCode) {
        String lockKey = "lock:product:" + skuCode;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS));
    }

    public void globalUnlock(String skuCode) {
        String lockKey = "lock:product:" + skuCode;
        redisTemplate.delete(lockKey);
    }
}
