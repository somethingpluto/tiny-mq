package org.tiny.mq.rebalance.strategy;


import org.tiny.mq.rebalance.ConsumerInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ReBalanceInfo {

    private Map<String, List<ConsumerInstance>> consumeInstanceMap;
    //消费者发生变化的消费组
    private Map<String, Set<String>> changeConsumerGroupMap = new HashMap<>();

    public Map<String, List<ConsumerInstance>> getConsumeInstanceMap() {
        return consumeInstanceMap;
    }

    public void setConsumeInstanceMap(Map<String, List<ConsumerInstance>> consumeInstanceMap) {
        this.consumeInstanceMap = consumeInstanceMap;
    }

    public Map<String, Set<String>> getChangeConsumerGroupMap() {
        return changeConsumerGroupMap;
    }

    public void setChangeConsumerGroupMap(Map<String, Set<String>> changeConsumerGroupMap) {
        this.changeConsumerGroupMap = changeConsumerGroupMap;
    }
}
