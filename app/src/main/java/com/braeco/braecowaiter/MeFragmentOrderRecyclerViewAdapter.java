package com.braeco.braecowaiter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.braeco.braecowaiter.UIs.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/1.
 */

public class MeFragmentOrderRecyclerViewAdapter
        extends RecyclerView.Adapter<MeFragmentOrderRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private HashMap<Integer, Boolean> expand;

    private OnRefundListener onRefundListener;

    public MeFragmentOrderRecyclerViewAdapter(OnRefundListener onRefundListener) {
        this.onRefundListener = onRefundListener;

        expand = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_me_fragment_record, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int p = position;

        holder.setIsRecyclable(false);
        holder.position.setText((p + 1) + "");

        if ("尚未到账".equals(BraecoWaiterApplication.oneDayRecords.get(position).get("serial"))) {
            holder.orderId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            holder.orderId.setText("尚未到账");
        } else if ("未出票".equals(BraecoWaiterApplication.oneDayRecords.get(position).get("serial"))) {
            holder.orderId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            holder.orderId.setText("未出票");
        } else if ("推送失败".equals(BraecoWaiterApplication.oneDayRecords.get(position).get("serial"))) {
            holder.orderId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            holder.orderId.setText("推送失败");
        } else if ("推送中".equals(BraecoWaiterApplication.oneDayRecords.get(position).get("serial"))) {
            holder.orderId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            holder.orderId.setText("推送中");
        } else {
            holder.orderId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            String orderIdString = String.format("%04d",
                    Integer.parseInt((String) BraecoWaiterApplication.oneDayRecords.get(position).get("serial")));
            holder.orderId.setText(orderIdString);
        }

        if ("cash".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("现金");
            holder.payType.setBackgroundResource(R.drawable.shape_cash_pay);
        } else if ("prepayment".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("会员余额");
            holder.payType.setBackgroundResource(R.drawable.shape_cash_pay);
        } else if ("waiter".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("服务员下单");
            holder.payType.setBackgroundResource(R.drawable.shape_waiter_pay);
        } else if ("wx_pub".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("微信");
            holder.payType.setBackgroundResource(R.drawable.shape_online_pay);
        } else if ("p2p_wx_pub".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("微信P2P");
            holder.payType.setBackgroundResource(R.drawable.shape_online_pay);
        } else if ("alipay_qr_f2f".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("支付宝当面付");
            holder.payType.setBackgroundResource(R.drawable.shape_ali_pay);
        } else if ("alipay_wap".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("支付宝");
            holder.payType.setBackgroundResource(R.drawable.shape_ali_pay);
        } else if ("bfb_wap".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("channel"))) {
            holder.payType.setText("百度钱包");
            holder.payType.setBackgroundResource(R.drawable.shape_online_pay);
        }

        long createDate = (Integer) BraecoWaiterApplication.oneDayRecords.get(p).get("create_date");
        Date createCalendar = new Date(createDate * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        holder.date.setText(sdf.format(createCalendar));

        if ("eatin".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("type"))) {
            holder.tableId.setText("堂食" + (String) BraecoWaiterApplication.oneDayRecords.get(p).get("table") + "号桌");
        } else if ("membership".equals((String) BraecoWaiterApplication.oneDayRecords.get(p).get("type"))) {
            holder.tableId.setText("会员充值");
        } else if ("takeout".equals((String) BraecoWaiterApplication.oneDayRecords.get(p).get("type"))) {
            holder.tableId.setText("外带");
        }

        if (!BraecoWaiterUtils.notNull((String) BraecoWaiterApplication.oneDayRecords.get(p).get("phone")))
            holder.phone.setVisibility(View.INVISIBLE);
        holder.phone.setText("手机号：" + BraecoWaiterApplication.oneDayRecords.get(p).get("phone"));

        holder.list = (List<Map<String, Object>>) BraecoWaiterApplication.oneDayRecords.get(p).get("content");
        holder.mealAdapter = new MealAdapter(holder.list);
        holder.listView.setAdapter(holder.mealAdapter);
        BraecoWaiterUtils.getInstance().getTotalHeightofListView(holder.listView);
        holder.sum.setText("¥" + String.format("%.2f", holder.mealAdapter.prices));

        if ("发起退款".equals(BraecoWaiterApplication.oneDayRecords.get(p).get("refund"))) {
            holder.refund.setEnabled(true);
            holder.refund.setText((String) BraecoWaiterApplication.oneDayRecords.get(p).get("refund"));
            holder.refund.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refund(context, String.valueOf(BraecoWaiterApplication.oneDayRecords.get(p).get("id")), p);
                }
            });
        } else {
            holder.refund.setBackgroundResource(R.drawable.button_n_refund);
            holder.refund.setText((String) BraecoWaiterApplication.oneDayRecords.get(p).get("refund"));
            holder.refund.setEnabled(false);
        }

        if (expanded(position)) holder.expandableLayout.showImmediately();
        else holder.expandableLayout.hideImmediately();

        holder.buttonLayout.setRotation(expanded(position) ? 180f : 0f);
        holder.buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (expanded(position)) holder.expandableLayout.hide();
                else holder.expandableLayout.show();
                BraecoWaiterUtils.createRotateAnimator(holder.buttonLayout, expanded(position) ? 180f : 0f, expanded(position) ? 0f : 180f).start();
                expand.put(position, !expanded(position));
            }
        });
    }

    private boolean expanded(int position) {
        return expand.containsKey(position) && expand.get(position);
    }

    @Override
    public int getItemCount() {
        return BraecoWaiterApplication.oneDayRecords.size();
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
        public TextView position;

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
            position = (TextView)v.findViewById(R.id.position);
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
            prices = 0;
            for (int i = 0; i < list.size(); i++) {
                prices += (int) list.get(i).get("sum") * Double.parseDouble((String)list.get(i).get("price"));
            }
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

            MealAdapterViewholder viewholder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_meal_detail, null);
                viewholder = new MealAdapterViewholder();
                viewholder.name = (TextView) convertView.findViewById(R.id.name);
                viewholder.price = (TextView) convertView.findViewById(R.id.price);
                viewholder.num = (TextView) convertView.findViewById(R.id.num);

                convertView.setTag(viewholder);

            } else {
                viewholder = (MealAdapterViewholder)convertView.getTag();
            }

            viewholder.name.setText((String)list.get(position).get("properties"));
//            sum += (int) list.get(position).get("sum");
            viewholder.num.setText("×" + String.valueOf((int) list.get(position).get("sum")));
            double d = Double.parseDouble((String)list.get(position).get("price"));
            if (d < 0) {
                viewholder.price.setText("-¥" + String.format("%.2f", -d));
            } else {
                viewholder.price.setText("¥" + String.format("%.2f", d));
            }

            if ((Boolean)list.get(position).get("refundingOrEd")) {
                viewholder.name.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.refunding_or_refunded));
                viewholder.num.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.refunding_or_refunded));
                viewholder.price.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.refunding_or_refunded));
            } else {
                viewholder.name.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black));
                viewholder.num.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black));
                viewholder.price.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black));
            }

            return convertView;

        }

        private class MealAdapterViewholder {
            public TextView name;
            public TextView price;
            public TextView num;
        }
    }

    interface OnRefundListener {
        void onRefund(int p);
    }
}
