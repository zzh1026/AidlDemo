package com.zzh.aidl.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zzh.aidl.R;
import com.zzh.aidl.data.MessageModel;

import java.util.List;

/**
 * 车主邦
 * ---------------------------
 * <p>
 * Created by zhaozh on 2020/8/26.
 */
public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.MyViewHolder> {
    public List<MessageModel> mDatas;
    private Activity activity;
    private LayoutInflater inflater;

    private final int VIEW_TYPE_MY = 1;
    private final int VIEW_TYPE_OTHER = 2;

    public ChartAdapter(Activity activity, List<MessageModel> mDatas) {
        this.mDatas = mDatas;
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public ChartAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource = R.layout.item_chart_other_layout;
        if (viewType == VIEW_TYPE_MY) {
            resource = R.layout.item_chart_my_layout;
        }
        return new MyViewHolder(inflater.inflate(resource, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChartAdapter.MyViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        MessageModel messageModel = mDatas.get(position);
        switch (itemViewType) {
            case VIEW_TYPE_MY:
                holder.textView.setText(" : " + messageModel.getFrom() + "\n" + messageModel.getContent());
                break;
            default:
                holder.textView.setText(messageModel.getFrom() + " : " + "\n" + messageModel.getContent());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel messageModel = mDatas.get(position);
        String from = messageModel.getFrom();
        if (!TextUtils.isEmpty(from) && from.equals(messageModel.getMyName())) {
            return VIEW_TYPE_MY;
        }
        return VIEW_TYPE_OTHER;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv);
        }
    }

}
