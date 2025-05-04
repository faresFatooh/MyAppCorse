package com.hrtrack.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hrtrack.app.R;
import com.hrtrack.app.models.CardItem;
import com.hrtrack.app.ui.FragmentHostActivity;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final List<CardItem> cardItems;
    private final Context context;

    public CardAdapter(List<CardItem> cardItems, Context context) {
        this.cardItems = cardItems;
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem item = cardItems.get(position);
        holder.title.setText(item.getTitle());
        holder.icon.setImageResource(item.getIconRes());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FragmentHostActivity.class);
            intent.putExtra(FragmentHostActivity.EXTRA_FRAGMENT, item.getFragmentTag());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.card_icon);
            title = itemView.findViewById(R.id.card_title);
        }
    }
}
