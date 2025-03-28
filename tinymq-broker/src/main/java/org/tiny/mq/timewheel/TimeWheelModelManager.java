package org.tiny.mq.timewheel;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.event.model.TimeWheelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TimeWheelModelManager {
    private static final Logger logger = LoggerFactory.getLogger(TimeWheelModelManager.class);
    /**
     * 秒级时间轮的锁
     */
    private static final ReentrantLock secondsLock = new ReentrantLock();
    /**
     * 分钟级时间轮的锁
     */
    private static final ReentrantLock minutesLock = new ReentrantLock();
    /**
     * 时间轮已经执行的时间
     */
    private long executeSeconds = 0;
    /**
     * 秒级时间轮
     */
    private TimeWheelModel secondsTimeWheelModel;
    /**
     * 分钟级时间轮
     */
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

    private void doSecondsTimeWheelExecute() {
        try {
            secondsLock.lock();
            int current = secondsTimeWheelModel.getCurrent();
            logger.info("second time wheel do scan slot:{}", current);
            TimeWheelSlotListModel timeWheelSlotListModel = secondsTimeWheelModel.getTimeWheelSlotListModels()[current];
            List<TimeWheelSlotModel> timeWheelSlotModelList = timeWheelSlotListModel.getTimeWheelSlotModelList();
            if (CollectionUtils.isNotEmpty(timeWheelSlotModelList)) {
                TimeWheelEvent timeWheelEvent = new TimeWheelEvent();
                timeWheelEvent.setTimeWheelSlotModelList(timeWheelSlotModelList);
                logger.info("second scan slot:{}  data:{}", current, timeWheelSlotModelList);
                eventBus.publish(timeWheelEvent);
            }
            // 将要执行的任务发生出去后
            timeWheelSlotListModel.setTimeWheelSlotModelList(new ArrayList<>());
            if (current == secondsTimeWheelModel.getTimeWheelSlotListModels().length - 1) { // 刚好走到最后的位置
                current = 0;
            } else {
                current++;
            }
            secondsTimeWheelModel.setCurrent(current);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            secondsLock.unlock();
        }
    }

    private void doMinutesTimeWheelExecute() {
        try {
            minutesLock.lock();
            int current = minutesTimeWheelModel.getCurrent();
            logger.info("minute time wheel do scan slot:{}", current);
            TimeWheelSlotListModel timeWheelSlotListModel = minutesTimeWheelModel.getTimeWheelSlotListModels()[current];
            List<TimeWheelSlotModel> timeWheelSlotModelList = timeWheelSlotListModel.getTimeWheelSlotModelList();
            List<TimeWheelSlotModel> needHandleList = new ArrayList<>();
            for (TimeWheelSlotModel timeWheelSlotModel : timeWheelSlotModelList) {
                int remainSecond = timeWheelSlotModel.getDelaySeconds() % 60;
                if (remainSecond > 0) {
                    DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
                    delayMessageDTO.setData(timeWheelSlotModel.getData());
                    delayMessageDTO.setDelay(remainSecond);
                    delayMessageDTO.setSlotStoreTypeEnum(SlotStoreTypeEnum.MESSAGE_RETRY_DTO);
                    add(delayMessageDTO);
                } else { // 这个slot需要处理的
                    needHandleList.add(timeWheelSlotModel);
                }
            }
            // 将所有要处理的发送出去
            if (CollectionUtils.isNotEmpty(needHandleList)) {
                TimeWheelEvent timeWheelEvent = new TimeWheelEvent();
                timeWheelEvent.setTimeWheelSlotModelList(timeWheelSlotModelList);
                logger.info("minute scan slot:{}  data:{}", current, timeWheelSlotModelList);
                eventBus.publish(timeWheelEvent);
            }
            timeWheelSlotListModel.setTimeWheelSlotModelList(new ArrayList<>());
            if (current == minutesTimeWheelModel.getTimeWheelSlotListModels().length - 1) {
                current = 0;
            } else {
                current = current + 1;
            }
            minutesTimeWheelModel.setCurrent(current);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            minutesLock.unlock();
        }
    }

    public void add(DelayMessageDTO delayMessageDTO) {
        int delay = delayMessageDTO.getDelay();
        int min = delay / 60;
        if (min == 0) { // 走秒的时间轮
            try {
                secondsLock.lock();
                int nextSlot = secondsTimeWheelModel.countNextSlot(delay);
                logger.info("current second slot:{} next slot:{}", secondsTimeWheelModel.getCurrent(), nextSlot);
                TimeWheelSlotListModel timeWheelSlotListModel = secondsTimeWheelModel.getTimeWheelSlotListModels()[nextSlot];
                List<TimeWheelSlotModel> timeWheelSlotModelList = timeWheelSlotListModel.getTimeWheelSlotModelList();
                TimeWheelSlotModel timeWheelSlotModel = new TimeWheelSlotModel(delay, delayMessageDTO, delayMessageDTO.getSlotStoreTypeEnum().getClazz());
                timeWheelSlotModelList.add(timeWheelSlotModel);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                secondsLock.unlock();
            }
        } else if (min > 0) {
            try {
                minutesLock.lock();
                int nextSlot = minutesTimeWheelModel.countNextSlot(min);
                logger.info("current minutes slot:{},next slot:{}", minutesTimeWheelModel.getCurrent(), nextSlot);
                TimeWheelSlotListModel timeWheelSlotListModel = minutesTimeWheelModel.getTimeWheelSlotListModels()[nextSlot];
                List<TimeWheelSlotModel> timeWheelSlotModelList = timeWheelSlotListModel.getTimeWheelSlotModelList();
                TimeWheelSlotModel timeWheelSlotModel = new TimeWheelSlotModel(delay, delayMessageDTO, delayMessageDTO.getSlotStoreTypeEnum().getClazz());
                timeWheelSlotModelList.add(timeWheelSlotModel);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                minutesLock.unlock();
            }
        }
    }

    public void doScanTask() {
        Thread scanThread = new Thread(() -> {
            logger.info("start scan slot task");
            while (true) {
                try {
                    doSecondsTimeWheelExecute();
                    if (executeSeconds % 60 == 0) {
                        doMinutesTimeWheelExecute();
                    }
                    TimeUnit.SECONDS.sleep(1);
                    executeSeconds++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        scanThread.setName("scan-slot-task");
        scanThread.start();
    }

    private TimeWheelSlotListModel[] buildTimeWheelSlotListModel(int count) {
        TimeWheelSlotListModel[] timeWheelSlotListModels = new TimeWheelSlotListModel[count];
        for (int i = 0; i < count; i++) {
            timeWheelSlotListModels[i] = new TimeWheelSlotListModel();
        }
        return timeWheelSlotListModels;
    }
}
