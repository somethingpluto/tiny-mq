package org.tiny.mq.async.model;

import org.tiny.mq.common.event.model.Event;

public class BrokerConnectionClosedEvent extends Event {
    private String brokerReqId;

    public String getBrokerReqId() {
        return brokerReqId;
    }

    public void setBrokerReqId(String brokerReqId) {
        this.brokerReqId = brokerReqId;
    }
}
