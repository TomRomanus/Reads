package com.tomromanus.mangasprogress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
    private RecyclerView recyclerView;

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
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        adapter = new MyAdapter(this, searchData);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initialiseAdapter();
        initialiseItemTouchHelper();
    }

    private void initialiseAdapter() {
        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnBtnSubtractWatchedClicked(int position) {
                searchData.get(position).subtractAmountWatched();
                itemChanged();
            }

            @Override
            public void OnBtnSubtractWatchedLongClicked(int position) {
                searchData.get(position).resetAmountWatched();
                itemChanged();
            }

            @Override
            public void OnBtnAddWatchedClicked(int position) {
                searchData.get(position).addAmountWatched();
                itemChanged();
            }

            @Override
            public void OnBtnMenuClicked(int position) {
                Item item = searchData.get(position);

                AlertDialog.Builder menuDialogBuilder = new AlertDialog.Builder(context);
                View menuAlertCustomDialog = LayoutInflater.from(ListView.this).inflate(R.layout.layout_item_menu,null);
                menuDialogBuilder.setView(menuAlertCustomDialog);
                AlertDialog menuAlertDialog = menuDialogBuilder.create();
                menuAlertDialog.show();

                TextView txtMenuTitle = menuAlertCustomDialog.findViewById(R.id.txtMenuTitle);
                txtMenuTitle.setText(item.getTitle());

                Button btnCopyTitle = menuAlertCustomDialog.findViewById(R.id.btnCopyTitle);
                btnCopyTitle.setOnClickListener(view -> {
                    String text = searchData.get(position).getTitle();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(text, text);
                    clipboard.setPrimaryClip(clip);
                    menuAlertDialog.dismiss();
                });

                Button btnChangeTitle = menuAlertCustomDialog.findViewById(R.id.btnChangeTitle);
                btnChangeTitle.setOnClickListener(view -> {
                    menuAlertDialog.dismiss();
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    View alertCustomDialog = LayoutInflater.from(ListView.this).inflate(R.layout.layout_change_title,null);
                    dialogBuilder.setView(alertCustomDialog);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();

                    EditText input = alertCustomDialog.findViewById(R.id.txtChangeTitle);
                    input.setText(searchData.get(position).getTitle());
                    Button btnSet = alertCustomDialog.findViewById(R.id.btnSetTitle);
                    Button btnCancel = alertCustomDialog.findViewById(R.id.btnCancelTitle);

                    btnSet.setOnClickListener(view1 -> {
                        searchData.get(position).setTitle(input.getText().toString());
                        itemChanged();
                        alertDialog.dismiss();
                    });

                    btnCancel.setOnClickListener(view12 -> alertDialog.dismiss());
                });

                Button btnMoveToTop = menuAlertCustomDialog.findViewById(R.id.btnMoveToTop);
                btnMoveToTop.setOnClickListener(view -> {
                    searchData.remove(item);
                    searchData.add(0, item);
                    mainData.remove(item);
                    mainData.add(0, item);
                    itemChanged();
                    menuAlertDialog.dismiss();
                });

                Button btnMoveToBottom = menuAlertCustomDialog.findViewById(R.id.btnMoveToBottom);
                btnMoveToBottom.setOnClickListener(view -> {
                    searchData.remove(item);
                    searchData.add(item);
                    mainData.remove(item);
                    mainData.add(item);
                    itemChanged();
                    menuAlertDialog.dismiss();
                });

                Button btnSetFinished = menuAlertCustomDialog.findViewById(R.id.btnSetFinished);
                btnSetFinished.setOnClickListener(view -> {
                    searchData.get(position).toggleFinished();
                    adapter.dataChanged(searchData);
                    adapter.notifyItemChanged(position);
                    itemChanged();
                    menuAlertDialog.dismiss();
                });

                Button btnDelete = menuAlertCustomDialog.findViewById(R.id.btnDelete);
                btnDelete.setOnClickListener(view -> {
                    searchData.remove(item);
                    mainData.remove(item);
                    itemChanged();
                    menuAlertDialog.dismiss();
                });
            }
        });
    }

    private void initialiseItemTouchHelper() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
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
                if(direction == ItemTouchHelper.RIGHT) {
                    int position = viewHolder.getBindingAdapterPosition();
                    Item item = searchData.get(position);
                    mainData.remove(item);
                    searchData.remove(item);
                    adapter.notifyItemRemoved(position);
                    itemChanged();

                }
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    private void itemChanged() {
        btnSave.setTextColor(getResources().getColor(R.color.red_munsell, getApplicationContext().getTheme()));
        btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_red, 0, 0, 0);
        adapter.dataChanged(searchData);
        adapter.notifyDataSetChanged();
    }

    public void onBtnBack_clicked(View view) {
        Snackbar snackbar = Snackbar.make(view, R.string.saving, Snackbar.LENGTH_SHORT);
        snackbar.show();
        if(dataHandler.saveData(mainData, this)) {
            snackbar.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else {
            snackbar = Snackbar
                    .make(view, R.string.saving_error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, this::onBtnBack_clicked);
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
        Snackbar snackbar = Snackbar.make(view, R.string.saving, Snackbar.LENGTH_SHORT);
        snackbar.show();
        if(dataHandler.saveData(mainData, this)) {
            snackbar.dismiss();
            Intent intent = new Intent(this, AddActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }
        else {
            snackbar = Snackbar
                    .make(view, R.string.saving_error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, this::onBtnBack_clicked);
            snackbar.show();
        }
    }

    public void onBtnSave_clicked(View view) {
        Snackbar snackbar = Snackbar.make(view, R.string.saving, Snackbar.LENGTH_SHORT);
        snackbar.show();
        if(dataHandler.saveData(mainData, this)) {
            snackbar.dismiss();
            btnSave.setTextColor(getResources().getColor(R.color.mint_cream, getApplicationContext().getTheme()));
            btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save, 0, 0, 0);
        }
        else {
            snackbar = Snackbar
                    .make(view, R.string.saving_error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, this::onBtnSave_clicked);
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