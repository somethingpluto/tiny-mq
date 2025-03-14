package org.tiny.mq.common.dto;

public class SlaveSyncRespDTO extends BaseBrokerRemoteDTO {
    private boolean syncSuccess;

    public boolean isSyncSuccess() {
        return syncSuccess;
    }

    public void setSyncSuccess(boolean syncSuccess) {
        this.syncSuccess = syncSuccess;
    }
}
