package com.freak.serialporttool.serialport;

import android.os.SystemClock;
import android.util.Log;

import com.freak.serialporttool.message.LogManager;
import com.freak.serialporttool.message.ReceiveMessage;
import com.freak.serialporttool.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 串口读线程
 *
 * @author Freak
 * @date 2019/8/12.
 */
public class SerialReadThread extends Thread {

    private static final String TAG = "SerialReadThread";

    private BufferedInputStream mInputStream;

    public SerialReadThread(InputStream is) {
        mInputStream = new BufferedInputStream(is);
    }

    @Override
    public void run() {
        byte[] received = new byte[1024];
        int size;

        LogUtil.e("开始读线程");

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {

                int available = mInputStream.available();

                if (available > 0) {
                    size = mInputStream.read(received);
                    if (size > 0) {
                        onDataReceive(received, size);
                    }
                } else {
                    // 暂停一点时间，免得一直循环造成CPU占用率过高
                    SystemClock.sleep(1);
                }
            } catch (IOException e) {
                LogUtil.e("读取数据失败", e);
            }
            //Thread.yield();
        }

        LogUtil.e("结束读进程");
    }

    /**
     * 处理获取到的数据
     *
     * @param received
     * @param size
     */
    private void onDataReceive(byte[] received, int size) {
        // TODO: 2018/3/22 解决粘包、分包等
        String hexStr = ByteUtil.bytes2HexStr(received, 0, size);
        LogManager.instance().post(new ReceiveMessage(hexStr));
        LogUtil.e("扫码结果--》"+ hexStr);
//        LogManager.instance().post(new ReceiveMessage(ByteUtil.hexStr2decimal(hexStr)+""));
    }

    /**
     * 停止读线程
     */
    public void close() {

        try {
            mInputStream.close();
        } catch (IOException e) {
            LogUtil.e("异常", e);
        } finally {
            super.interrupt();
        }
    }
}

