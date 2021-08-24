package com.tomromanus.mangasprogress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<Item> mainData = new ArrayList<>();
    private final ArrayList<Item> subData = new ArrayList<>();
    private Button btnSave;
    private TextView txtSearch;
    private boolean isSearchActive;
    private int menuPosition;
    private MyAdapter adapter;
    private final DataHandler dataHandler = new TextFileDataHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSave = findViewById(R.id.btnSave);
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.GONE);

        mainData.addAll(dataHandler.getData(this));
        subData.addAll(mainData);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        adapter = new MyAdapter(this, subData);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnBtnSubstractWatchedClicked(int position) {
                Item item = subData.get(position);
                int mainPosition = mainData.indexOf(item);
                mainData.remove(item);
                item.substractAmountWatched();
                mainData.add(mainPosition, item);
                itemChanged();
            }

            @Override
            public void OnBtnSubstractWatchedLongClicked(int position) {
                Item item = subData.get(position);
                int mainPosition = mainData.indexOf(item);
                mainData.remove(item);
                item.resetAmountWatched();
                mainData.add(mainPosition, item);
                itemChanged();
            }

            @Override
            public void OnBtnAddWatchedClicked(int position) {
                Item item = subData.get(position);
                int mainPosition = mainData.indexOf(item);
                mainData.remove(item);
                item.addAmountWatched();
                mainData.add(mainPosition, item);
                itemChanged();
            }

            @Override
            public void OnItemClicked(int position) {
                String text = subData.get(position).getTitle();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(text, text);
                clipboard.setPrimaryClip(clip);
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
                Item item = subData.get(menuPosition);
                switch (menuItem.getItemId()) {
                    case R.id.copyTitle:
                        String text = mainData.get(menuPosition).getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(text, text);
                        clipboard.setPrimaryClip(clip);
                        return true;

                    case R.id.moveToTop:
                        subData.clear();
                        subData.add(item);
                        mainData.remove(item);
                        subData.addAll(mainData);
                        mainData.clear();
                        mainData.addAll(subData);
                        itemChanged();
                        return true;

                    case R.id.moveToBottom:
                        subData.clear();
                        mainData.remove(item);
                        subData.addAll(mainData);
                        subData.add(item);
                        mainData.clear();
                        mainData.addAll(subData);
                        itemChanged();
                        return true;

                    case R.id.finished:
                        mainData.get(menuPosition).toggleFinished();
                        adapter.dataChanged(mainData);
                        adapter.notifyItemChanged(menuPosition);
                        itemChanged();
                        return true;

                    case R.id.deleteItem:
                        subData.remove(item);
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
                                    mainData.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();
                                itemChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Item item = subData.get(position);
                                    mainData.remove(item);
                                    subData.remove(item);
                                    adapter.notifyItemRemoved(position);
                                }
                                itemChanged();
                            }
                        });
        recyclerView.addOnItemTouchListener(swipeTouchListener);

        if(subData.size() == mainData.size()) {
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                    int position_dragged = dragged.getBindingAdapterPosition();
                    int position_target = target.getBindingAdapterPosition();

                    Collections.swap(mainData, position_dragged, position_target);
                    Collections.swap(subData, position_dragged, position_target);

                    adapter.notifyItemMoved(position_dragged, position_target);

                    btnSave.setTextColor(Color.RED);
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
        btnSave.setTextColor(Color.RED);
        btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_red, 0, 0, 0);
        adapter.dataChanged(subData);
        adapter.notifyDataSetChanged();
    }

    public void onBtnAdd_clicked(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }

    public void onBtnSave_clicked(View view) {
        if(dataHandler.saveData(mainData, this)) {
            btnSave.setTextColor(Color.parseColor("#EFF6EE"));
            btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save, 0, 0, 0);
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
            subData.clear();
            subData.addAll(mainData);
            adapter.dataChanged(subData);
            adapter.notifyDataSetChanged();
        }
    }

    private void search(CharSequence s) {
        subData.clear();
        for (Item item : mainData) {
            if (item.getTitle().toLowerCase().contains(s.toString().toLowerCase()))
                subData.add(item);
        }
        adapter.dataChanged(subData);
        adapter.notifyDataSetChanged();
    }
}