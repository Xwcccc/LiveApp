package com.zsxy.utils;

public interface IRedisLock {
    /**
     * 获取分布式锁
     * @return
     */
    Boolean getLock(long timeout);

    /**
     * 业务流程正常结束后释放锁
     * @return
     */
    void unlock();
}
