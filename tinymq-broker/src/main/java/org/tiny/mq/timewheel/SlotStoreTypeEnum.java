package org.tiny.mq.timewheel;

import org.tiny.mq.common.dto.MessageRetryDTO;
import org.tiny.mq.model.TransactionMsgAckModel;

/**
 * 时间轮槽里面数据类型枚举
 */
public enum SlotStoreTypeEnum {

    MESSAGE_RETRY_DTO(MessageRetryDTO.class), DELAY_MESSAGE_DTO(DelayMessageDTO.class),
    TRANSACTION_MSG_DTO(TransactionMsgAckModel.class);

    Class clazz;

    public Class getClazz() {
        return clazz;
    }

    SlotStoreTypeEnum(Class clazz) {
        this.clazz = clazz;
    }
}
