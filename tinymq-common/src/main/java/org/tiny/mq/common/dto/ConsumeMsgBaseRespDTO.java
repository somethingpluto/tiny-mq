package org.tiny.mq.common.dto;

import java.util.List;

public class ConsumeMsgBaseRespDTO extends BaseBrokerRemoteDTO {

    private List<ConsumeMsgRespDTO> consumeMsgRespDTOList;

    public List<ConsumeMsgRespDTO> getConsumeMsgRespDTOList() {
        return consumeMsgRespDTOList;
    }

    public void setConsumeMsgRespDTOList(List<ConsumeMsgRespDTO> consumeMsgRespDTOList) {
        this.consumeMsgRespDTOList = consumeMsgRespDTOList;
    }
}
