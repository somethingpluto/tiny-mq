package org.tiny.mq.event.model;

import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.timewheel.TimeWheelSlotModel;

import java.util.List;

public class TimeWheelEvent extends Event {
    private List<TimeWheelSlotModel> timeWheelSlotModelList;

    public List<TimeWheelSlotModel> getTimeWheelSlotModelList() {
        return timeWheelSlotModelList;
    }

    public void setTimeWheelSlotModelList(List<TimeWheelSlotModel> timeWheelSlotModelList) {
        this.timeWheelSlotModelList = timeWheelSlotModelList;
    }
}
