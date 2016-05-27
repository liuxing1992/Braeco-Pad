package com.braeco.braecowaiter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.UIs.ExpandableLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/1.
 */

public class MessageBookFragmentRecyclerViewAdapter
        extends RecyclerView.Adapter<MessageBookFragmentRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private HashMap<Integer, Boolean> hide;

    private OnDealListener onDealListener;
    private OnRefundListener onRefundListener;

    public MessageBookFragmentRecyclerViewAdapter(OnDealListener onDealListener, OnRefundListener onRefundListener) {
        this.onDealListener = onDealListener;
        this.onRefundListener = onRefundListener;
        hide = new HashMap<>();

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_message_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        final int p = position;
        holder.orderId.setText((String) BraecoWaiterApplication.allOrder.get(p).get("serial"));
//        if ((Integer)BraecoWaiterApplication.allOrder.get(p).get("change") == -3) {
//            holder.payType.setText("等待支付宝扫码");
//            holder.payType.setBackgroundResource(R.drawable.shape_ali_pay);
//        } else if ((Integer)BraecoWaiterApplication.allOrder.get(p).get("change") == -2) {
//            holder.payType.setText("服务员下单");
//            holder.payType.setBackgroundResource(R.drawable.shape_waiter_pay);
//        } else if ((Integer)BraecoWaiterApplication.allOrder.get(p).get("change") == -1) {
//            holder.payType.setText("已在线支付");
//            holder.payType.setBackgroundResource(R.drawable.shape_online_pay);
//        } else {
//            holder.payType.setText("现金支付");
//            holder.payType.setBackgroundResource(R.drawable.shape_cash_pay);
//        }
        String payTypeString = (String) BraecoWaiterApplication.allOrder.get(p).get("channel");
        holder.payType.setText(payTypeString);
        if ("等待支付宝扫码".equals(payTypeString)) holder.payType.setBackgroundResource(R.drawable.shape_ali_pay);
        else if ("服务员下单".equals(payTypeString)) holder.payType.setBackgroundResource(R.drawable.shape_waiter_pay);
        else if ("已在线支付".equals(payTypeString)) holder.payType.setBackgroundResource(R.drawable.shape_online_pay);
        else if ("现金支付".equals(payTypeString)) holder.payType.setBackgroundResource(R.drawable.shape_cash_pay);
        else holder.payType.setBackgroundResource(R.drawable.shape_waiter_pay);

        holder.date.setText((String) BraecoWaiterApplication.allOrder.get(p).get("date"));
        if ("外带".equals((String) BraecoWaiterApplication.allOrder.get(p).get("table"))) {
            holder.tableId.setText("外带");
        } else if (BraecoWaiterApplication.allOrder.get(p).get("table") == null
                || "null".equals(BraecoWaiterApplication.allOrder.get(p).get("table"))) {
            holder.tableId.setText("会员充值");
        } else {
            holder.tableId.setText("堂食" + (String) BraecoWaiterApplication.allOrder.get(p).get("table") + "号桌");
        }
        holder.phone.setText((String) BraecoWaiterApplication.allOrder.get(p).get("phone"));

        holder.list = (List<Map<String, Object>>) BraecoWaiterApplication.allOrder.get(p).get("content");
        holder.mealAdapter = new MealAdapter(holder.list);
        holder.listView.setAdapter(holder.mealAdapter);
        holder.sum.setText("¥" +
                String.format("%.2f", (Double) BraecoWaiterApplication.allOrder.get(p).get("prices")));

        if ("发起退款".equals(BraecoWaiterApplication.allOrder.get(p).get("refund"))) {
            holder.refund.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refund(context, String.valueOf(BraecoWaiterApplication.allOrder.get(p).get("id")), p);
                }
            });
            holder.refund.setEnabled(true);
        } else {
            holder.refund.setBackgroundResource(R.drawable.button_n_refund);
            holder.refund.setText((String) BraecoWaiterApplication.allOrder.get(p).get("refund"));
            holder.refund.setEnabled(false);
        }

        holder.deal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deal(p);
            }
        });

        if (hided(position)) holder.expandableLayout.hideImmediately();
        else holder.expandableLayout.showImmediately();

        holder.buttonLayout.setRotation(hided(position) ? 180f : 0f);
        holder.buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (hided(position)) holder.expandableLayout.show();
                else holder.expandableLayout.hide();
                BraecoWaiterUtils.createRotateAnimator(holder.buttonLayout, hided(position) ? 180f : 0f, hided(position) ? 0f : 180f).start();
                hide.put(position, !hided(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return BraecoWaiterApplication.allOrder.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView orderId;
        public TextView payType;
        public TextView date;
        public RelativeLayout buttonLayout;
        public TextView tableId;
        public TextView phone;
        public List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        public ExpandedListView listView;
        public MealAdapter mealAdapter;
        public TextView sum;
        public ExpandableLayout expandableLayout;
        public AutofitTextView refund;
        public AutofitTextView deal;

        public ViewHolder(View v) {
            super(v);
            orderId = (TextView)v.findViewById(R.id.order_id);
            payType = (TextView)v.findViewById(R.id.pay_type);
            date = (TextView)v.findViewById(R.id.date);
            buttonLayout = (RelativeLayout) v.findViewById(R.id.button);
            tableId = (TextView)v.findViewById(R.id.table_id);
            phone = (TextView)v.findViewById(R.id.phone);
            listView = (ExpandedListView)v.findViewById(R.id.list_view);
            sum = (TextView)v.findViewById(R.id.sum);
            expandableLayout = (ExpandableLayout) v.findViewById(R.id.expandable_layout);
            refund = (AutofitTextView)v.findViewById(R.id.refund);
            deal = (AutofitTextView)v.findViewById(R.id.deal);
        }
    }

    private boolean hided(int position) {
        return hide.containsKey(position) && hide.get(position);
    }

    public String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getkey(String type_ , Object msg_){
        JSONObject object = new JSONObject();
        String str1 = "{";
        str1 += "\"certificate\":" + "\"" + BraecoWaiterApplication.certificate + "\"";
        if (msg_ != null) str1 += ",\"msg\":" +  msg_.toString();
        str1 += ",\"pw\":" + "\""+ BraecoWaiterApplication.password+ "\"";
        str1 += ",\"token\":" + "\""+ BraecoWaiterApplication.token+ "\"";
        str1 += ",\"type\":" + "\""+ type_ + "\""+ "}";
        return SHA1(str1);
    }

    public String newstring(String type_ , Object msg_) {
        String key = "";
        key = getkey(type_ , msg_);
        JSONObject object = new JSONObject();
        Object str1 = "{";
        str1 += "\"type\":" + "\"" + type_ + "\"";
        if (msg_ != null) str1 += ",\"msg\":" +  msg_;
        str1 += ",\"key\":" + "\""+ key + "\""+ "}";
        return (String)str1  + "\n";
    }

    private void deal(final int p) {
        if (!AuthorityManager.ableTo(Authority.DEAL_ORDER)) {
            AuthorityManager.showDialog(context, "处理订单");
            return;
        }
        if ("餐到付款".equals(BraecoWaiterApplication.allOrder.get(p).get("type"))) {
            new MaterialDialog.Builder(context)
                    .title("确认")
                    .content("该订单为“餐到付款”\n请确认收到顾客现金后再处理")
                    .positiveText("确认")
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                trueDeal(p);
                            }
                        }
                    })
                    .show();
        } else {
            trueDeal(p);
        }
    }

    private void trueDeal(final int p) {
        JSONObject obj = new JSONObject();
        JSONObject message = new JSONObject();
        try {
            message.put("orderid", (int) BraecoWaiterApplication.allOrder.get(p).get("id"));
            message.put("status", 1);
            obj.put("key", newstring("order_change", message));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BraecoWaiterApplication.allOrder.remove(p);
        BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter.notifyDataSetChanged();
        onDealListener.onDeal();
        if (BraecoWaiterApplication.socket != null) {
            if (!BraecoWaiterApplication.socket.isClosed()) {
                if (BraecoWaiterApplication.socket.isConnected()) {
                    try {
                        PrintWriter out = new PrintWriter(BraecoWaiterApplication.socket.getOutputStream(), true);
                        Log.d("BraecoWaiter", newstring("order_change", message));
                        out.print(newstring("order_change", message));
                        out.flush();
                    } catch (IOException i) {
                        Log.d("BraecoWaiter", "Ooops!");
                    }
                }
            } else {
                BraecoWaiterUtils.showToast(context, "网络故障");
            }
        }
    }

    private void refund(final Context context, final String id, final int p) {
        onRefundListener.onRefund(p);
    }

    public class MealAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Map<String, Object>> list;
        public int sum = 0;
        public double prices = 0;

        public MealAdapter(List<Map<String, Object>> list) {
            this.list = list;
            this.mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            MealAdapterViewHolder viewHolder;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.item_meal_detail, null);
                viewHolder = new MealAdapterViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.price = (TextView) convertView.findViewById(R.id.price);
                viewHolder.num = (TextView) convertView.findViewById(R.id.num);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (MealAdapterViewHolder)convertView.getTag();
            }

            viewHolder.name.setText((String)list.get(position).get("properties"));
            sum += (int) list.get(position).get("sum");
            prices += (int) list.get(position).get("sum") * (double)list.get(position).get("price");
            viewHolder.num.setText("×" + String.valueOf((int) list.get(position).get("sum")));
            double d = (double)list.get(position).get("price");
            if (d < 0) {
                viewHolder.price.setText("-¥" + String.format("%.2f", -d));
            } else {
                viewHolder.price.setText("¥" + String.format("%.2f", d));
            }

            if ((Boolean)list.get(position).get("refundingOrEd")) {
                viewHolder.name.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.refunding_or_refunded));
                viewHolder.num.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.refunding_or_refunded));
                viewHolder.price.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.refunding_or_refunded));
            } else {
                viewHolder.name.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black));
                viewHolder.num.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black));
                viewHolder.price.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black));
            }

            return convertView;
        }

        private class MealAdapterViewHolder {
            public TextView name;
            public TextView price;
            public TextView num;
        }

    }

    interface OnDealListener {
        void onDeal();
    }

    interface OnRefundListener {
        void onRefund(int p);
    }
}
