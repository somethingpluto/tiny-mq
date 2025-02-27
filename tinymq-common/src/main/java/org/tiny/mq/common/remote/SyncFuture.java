package org.tiny.mq.common.remote;

import org.tiny.mq.common.cache.NameServerSyncFutureManager;

import java.util.concurrent.*;

public class SyncFuture implements Future {

    // 远程RPC返回的数据内容
    private Object response;
    private String msgId;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return response != null;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        NameServerSyncFutureManager.remove(msgId);
        return response;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        NameServerSyncFutureManager.remove(msgId);
        return response;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
        countDownLatch.countDown();
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
