package org.tiny.mq.timewheel;

public enum TimeWheelSlotStepUnitEnum {

    SECOND("second"), MINUTE("minute"), HOUR("hour"), DAY("day");

    TimeWheelSlotStepUnitEnum(String desc) {
        this.desc = desc;
    }

    ;

    public String getDesc() {
        return desc;
    }

    private String desc;
}
