package org.tiny.mq.timewheel;

import org.tiny.mq.common.dto.MessageRetryDTO;

/**
 * 时间轮槽里面数据类型枚举
 */
public enum SlotStoreTypeEnum {

    MESSAGE_RETRY_DTO(MessageRetryDTO.class),
    ;

    Class clazz;

    public Class getClazz() {
        return clazz;
    }

    SlotStoreTypeEnum(Class clazz) {
        this.clazz = clazz;
    }
}
