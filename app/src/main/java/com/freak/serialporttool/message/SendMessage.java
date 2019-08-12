package com.freak.serialporttool.message;


import com.freak.serialporttool.utils.TimeUtil;

/**
 * 发送的日志
 *
 * @author Freak
 * @date 2019/8/12.
 */

public class SendMessage implements IMessage {

    private String command;
    private String message;

    public SendMessage(String command) {
        this.command = command;
        this.message = TimeUtil.currentTime() + "\n发送命令：\n" + command;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isToSend() {
        return true;
    }
}
