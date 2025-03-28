package org.tiny.mq.timewheel;


/**
 * 时间轮槽模型
 */
public class TimeWheelSlotModel {
    /**
     * 延迟时间
     */
    private int delaySeconds;
    /**
     * 数据
     */
    private Object data;
    /**
     * 存储类型
     */
    private Class storeType;


    public TimeWheelSlotModel(int delaySeconds, Object data, Class storeType) {
        this.delaySeconds = delaySeconds;
        this.data = data;
        this.storeType = storeType;
    }

    public TimeWheelSlotModel() {
    }


    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Class getStoreType() {
        return storeType;
    }

    public void setStoreType(Class storeType) {
        this.storeType = storeType;
    }

    public String toString() {
        return "TimeWheelSlotModel{" + "delaySeconds=" + delaySeconds + ", data=" + data + ", storeType=" + storeType + '}';
    }
}
