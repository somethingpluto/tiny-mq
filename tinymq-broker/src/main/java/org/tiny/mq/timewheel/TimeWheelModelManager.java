package org.tiny.mq.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.event.EventBus;

import java.util.concurrent.locks.ReentrantLock;

public class TimeWheelModelManager {
    private static final Logger logger = LoggerFactory.getLogger(TimeWheelModelManager.class);
    private static final ReentrantLock secondsLock = new ReentrantLock();
    private static final ReentrantLock minutesLock = new ReentrantLock();
    private long executeSeconds = 0;
    private TimeWheelModel secondsTimeWheelModel;
    private TimeWheelModel minutesTimeWheelModel;
    private EventBus eventBus;

    public void init(EventBus eventBus) {
        secondsTimeWheelModel = new TimeWheelModel();
        secondsTimeWheelModel.setCurrent(0);
        secondsTimeWheelModel.setTimeWheelSlotListModels(buildTimeWheelSlotListModel(60));
        secondsTimeWheelModel.setUnit(TimeWheelSlotStepUnitEnum.SECOND.getDesc());

        minutesTimeWheelModel = new TimeWheelModel();
        minutesTimeWheelModel.setCurrent(0);
        minutesTimeWheelModel.setTimeWheelSlotListModels(buildTimeWheelSlotListModel(60));
        minutesTimeWheelModel.setUnit(TimeWheelSlotStepUnitEnum.MINUTE.getDesc());
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    public void add(DelayMessageDTO delayMessageDTO) {
        int delay = delayMessageDTO.getDelay();
        int min = delay / 60;
        if (min == 0) { // 走秒的时间轮
            try {
                secondsLock.lock();
                int nextSlot = secondsTimeWheelModel.countNextSlot(delay);
                logger.info("current second slot:{} next slot:{}", secondsTimeWheelModel.getCurrent(), nextSlot);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                secondsLock.unlock();
            }
        }
    }

    private TimeWheelSlotListModel[] buildTimeWheelSlotListModel(int count) {
        TimeWheelSlotListModel[] timeWheelSlotListModels = new TimeWheelSlotListModel[count];
        for (int i = 0; i < count; i++) {
            timeWheelSlotListModels[i] = new TimeWheelSlotListModel();
        }
        return timeWheelSlotListModels;
    }
}
