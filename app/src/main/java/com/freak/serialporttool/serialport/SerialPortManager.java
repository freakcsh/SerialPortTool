package com.freak.serialporttool.serialport;


import android.annotation.SuppressLint;
import android.os.HandlerThread;
import android.serialport.SerialPort;

import com.freak.serialporttool.message.LogManager;
import com.freak.serialporttool.message.SendMessage;
import com.freak.serialporttool.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * 串口管理类
 * <p>
 * 触发扫码：7E 00 08 01 00 02 01 AB CD
 * 停止扫码：7E 00 08 01 00 02 00 AB CD
 * <p>
 * <p>
 * 一次读码延时（默认5秒）
 * 10秒：7E 00 08 01 00 06 64 AB CD
 * 15秒：7E 00 08 01 00 06 96 AB CD
 * 20秒：7E 00 08 01 00 06 C8 AB CD
 * 无限长：7E 00 08 01 00 06 00 AB CD
 * 保存：7E 00 09 01 00 00 00 AB CD （如果不保存，断电后不能生效）
 *
 * @author Freak
 * @date 2019/8/12.
 */
public class SerialPortManager {

    private static final String TAG = "SerialPortManager";

    private SerialReadThread mReadThread;
    private OutputStream mOutputStream;
    private HandlerThread mWriteThread;
    private Scheduler mSendScheduler;

    private static class InstanceHolder {

        public static SerialPortManager sManager = new SerialPortManager();
    }

    public static SerialPortManager instance() {
        return InstanceHolder.sManager;
    }

    private SerialPort mSerialPort;

    private SerialPortManager() {
    }

    /**
     * 打开串口
     *
     * @param device
     * @return
     */
    public SerialPort open(Device device) {
        return open(device.getPath(), device.getBaudrate());
    }

    /**
     * 打开串口
     *
     * @param devicePath
     * @param baudrateString
     * @return
     */
    public SerialPort open(String devicePath, String baudrateString) {
        if (mSerialPort != null) {
            close();
        }

        try {
            File device = new File(devicePath);
            int baurate = Integer.parseInt(baudrateString);
            mSerialPort = new SerialPort(device, baurate, 0);

            mReadThread = new SerialReadThread(mSerialPort.getInputStream());
            mReadThread.start();

            mOutputStream = mSerialPort.getOutputStream();

            mWriteThread = new HandlerThread("write-thread");
            mWriteThread.start();
            mSendScheduler = AndroidSchedulers.from(mWriteThread.getLooper());

            return mSerialPort;
        } catch (Throwable tr) {
            LogUtil.e("打开串口失败", tr);
            close();
            return null;
        }
    }

    /**
     * 关闭串口
     */
    public void close() {
        if (mReadThread != null) {
            mReadThread.close();
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mWriteThread != null) {
            mWriteThread.quit();
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    /**
     * 发送数据
     *
     * @param datas
     * @return
     */
    private void sendData(byte[] datas) throws Exception {
        mOutputStream.write(datas);
    }

    /**
     * (rx包裹)发送数据
     *
     * @param datas
     * @return
     */
    private Observable<Object> rxSendData(final byte[] datas) {

        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                try {
                    sendData(datas);
                    emitter.onNext(new Object());
                } catch (Exception e) {

                    LogUtil.e("发送：" + ByteUtil.bytes2HexStr(datas) + " 失败", e);

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }
                emitter.onComplete();
            }
        });
    }

    /**
     * 发送命令包
     */
    @SuppressLint("CheckResult")
    public void sendCommand(final String command) {

        // TODO: 2018/3/22
        LogUtil.i("发送命令：" + command);

        byte[] bytes = ByteUtil.hexStr2bytes(command);
        rxSendData(bytes).subscribeOn(mSendScheduler).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                LogManager.instance().post(new SendMessage(command));
            }

        });
    }
}

