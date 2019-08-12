package com.freak.serialporttool.message;


import com.freak.serialporttool.serialport.ByteUtil;
import com.freak.serialporttool.utils.TimeUtil;

/**
 * 收到的日志
 * @author Freak
 * @date 2019/8/12.
 */

public class ReceiveMessage implements IMessage {
    
    private String command;
    private String message;

    public ReceiveMessage(String command) {
        this.command = command;
        this.message = TimeUtil.currentTime() + "\n收到命令：\n" + command+ "\n转换成十进制：\n"+ ByteUtil.hexToString1(command);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isToSend() {
        return false;
    }
}
