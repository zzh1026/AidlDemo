// MessageReceiver.aidl
package com.zzh.aidl;

// Declare any non-default types here with import statements
import com.zzh.aidl.data.MessageModel;
interface MessageReceiver {
    void onMessageReceived(in MessageModel receivedMessage);
}
