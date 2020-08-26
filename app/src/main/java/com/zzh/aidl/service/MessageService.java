package com.zzh.aidl.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.blankj.utilcode.util.LogUtils;
import com.zzh.aidl.MessageReceiver;
import com.zzh.aidl.MessageSender;
import com.zzh.aidl.data.MessageModel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageService extends Service {
    private String RESULT_POOL = "时间到了看风景了斯柯达放假落实到积分了三定律客服塑料袋看风景莱克斯顿解放路口就水电费";
    private Random random = new Random();

    private AtomicBoolean serviceStop = new AtomicBoolean(false);
    private RemoteCallbackList<MessageReceiver> listenerList = new RemoteCallbackList<>();

    public MessageService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    IBinder messageSender = new MessageSender.Stub() {
        @Override
        public void sendChartMessage(MessageModel messageModel) throws RemoteException {
            LogUtils.e("messageModel = " + messageModel.toString());
            sendBackMessage(messageModel);
        }

        @Override
        public void registerReceiveListener(MessageReceiver messageReceiver) throws RemoteException {
            LogUtils.e("添加注册的 MessageReceiver " + messageReceiver.toString());
            listenerList.register(messageReceiver);
        }

        @Override
        public void unregisterReceiveListener(MessageReceiver messageReceiver) throws RemoteException {
            LogUtils.e("取消注册的 MessageReceiver " + messageReceiver.toString());
            listenerList.unregister(messageReceiver);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            /**
             * 包名验证方式
             */
            String packageName = null;
            String[] packagesForUid = getPackageManager().getPackagesForUid(getCallingUid());
            if (packagesForUid != null && packagesForUid.length > 0) {
                packageName = packagesForUid[0];
            }
            if (packageName == null || !packageName.startsWith("com.zzh.aidl")) {
                LogUtils.e("拒绝调用 : " + packageName);
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    private void sendBackMessage(final MessageModel messageModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.e("当前发送消息线程为: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MessageModel messageModelBack = new MessageModel();
                messageModelBack.setFrom(messageModel.getTo());
                messageModelBack.setTo(messageModel.getFrom());
                messageModelBack.setContent(getBackMsgContent(messageModel.getContent()));
                int count = listenerList.beginBroadcast();
                for (int i = 0; i < count; i++) {
                    MessageReceiver broadcastItem = listenerList.getBroadcastItem(i);
                    if (broadcastItem != null) {
                        try {
                            broadcastItem.onMessageReceived(messageModelBack);
                        } catch (Exception e) {
                            LogUtils.e("服务端消息发送失败: " + e.getMessage());
                        }
                    }
                }
                listenerList.finishBroadcast();
            }
        }).start();
    }

    private String getBackMsgContent(String receiveContent) {
        int backContentLen = random.nextInt(5) + 3;
        StringBuilder sb = new StringBuilder();
        sb.append("服务端收到了您的消息: ");
        sb.append("\n");
        sb.append(receiveContent);
        sb.append("\n");
        sb.append("服务端的回复为: ");
        sb.append("\n");
        for (int i = 0; i < backContentLen; i++) {
            sb.append(RESULT_POOL.charAt(random.nextInt(RESULT_POOL.length())));
        }
        return sb.toString();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //自定义权限方式检查权限
        if (checkCallingOrSelfPermission("com.zzh.aidl.permission.REMOTE_SERVICE_PERMISSION") == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return messageSender;
    }

}
