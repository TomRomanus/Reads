package com.tomromanus.mangasprogress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    private final Context context;
    private ArrayList<Item> data;
    private OnItemClickListener itemClickListener;
    private PopupMenu.OnMenuItemClickListener menuItemClickListener;

    public interface OnItemClickListener {
        void OnBtnSubtractWatchedClicked(int position);
        void OnBtnSubtractWatchedLongClicked(int position);
        void OnBtnAddWatchedClicked(int position);
        void MenuPosition(int position);
    }

    public void dataChanged(ArrayList<Item> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setMenuItemClickListener(PopupMenu.OnMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
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
        return new ViewHolder(v, itemClickListener, menuItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = data.get(position);

        holder.txtTitle.setText(item.getTitle());
        holder.txtAmountWatched.setText(String.valueOf(item.getAmountWatched()));

        if(item.getAmountWatched() == 0)
            holder.btnSubtractWatched.setEnabled(false);
        if(item.getAmountWatched() > 0)
            holder.btnSubtractWatched.setEnabled(true);

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

        public ViewHolder(View itemView,
                          OnItemClickListener listener,
                          PopupMenu.OnMenuItemClickListener menuItemClickListener) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitleItem);
            txtAmountWatched = itemView.findViewById(R.id.txtAmountWatchedItem);
            btnSubtractWatched = itemView.findViewById(R.id.btnSubstractWatched);
            btnAddWatched = itemView.findViewById(R.id.btnAddWatched);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            cardView = itemView.findViewById(R.id.cardView);

            btnMenu.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.MenuPosition(position);
                        showPopupMenu(v, menuItemClickListener);
                    }
                }
            });

            btnAddWatched.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.OnBtnAddWatchedClicked(position);
                    }
                }
            });

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

        private void showPopupMenu(View view, PopupMenu.OnMenuItemClickListener menuItemClickListener) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(menuItemClickListener);
            popupMenu.show();
        }
    }
}
