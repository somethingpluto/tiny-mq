package org.tiny.mq.timewheel;

import java.util.ArrayList;
import java.util.List;

public class TimeWheelSlotListModel {
    /**
     * 时间槽集合
     */
    private List<TimeWheelSlotModel> timeWheelSlotModelList = new ArrayList<>();

    public List<TimeWheelSlotModel> getTimeWheelSlotModelList() {
        return timeWheelSlotModelList;
    }

    public void setTimeWheelSlotModelList(List<TimeWheelSlotModel> timeWheelSlotModelList) {
        this.timeWheelSlotModelList = timeWheelSlotModelList;
    }
}
