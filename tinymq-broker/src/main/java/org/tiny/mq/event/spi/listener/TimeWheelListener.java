package org.tiny.mq.event.spi.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.dto.MessageRetryDTO;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.TimeWheelEvent;
import org.tiny.mq.timewheel.SlotStoreTypeEnum;
import org.tiny.mq.timewheel.TimeWheelSlotModel;

import java.util.List;

public class TimeWheelListener implements Listener<TimeWheelEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TimeWheelListener.class);

    @Override
    public void onReceive(TimeWheelEvent event) throws Exception {
        List<TimeWheelSlotModel> timeWheelSlotModelList = event.getTimeWheelSlotModelList();
        if (timeWheelSlotModelList != null && timeWheelSlotModelList.size() > 0) {
            logger.error("timeWheelSlotModelList is empty");
            return;
        }
        for (TimeWheelSlotModel timeWheelSlotModel : timeWheelSlotModelList) {
            if (SlotStoreTypeEnum.MESSAGE_RETRY_DTO.getClazz().equals(timeWheelSlotModel.getStoreType())) {
                MessageRetryDTO messageRetryDTO = (MessageRetryDTO) timeWheelSlotModel.getData();
                this.messageRetryHandler(messageRetryDTO);
            }
        }
    }

    private void messageRetryHandler(MessageRetryDTO messageRetryDTO) {

    }
}
