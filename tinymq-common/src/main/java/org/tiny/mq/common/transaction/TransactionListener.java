package org.tiny.mq.common.transaction;

import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.enums.LocalTransactionState;

public interface TransactionListener {
    /**
     * 执行本地事务函数
     *
     * @param messageDTO
     * @return
     */
    LocalTransactionState executeLocalTransaction(final MessageDTO messageDTO);
}
