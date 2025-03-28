package org.tiny.mq.timewheel;

import org.tiny.mq.common.utils.AssertUtils;

/**
 * 时间轮模型
 */
public class TimeWheelModel {
    /**
     * 当前时间轮的槽位
     */
    private int current;
    /**
     * 时间轮槽位列表
     */
    private TimeWheelSlotListModel[] timeWheelSlotListModels;
    /**
     * 时间单位
     */
    private String unit;

    public TimeWheelModel(int current, TimeWheelSlotListModel[] timeWheelSlotListModels, String unit) {
        this.current = current;
        this.timeWheelSlotListModels = timeWheelSlotListModels;
        this.unit = unit;
    }

    public TimeWheelModel() {
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public TimeWheelSlotListModel[] getTimeWheelSlotListModels() {
        return timeWheelSlotListModels;
    }

    public void setTimeWheelSlotListModels(TimeWheelSlotListModel[] timeWheelSlotListModels) {
        this.timeWheelSlotListModels = timeWheelSlotListModels;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


    public int countNextSlot(int delay) {
        AssertUtils.isTrue(delay < timeWheelSlotListModels.length, "delay can not large than slot's total count");
        int remainSlotCount = timeWheelSlotListModels.length - current;
        int diff = delay - remainSlotCount;
        if (diff < 0) {
            return current + delay;
        }
        return diff;
    }
}
