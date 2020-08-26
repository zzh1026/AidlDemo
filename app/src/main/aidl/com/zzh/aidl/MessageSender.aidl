// MessageSender.aidl
package com.zzh.aidl;

// Declare any non-default types here with import statements
import com.zzh.aidl.data.MessageModel;
import com.zzh.aidl.MessageReceiver;
interface MessageSender {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void sendChartMessage(in MessageModel messageModel);

    void registerReceiveListener(MessageReceiver messageReceiver);

    void unregisterReceiveListener(MessageReceiver messageReceiver);
}
