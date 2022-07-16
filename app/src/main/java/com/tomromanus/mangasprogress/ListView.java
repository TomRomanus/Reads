package com.tomromanus.mangasprogress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

public class ListView extends AppCompatActivity {
    private final ArrayList<Item> mainData = new ArrayList<>();
    private final ArrayList<Item> searchData = new ArrayList<>();
    private DataHandler dataHandler;
    private String type;

    private boolean isSearchActive;

    private Button btnSave;
    private TextView txtSearch;
    private MyAdapter adapter;

    private final Context context = this;

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
            public void OnBtnMenuClicked(int position) {
                Item item = searchData.get(position);

                AlertDialog.Builder menuDialogBuilder = new AlertDialog.Builder(context);
                View menuAlertCustomdialog = LayoutInflater.from(ListView.this).inflate(R.layout.layout_item_menu,null);
                menuAlertCustomdialog.setElevation(100);
                menuDialogBuilder.setView(menuAlertCustomdialog);
                AlertDialog menuAlertDialog = menuDialogBuilder.create();
                menuAlertDialog.show();

                TextView txtMenuTitle = (TextView) menuAlertCustomdialog.findViewById(R.id.txtMenuTitle);
                txtMenuTitle.setText(item.getTitle());

                Button btnCopyTitle = (Button) menuAlertCustomdialog.findViewById(R.id.btnCopyTitle);
                Button btnChangeTitle = (Button) menuAlertCustomdialog.findViewById(R.id.btnChangeTitle);
                Button btnMoveToTop = (Button) menuAlertCustomdialog.findViewById(R.id.btnMoveToTop);
                Button btnMoveToBottom = (Button) menuAlertCustomdialog.findViewById(R.id.btnMoveToBottom);
                Button btnSetFinished = (Button) menuAlertCustomdialog.findViewById(R.id.btnSetFinished);
                Button btnDelete = (Button) menuAlertCustomdialog.findViewById(R.id.btnDelete);

                btnCopyTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = searchData.get(position).getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(text, text);
                        clipboard.setPrimaryClip(clip);
                        menuAlertDialog.dismiss();
                    }
                });

                btnChangeTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuAlertDialog.dismiss();
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                        View alertCustomdialog = LayoutInflater.from(ListView.this).inflate(R.layout.layout_change_title,null);
                        dialogBuilder.setView(alertCustomdialog);
                        AlertDialog alertDialog = dialogBuilder.create();
                        alertDialog.show();

                        EditText input = (EditText) alertCustomdialog.findViewById(R.id.txtChangeTitle);
                        input.setText(searchData.get(position).getTitle());
                        Button btnSet = (Button) alertCustomdialog.findViewById(R.id.btnSetTitle);
                        Button btnCancel = (Button) alertCustomdialog.findViewById(R.id.btnCancelTitle);

                        btnSet.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                searchData.get(position).setTitle(input.getText().toString());
                                itemChanged();
                                alertDialog.dismiss();
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });
                    }
                });

                btnMoveToTop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchData.remove(item);
                        searchData.add(0, item);
                        mainData.remove(item);
                        mainData.add(0, item);
                        itemChanged();
                        menuAlertDialog.dismiss();
                    }
                });

                btnMoveToBottom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchData.remove(item);
                        searchData.add(item);
                        mainData.remove(item);
                        mainData.add(item);
                        itemChanged();
                        menuAlertDialog.dismiss();
                    }
                });

                btnSetFinished.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchData.get(position).toggleFinished();
                        adapter.dataChanged(searchData);
                        adapter.notifyItemChanged(position);
                        itemChanged();
                        menuAlertDialog.dismiss();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchData.remove(item);
                        mainData.remove(item);
                        itemChanged();
                        menuAlertDialog.dismiss();
                    }
                });
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

    public void onBtnRevert_clicked(View view) {
        mainData.clear();
        searchData.clear();
        mainData.addAll(dataHandler.getData(this));
        searchData.addAll(mainData);
        adapter.dataChanged(searchData);
        adapter.notifyDataSetChanged();
        btnSave.setTextColor(getResources().getColor(R.color.mint_cream, getApplicationContext().getTheme()));
        btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save, 0, 0, 0);
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