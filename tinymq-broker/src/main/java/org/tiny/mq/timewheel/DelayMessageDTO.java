package org.tiny.mq.timewheel;

public class DelayMessageDTO {
    private Object data;
    private SlotStoreTypeEnum slotStoreTypeEnum;
    private int delay;

    private long nextExecuteTime;

    public long getNextExecuteTime() {
        return nextExecuteTime;
    }


    public void setNextExecuteTime(long nextExecuteTime) {
        this.nextExecuteTime = nextExecuteTime;
    }

    public DelayMessageDTO(Object data, SlotStoreTypeEnum slotStoreTypeEnum, int delay) {
        this.data = data;
        this.slotStoreTypeEnum = slotStoreTypeEnum;
        this.delay = delay;
    }

    public DelayMessageDTO(Object data, SlotStoreTypeEnum slotStoreTypeEnum, int delay, long nextExecuteTime) {
        this.data = data;
        this.slotStoreTypeEnum = slotStoreTypeEnum;
        this.delay = delay;
        this.nextExecuteTime = nextExecuteTime;
    }


    public DelayMessageDTO() {
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public SlotStoreTypeEnum getSlotStoreTypeEnum() {
        return slotStoreTypeEnum;
    }

    public void setSlotStoreTypeEnum(SlotStoreTypeEnum slotStoreTypeEnum) {
        this.slotStoreTypeEnum = slotStoreTypeEnum;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
