package com.freak.serialporttool.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.freak.serialporttool.R;
import com.freak.serialporttool.fragment.adapter.ListViewHolder;
import com.freak.serialporttool.message.IMessage;
import com.freak.serialporttool.message.LogManager;

/**
 * @author Freak
 * @date 2019/8/12.
 */
public class LogFragment extends Fragment {

    private Button mBtnClear;
    private ListView mLvLogs;
    private LogAdapter mAdapter;

    private Button mBtnAutoEnd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log, container, false);

        mBtnClear = (Button) view.findViewById(R.id.btn_clear_log);
        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清空列表
                LogManager.instance().clear();
                updateList();
            }
        });
        mBtnAutoEnd = (Button) view.findViewById(R.id.btn_auto_end);
        mBtnAutoEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogManager.instance().changAutoEnd();
                updateAutoEndButton();
            }
        });

        mLvLogs = (ListView) view.findViewById(R.id.lv_logs);
        mAdapter = new LogAdapter();
        mLvLogs.setAdapter(mAdapter);

        updateAutoEndButton();
        return view;
    }

    public void updateAutoEndButton() {
        if (getView() != null) {
            if (LogManager.instance().isAutoEnd()) {
                mBtnAutoEnd.setText("禁止自动显示最新日志");
                mLvLogs.setSelection(mAdapter.getCount() - 1);
            } else {
                mBtnAutoEnd.setText("自动显示最新日志");
            }
        }
    }

    private static class LogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return LogManager.instance().messages.size();
        }

        @Override
        public IMessage getItem(int position) {
            return LogManager.instance().messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, android.view.View convertView, ViewGroup parent) {

            IMessage message = getItem(position);

            ListViewHolder holder;
            if (convertView == null) {
                holder = new ListViewHolder(R.layout.item_log, parent);
                convertView = holder.getItemView();
            } else {
                holder = (ListViewHolder) convertView.getTag();
            }

            TextView tvLog = holder.getText(R.id.tv_log);
            TextView tvNum = holder.getText(R.id.tv_num);
            //设置消息
            tvLog.setText(message.getMessage());
            //设置接收消息和发送消息颜色
            tvLog.setEnabled(message.isToSend());
            //设置日志条数
            tvNum.setText(String.valueOf(position + 1));

            return convertView;
        }
    }

    public void add(IMessage message) {
        LogManager.instance().add(message);
        updateList();
    }

    public void updateList() {
        if (getView() != null) {
            mAdapter.notifyDataSetChanged();
            if (LogManager.instance().isAutoEnd()) {
                mLvLogs.setSelection(mAdapter.getCount() - 1);
            }
        }
    }
}

