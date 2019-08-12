package com.freak.serialporttool;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.SerialPortFinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.freak.serialporttool.base.BaseActivity;
import com.freak.serialporttool.base.IActivityStatusBar;
import com.freak.serialporttool.contance.PreferenceKeys;
import com.freak.serialporttool.serialport.Device;
import com.freak.serialporttool.serialport.SerialPortManager;
import com.freak.serialporttool.utils.PrefHelper;
import com.freak.serialporttool.utils.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Freak
 * @date 2019/8/12.
 */
public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, IActivityStatusBar, Handler.Callback {

    @BindView(R.id.spinner_devices)
    Spinner mSpinnerDevices;
    @BindView(R.id.spinner_baudrate)
    Spinner mSpinnerBaudrate;
    @BindView(R.id.btn_open_device)
    Button mBtnOpenDevice;
    @BindView(R.id.btn_send_data)
    Button mBtnSendData;
    @BindView(R.id.btn_stress)
    Button mBtnStress;
    @BindView(R.id.et_data)
    EditText mEtData;
    @BindView(R.id.edt_count)
    EditText mEdtCount;
    @BindView(R.id.edt_time)
    EditText mEdtTimm;

    private Device mDevice;

    private int mDeviceIndex;
    private int mBaudrateIndex;

    private String[] mDevices;
    private String[] mBaudrates;

    private boolean mOpened = false;

    private int mCount = 1;
    private int mTime = 5;
    private static final int MSG_DO_START_SCAN = 0;
    private int executeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDevice();
        initSpinners();
        updateViewState(mOpened);
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.instance().close();
        super.onDestroy();
    }

    @Override
    protected boolean hasActionBar() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    /**
     * 初始化设备列表
     */
    private void initDevice() {

        SerialPortFinder serialPortFinder = new SerialPortFinder();

        // 设备
        mDevices = serialPortFinder.getAllDevicesPath();
        if (mDevices.length == 0) {
            mDevices = new String[]{
                    getString(R.string.no_serial_device)
            };
        }
        // 波特率
        mBaudrates = getResources().getStringArray(R.array.baudrates);

        mDeviceIndex = PrefHelper.getDefault().getInt(PreferenceKeys.SERIAL_PORT_DEVICES, 0);
        mDeviceIndex = mDeviceIndex >= mDevices.length ? mDevices.length - 1 : mDeviceIndex;
        mBaudrateIndex = PrefHelper.getDefault().getInt(PreferenceKeys.BAUD_RATE, 0);

        mDevice = new Device(mDevices[mDeviceIndex], mBaudrates[mBaudrateIndex]);
    }

    public String getCount() {
        return mEdtCount.getText().toString().trim();
    }

    public String getTime() {
        return mEdtTimm.getText().toString().trim();
    }

    /**
     * 初始化下拉选项
     */
    private void initSpinners() {

        ArrayAdapter<String> deviceAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mDevices);
        deviceAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinnerDevices.setAdapter(deviceAdapter);
        mSpinnerDevices.setOnItemSelectedListener(this);

        ArrayAdapter<String> baudrateAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mBaudrates);
        baudrateAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinnerBaudrate.setAdapter(baudrateAdapter);
        mSpinnerBaudrate.setOnItemSelectedListener(this);

        mSpinnerDevices.setSelection(mDeviceIndex);
        mSpinnerBaudrate.setSelection(mBaudrateIndex);
    }

    @OnClick({R.id.btn_open_device, R.id.btn_send_data, R.id.btn_stress})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_open_device:
                switchSerialPort();
                break;
            case R.id.btn_send_data:
                sendData();
                break;
            //压力测试
            case R.id.btn_stress:
                mTime = Integer.parseInt(getTime());
                mCount = Integer.parseInt(getCount());
                executeCount = 0;
                handler.post(task);
                break;
            default:
                break;
        }
    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            Message ms = new Message();
            ms.what = MSG_DO_START_SCAN;
            handleMessage(ms);
            executeCount++;
            Log.i("次数", executeCount + "   频率    " + mTime+"   总次数   "+mCount);

            //设置延迟时间，此处默认是5秒
            handler.postDelayed(this, 2 * 1000);
            if (executeCount == mCount) {
                handler.removeCallbacks(task);
            }

        }
    };
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DO_START_SCAN:
                sendData();
                break;
            default:
                break;
        }
        return false;
    }

    private void sendData() {

        String text = mEtData.getText().toString().trim();
        if (TextUtils.isEmpty(text) || text.length() % 2 != 0) {
            ToastUtil.showOne(this, "无效数据");
            return;
        }

        SerialPortManager.instance().sendCommand(text);
    }

    /**
     * 打开或关闭串口
     */
    private void switchSerialPort() {
        if (mOpened) {
            SerialPortManager.instance().close();
            mOpened = false;
        } else {

            // 保存配置
            PrefHelper.getDefault().saveInt(PreferenceKeys.SERIAL_PORT_DEVICES, mDeviceIndex);
            PrefHelper.getDefault().saveInt(PreferenceKeys.BAUD_RATE, mBaudrateIndex);

            mOpened = SerialPortManager.instance().open(mDevice) != null;
            if (mOpened) {
                ToastUtil.showOne(this, "成功打开串口");
            } else {
                ToastUtil.showOne(this, "打开串口失败");
            }
        }
        updateViewState(mOpened);
    }

    /**
     * 更新视图状态
     *
     * @param isSerialPortOpened
     */
    private void updateViewState(boolean isSerialPortOpened) {

        int stringRes = isSerialPortOpened ? R.string.close_serial_port : R.string.open_serial_port;

        mBtnOpenDevice.setText(stringRes);

        mSpinnerDevices.setEnabled(!isSerialPortOpened);
        mSpinnerBaudrate.setEnabled(!isSerialPortOpened);
        mBtnSendData.setEnabled(isSerialPortOpened);
        mBtnStress.setEnabled(isSerialPortOpened);
        mTime = Integer.parseInt(TextUtils.isEmpty(getTime()) ? mTime + "" : getTime());
        mCount = Integer.parseInt(TextUtils.isEmpty(getTime()) ? mCount + "" : getCount());
        mEdtTimm.setText(mTime + "");
        mEdtCount.setText(mCount + "");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // Spinner 选择监听
        switch (parent.getId()) {
            case R.id.spinner_devices:
                mDeviceIndex = position;
                mDevice.setPath(mDevices[mDeviceIndex]);
                break;
            case R.id.spinner_baudrate:
                mBaudrateIndex = position;
                mDevice.setBaudrate(mBaudrates[mBaudrateIndex]);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 空实现
    }

    /**
     * 状态栏颜色
     *
     * @return
     */
    @Override
    public int getStatusBarColor() {
        return 0;
    }

    /**
     * 渐变色状态栏
     *
     * @return
     */
    @Override
    public int getDrawableStatusBar() {
        return 0;
    }


}
