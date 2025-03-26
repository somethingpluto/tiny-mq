package org.tiny.mq.timewheel;


import org.junit.Before;
import org.junit.Test;
import org.tiny.mq.common.dto.MessageRetryDTO;
import org.tiny.mq.common.event.EventBus;

import java.util.concurrent.CountDownLatch;

public class TestTimeWheel {

    private TimeWheelModelManager timeWheelModelManager = new TimeWheelModelManager();
    private TimeWheelModel timeWheelModel = new TimeWheelModel();

    @Before
    public void init() {
        timeWheelModelManager.init(new EventBus("time-wheel-event-bus"));
        timeWheelModelManager.doScanTask();
    }

    @Test
    public void testDelayJob() throws InterruptedException {
        //任务处理中心（submit一个任务，设定延迟多久之后执行）
        DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
        delayMessageDTO.setDelay(62);
        delayMessageDTO.setSlotStoreTypeEnum(SlotStoreTypeEnum.MESSAGE_RETRY_DTO);
        MessageRetryDTO messageRetryDTO = new MessageRetryDTO();
        messageRetryDTO.setNextRetryTime(System.currentTimeMillis());
        delayMessageDTO.setData(messageRetryDTO);
        timeWheelModelManager.add(delayMessageDTO);
        CountDownLatch count = new CountDownLatch(1);
        count.await();
    }


//    @Test
//    public void countNextSlot1() {
//        System.out.println(timeWheelModel.countNextSlot(10));
//        System.out.println(timeWheelModel.countNextSlot(20));
//        System.out.println(timeWheelModel.countNextSlot(59));
//        System.out.println(timeWheelModel.countNextSlot(60));
//    }
//
//    @Test
//    public void countNextSlot2() {
//        timeWheelModel.setCurrent(1);
//        System.out.println(timeWheelModel.countNextSlot(10));
//        System.out.println(timeWheelModel.countNextSlot(20));
//        System.out.println(timeWheelModel.countNextSlot(59));
////        System.out.println(timeWheelModel.countNextSlot(60));
//    }

    private TimeWheelSlotListModel[] buildTimeWheelSlotListModel(int count) {
        TimeWheelSlotListModel[] timeWheelSlotListModels = new TimeWheelSlotListModel[count];
        for (int i = 0; i < count; i++) {
            timeWheelSlotListModels[i] = new TimeWheelSlotListModel();
        }
        return timeWheelSlotListModels;
    }
}
