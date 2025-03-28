package org.tiny.mq.event.spi.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.dto.ConsumeMsgCommitLogDTO;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.MessageRetryDTO;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.core.CommitLogMMapFileModel;
import org.tiny.mq.event.model.TimeWheelEvent;
import org.tiny.mq.timewheel.SlotStoreTypeEnum;
import org.tiny.mq.timewheel.TimeWheelSlotModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TimeWheelListener implements Listener<TimeWheelEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TimeWheelListener.class);

    @Override
    public void onReceive(TimeWheelEvent event) throws Exception {
        List<TimeWheelSlotModel> timeWheelSlotModelList = event.getTimeWheelSlotModelList();
        if (timeWheelSlotModelList != null && !timeWheelSlotModelList.isEmpty()) {
            logger.error("timeWheelSlotModelList is empty");
            return;
        }
        for (TimeWheelSlotModel timeWheelSlotModel : timeWheelSlotModelList) {
            if (SlotStoreTypeEnum.MESSAGE_RETRY_DTO.getClazz().equals(timeWheelSlotModel.getStoreType())) {
                MessageRetryDTO messageRetryDTO = (MessageRetryDTO) timeWheelSlotModel.getData();
                this.messageRetryHandler(messageRetryDTO);
            } else if (SlotStoreTypeEnum.DELAY_MESSAGE_DTO.getClazz().equals(timeWheelSlotModel.getStoreType())) {
                MessageDTO messageDTO = (MessageDTO) timeWheelSlotModel.getData();
                CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO);
            }
        }
    }

    private void messageRetryHandler(MessageRetryDTO messageRetryDTO) {
        CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(messageRetryDTO.getTopic());
        ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(messageRetryDTO.getSourceCommitLogOffset(), (int) messageRetryDTO.getSourceCommitLogSize());
        byte[] body = consumeMsgCommitLogDTO.getBody();
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setBody(body);
        messageDTO.setTopic("retry_" + messageRetryDTO.getTopic());
        messageDTO.setQueueId(ThreadLocalRandom.current().nextInt());
        try {
            CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
