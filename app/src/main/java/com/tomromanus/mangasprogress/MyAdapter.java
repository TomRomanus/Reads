package com.tomromanus.mangasprogress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    private final Context context;
    private ArrayList<Item> data;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void OnBtnSubtractWatchedClicked(int position);
        void OnBtnSubtractWatchedLongClicked(int position);
        void OnBtnAddWatchedClicked(int position);
        void OnBtnMenuClicked(int position);
    }

    public void dataChanged(ArrayList<Item> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public MyAdapter(Context context, ArrayList<Item> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.layout_item, parent, false);
        return new ViewHolder(v, itemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = data.get(position);

        holder.txtTitle.setText(item.getTitle());
        holder.txtAmountWatched.setText(String.valueOf(item.getAmountWatched()));
        holder.btnSubtractWatched.setEnabled(item.getAmountWatched() > 0);

        if(item.isFinished())
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.cadet_blue_crayola, context.getTheme()));
        else
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.mint_cream, context.getTheme()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtTitle, txtAmountWatched;
        public Button btnSubtractWatched, btnAddWatched;
        public ImageButton btnMenu;
        public CardView cardView;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitleItem);
            txtAmountWatched = itemView.findViewById(R.id.txtAmountWatchedItem);
            cardView = itemView.findViewById(R.id.cardView);

            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnMenu.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.OnBtnMenuClicked(position);
                    }
                }
            });

            btnAddWatched = itemView.findViewById(R.id.btnAddWatched);
            btnAddWatched.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.OnBtnAddWatchedClicked(position);
                    }
                }
            });

            btnSubtractWatched = itemView.findViewById(R.id.btnSubstractWatched);
            btnSubtractWatched.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.OnBtnSubtractWatchedClicked(position);
                }
            });
            btnSubtractWatched.setOnLongClickListener(v -> {
                if(listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.OnBtnSubtractWatchedLongClicked(position);
                }
                return true;
            });
        }
    }
}
