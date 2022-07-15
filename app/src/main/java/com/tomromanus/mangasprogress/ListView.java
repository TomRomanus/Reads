package com.tomromanus.mangasprogress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

public class ListView extends AppCompatActivity {
    private final ArrayList<Item> mainData = new ArrayList<>();
    private final ArrayList<Item> searchData = new ArrayList<>();
    private DataHandler dataHandler;
    private String type;

    private boolean isSearchActive;
    private int menuPosition;

    private Button btnSave;
    private TextView txtSearch;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        type = getIntent().getStringExtra("type");
        dataHandler = new TextFileDataHandler(type);

        mainData.addAll(dataHandler.getData(this));
        searchData.addAll(mainData);

        btnSave = findViewById(R.id.btnSave);
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.GONE);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        adapter = new MyAdapter(this, searchData);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnBtnSubtractWatchedClicked(int position) {
                Item item = searchData.get(position);
                item.subtractAmountWatched();
                itemChanged();
            }

            @Override
            public void OnBtnSubtractWatchedLongClicked(int position) {
                Item item = searchData.get(position);
                item.resetAmountWatched();
                itemChanged();
            }

            @Override
            public void OnBtnAddWatchedClicked(int position) {
                Item item = searchData.get(position);
                item.addAmountWatched();
                itemChanged();
            }

            @Override
            public void MenuPosition(int position) {
                menuPosition = position;
            }
        });

        adapter.setMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Item item = searchData.get(menuPosition);
                switch (menuItem.getItemId()) {
                    case R.id.copyTitle:
                        String text = searchData.get(menuPosition).getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(text, text);
                        clipboard.setPrimaryClip(clip);
                        return true;

                    case R.id.moveToTop:
                        searchData.clear();
                        searchData.add(item);
                        mainData.remove(item);
                        searchData.addAll(mainData);
                        mainData.clear();
                        mainData.addAll(searchData);
                        itemChanged();
                        return true;

                    case R.id.moveToBottom:
                        searchData.clear();
                        mainData.remove(item);
                        searchData.addAll(mainData);
                        searchData.add(item);
                        mainData.clear();
                        mainData.addAll(searchData);
                        itemChanged();
                        return true;

                    case R.id.finished:
                        searchData.get(menuPosition).toggleFinished();
                        adapter.dataChanged(searchData);
                        adapter.notifyItemChanged(menuPosition);
                        itemChanged();
                        return true;

                    case R.id.deleteItem:
                        searchData.remove(item);
                        mainData.remove(item);
                        itemChanged();
                        return true;

                    default: return false;
                }
            }
        });

        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return false;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Item item = searchData.get(position);
                                    mainData.remove(item);
                                    searchData.remove(item);
                                    adapter.notifyItemRemoved(position);
                                }
                                itemChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                onDismissedBySwipeLeft(recyclerView, reverseSortedPositions);
                            }
                        });
        recyclerView.addOnItemTouchListener(swipeTouchListener);

        if(searchData.size() == mainData.size()) {
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                    int position_dragged = dragged.getBindingAdapterPosition();
                    int position_target = target.getBindingAdapterPosition();

                    Collections.swap(mainData, position_dragged, position_target);
                    Collections.swap(searchData, position_dragged, position_target);

                    adapter.notifyItemMoved(position_dragged, position_target);

                    btnSave.setTextColor(getResources().getColor(R.color.red_munsell, getApplicationContext().getTheme()));
                    btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_red, 0, 0, 0);

                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                }
            });
            helper.attachToRecyclerView(recyclerView);
        }
    }

    private void itemChanged() {
        btnSave.setTextColor(getResources().getColor(R.color.red_munsell, getApplicationContext().getTheme()));
        btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_red, 0, 0, 0);
        adapter.dataChanged(searchData);
        adapter.notifyDataSetChanged();
    }

    public void onBtnBack_clicked(View view) {
        Snackbar snackbar = Snackbar.make(view, "Saving...", Snackbar.LENGTH_SHORT);
        snackbar.show();
        if(dataHandler.saveData(mainData, this)) {
            snackbar.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else {
            snackbar = Snackbar
                    .make(view, "Unable to save", Snackbar.LENGTH_LONG)
                    .setAction("retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBtnBack_clicked(view);
                        }});
            snackbar.show();
        }
    }

    public void onBtnAdd_clicked(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    public void onBtnSave_clicked(View view) {
        Snackbar snackbar = Snackbar.make(view, "Saving...", Snackbar.LENGTH_SHORT);
        snackbar.show();
        if(dataHandler.saveData(mainData, this)) {
            snackbar.dismiss();
            btnSave.setTextColor(getResources().getColor(R.color.mint_cream, getApplicationContext().getTheme()));
            btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save, 0, 0, 0);
        }
        else {
            snackbar = Snackbar
                    .make(view, "Unable to save", Snackbar.LENGTH_LONG)
                    .setAction("retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBtnSave_clicked(view);
                        }});
            snackbar.show();
        }
    }

    public void onBtnSearchClicked(View view) {
        if(!isSearchActive) {
            txtSearch.setVisibility(View.VISIBLE);
            isSearchActive = true;

            if(txtSearch.length() != 0)
                search(txtSearch.getText());

            txtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    search(s);
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        else {
            txtSearch.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            isSearchActive = false;
            searchData.clear();
            searchData.addAll(mainData);
            adapter.dataChanged(searchData);
            adapter.notifyDataSetChanged();
        }
    }

    private void search(CharSequence s) {
        searchData.clear();
        for (Item item : mainData) {
            if (item.getTitle().toLowerCase().contains(s.toString().toLowerCase()))
                searchData.add(item);
        }
        adapter.dataChanged(searchData);
        adapter.notifyDataSetChanged();
    }
}