package com.braeco.braecowaiter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/1.
 */

public class ServiceVipFragmentRecyclerViewAdapter
        extends RecyclerView.Adapter<ServiceVipFragmentRecyclerViewAdapter.ViewHolder> {

    OnItemClickListener itemClickListener;

    private Context context;

    public ServiceVipFragmentRecyclerViewAdapter(OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_vip, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        holder.name.setText(BraecoWaiterApplication.vips.get(position).getNickname());
        holder.level.setText(BraecoWaiterApplication.vips.get(position).getLevel());
        holder.id.setText(BraecoWaiterApplication.vips.get(position).getDinnerId() + "");
        String phoneString = BraecoWaiterApplication.vips.get(position).getPhone();
        if (phoneString == null) holder.phoneLayout.setVisibility(View.GONE);
        else {
            holder.phoneLayout.setVisibility(View.VISIBLE);
            holder.phone.setText(phoneString);
        }
        holder.exp.setText(BraecoWaiterApplication.vips.get(position).getExp() + "");
        holder.balance.setText("¥ " + String.format("%.2f", BraecoWaiterApplication.vips.get(position).getBalance()));
        holder.position.setText((position + 1)  + "");
    }

    @Override
    public int getItemCount() {
        return BraecoWaiterApplication.vips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MaterialRippleLayout ripple;
        public TextView name;
        public AutofitTextView level;
        public AutofitTextView id;
        public LinearLayout phoneLayout;
        public AutofitTextView phone;
        public AutofitTextView exp;
        public AutofitTextView balance;
        public TextView position;

        public ViewHolder(View v) {
            super(v);
            ripple = (MaterialRippleLayout)v.findViewById(R.id.ripple);
            ripple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            name = (TextView)v.findViewById(R.id.name);
            level = (AutofitTextView)v.findViewById(R.id.level);
            id = (AutofitTextView)v.findViewById(R.id.id);
            phoneLayout = (LinearLayout)v.findViewById(R.id.phone_layout);
            phone = (AutofitTextView)v.findViewById(R.id.phone);
            exp = (AutofitTextView)v.findViewById(R.id.exp);
            balance = (AutofitTextView)v.findViewById(R.id.balance);
            position = (TextView)v.findViewById(R.id.position);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
