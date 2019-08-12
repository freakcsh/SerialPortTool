package com.freak.serialporttool.message;

/**
 * 日志消息数据接口
 *
 * @author Freak
 * @date 2019/8/12.
 */

public interface IMessage {
    /**
     * 消息文本
     *
     * @return
     */
    String getMessage();

    /**
     * 是否发送的消息
     *
     * @return
     */
    boolean isToSend();
}
