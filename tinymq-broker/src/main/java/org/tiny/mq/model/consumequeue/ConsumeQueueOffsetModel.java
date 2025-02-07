package org.tiny.mq.model.consumequeue;


import java.util.HashMap;
import java.util.Map;

public class ConsumeQueueOffsetModel {

    private Map<String, ConsumeGroupInfo> offsetTable = new HashMap<>();

    public Map<String, ConsumeGroupInfo> getOffsetTable() {
        return offsetTable;
    }

    public void setOffsetTable(Map<String, ConsumeGroupInfo> offsetTable) {
        this.offsetTable = offsetTable;
    }

    public static class ConsumeGroupInfo {
        private Map<String, Map<String, String>> consumeGroup = new HashMap<>();

        public Map<String, Map<String, String>> getConsumeGroup() {
            return consumeGroup;
        }

        public void setConsumeGroup(Map<String, Map<String, String>> consumeGroup) {
            this.consumeGroup = consumeGroup;
        }
    }
}
