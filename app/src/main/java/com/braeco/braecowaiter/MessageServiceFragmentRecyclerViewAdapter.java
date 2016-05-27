package com.braeco.braecowaiter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Weiping on 2015/12/1.
 */

public class MessageServiceFragmentRecyclerViewAdapter
        extends RecyclerView.Adapter<MessageServiceFragmentRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private SparseBooleanArray expandState = new SparseBooleanArray();

    public MessageServiceFragmentRecyclerViewAdapter() {

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_message_service, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int p = position;
        String orderIdString = "S" + String.format("%02d", position + 1);
        holder.serviceId.setText(orderIdString);
        holder.serviceType.setText((String)BraecoWaiterApplication.serve.get(p).get("serve"));
        holder.date.setText((String)BraecoWaiterApplication.serve.get(p).get("date"));
        if ("0".equals((String)BraecoWaiterApplication.serve.get(p).get("table"))) {
            holder.tableId.setText("外带");
        } else {
            holder.tableId.setText((String)BraecoWaiterApplication.serve.get(p).get("table") + "号桌");
        }
        holder.phone.setText((String)BraecoWaiterApplication.serve.get(p).get("phone"));
        holder.deal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deal(p);
            }
        });
        if (BraecoWaiterApplication.serve.get(p).get("word") == null
                || "".equals(BraecoWaiterApplication.serve.get(p).get("word"))
                || "null".equals(BraecoWaiterApplication.serve.get(p).get("word"))) {
            holder.wordLy.setVisibility(View.GONE);
        } else {
            holder.word.setText(BraecoWaiterApplication.serve.get(p).get("word") + "");
            holder.wordLy.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return BraecoWaiterApplication.serve.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView serviceId;
        public TextView serviceType;
        public TextView date;
        public TextView tableId;
        public TextView phone;
        public TextView deal;
        public LinearLayout wordLy;
        public TextView word;

        public ViewHolder(View v) {
            super(v);
            serviceId = (TextView)v.findViewById(R.id.service_id);
            serviceType = (TextView)v.findViewById(R.id.service_type);
            date = (TextView)v.findViewById(R.id.date);
            tableId = (TextView)v.findViewById(R.id.table_id);
            phone = (TextView)v.findViewById(R.id.phone);
            deal = (TextView)v.findViewById(R.id.deal);
            wordLy = (LinearLayout)v.findViewById(R.id.word_ly);
            word = (TextView)v.findViewById(R.id.word);
        }
    }

    private void deal(final int p) {
        new MaterialDialog.Builder(context)
                .title("提示")
                .content("已经完成这条服务了吗？")
                .positiveText("确认")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("id", BraecoWaiterApplication.serve.get(p).get("id"));
                                json.put("status", 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!BraecoWaiterApplication.socket.isClosed()) {
                                if (BraecoWaiterApplication.socket.isConnected()) {
                                    if (BraecoWaiterApplication.socket != null) {
                                        BraecoWaiterApplication.printWriterOut.print(
                                                BraecoWaiterUtils.getInstance().newString("service" , json));
                                        BraecoWaiterApplication.printWriterOut.flush();
                                    }
                                }
                            }
                            BraecoWaiterApplication.serve.remove(p);
                            BraecoWaiterApplication.messageServiceFragmentRecyclerViewAdapter.notifyDataSetChanged();
                            if (context instanceof MainActivity) {
                                ((MainActivity)context).setNum();
                            } else {
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "setNum error");
                            }
                        }
                        if (dialogAction == DialogAction.NEUTRAL) {
                            materialDialog.dismiss();
                        }
                    }
                })
                .show();
    }

}
