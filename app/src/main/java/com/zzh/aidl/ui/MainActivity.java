package com.zzh.aidl.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zzh.aidl.MessageReceiver;
import com.zzh.aidl.MessageSender;
import com.zzh.aidl.R;
import com.zzh.aidl.adapter.ChartAdapter;
import com.zzh.aidl.data.MessageModel;
import com.zzh.aidl.service.MessageService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MessageSender messageSender;
    private EditText mEt;

    private String myName = "zzh";
    private String otherName = "jyl";

    private List<MessageModel> mData = new ArrayList<>();
    private RecyclerView recyclerView;
    private ChartAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.getConfig().setGlobalTag("Alog");

        mEt = (EditText) findViewById(R.id.et);
        recyclerView = findViewById(R.id.recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ChartAdapter(this, mData);
        recyclerView.setAdapter(mAdapter);

        LogUtils.e("准备绑定服务 ");
        initService();
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtils.e("链接服务成功 " + componentName.toString() + " iBinder = " + iBinder.toString());
            //使用asInterface方法取得AIDL对应的操作接口
            messageSender = MessageSender.Stub.asInterface(iBinder);

            try {
                //把消息接收器注册到服务端
                messageSender.registerReceiveListener(messageReceiver);
            } catch (Exception e) {
                LogUtils.e("注册消息接收器失败: " + e.getMessage());
            }

            //设置binder死亡监听
            try {
                messageSender.asBinder().linkToDeath(deathRecipient, 0);
            } catch (Exception e) {
                LogUtils.e("死亡监听注册失败:" + e.getMessage());
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.e("关闭服务成功 " + componentName.toString());
        }
    };

    /**
     * binder可能会意外死亡(比如service Crash)
     * Client监听到binder死亡后可以进行重连服务等操作
     */
    public IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            LogUtils.e("重新连接");
            if (messageSender != null) {
                messageSender.asBinder().unlinkToDeath(this, 0);
                messageSender = null;
            }
            initService();
        }
    };

    public MessageReceiver messageReceiver = new MessageReceiver.Stub() {
        @Override
        public void onMessageReceived(MessageModel receivedMessage) throws RemoteException {
            LogUtils.e("收到消息 " + receivedMessage.toString());
            refreshUI(receivedMessage);
        }
    };

    private void initService() {
        Intent intent = new Intent(this, MessageService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }


    @Override
    protected void onDestroy() {
        //解除消息监听接口
        if (messageSender != null && messageSender.asBinder().isBinderAlive()) {
            try {
                messageSender.unregisterReceiveListener(messageReceiver);
            } catch (Exception e) {
                LogUtils.e("关闭服务失败 " + e.toString());
            }
        }
        unbindService(serviceConnection);
        super.onDestroy();
    }

    public void startService(View view) {
        String s = mEt.getText().toString();
        if (TextUtils.isEmpty(s)) {
            ToastUtils.showShort("您发送的内容为空,请输入发送内容");
            return;
        }
        MessageModel messageModel = new MessageModel();
        messageModel.setContent(s);
        messageModel.setMyName(myName);
        messageModel.setFrom(myName);
        messageModel.setTo(otherName);
        LogUtils.e("消息为: " + messageModel.toString() + " ,准备发送");
        sendMessage(messageModel);
    }

    private void sendMessage(MessageModel messageModel) {
        if (messageModel == null) {
            ToastUtils.showShort("消息为空,请重试");
            return;
        }
        if (messageSender != null) {
            try {
                messageSender.sendChartMessage(messageModel);
            } catch (Exception e) {
                LogUtils.e("发送消息失败,原因为: " + e.getMessage());
            }
        } else {
            ToastUtils.showShort("服务未建立,请稍后重试");
        }
        refreshUI(messageModel);
    }

    private void refreshUI(final MessageModel messageModel) {
        if (ThreadUtils.isMainThread()) {
            refreshUIOnMain(messageModel);
        } else {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshUIOnMain(messageModel);
                }
            });
        }
    }

    private void refreshUIOnMain(MessageModel messageModel) {
        LogUtils.e("当前的线程为:" + Thread.currentThread().getName());
        if (messageModel != null) {
            mData.add(messageModel);
        }
        if (mData.size() > 0) {
            recyclerView.smoothScrollToPosition(mData.size() - 1);
        }
        mAdapter.notifyDataSetChanged();
    }
}