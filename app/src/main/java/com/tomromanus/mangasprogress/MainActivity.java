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
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
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
    private ArrayList<Item> mainData = new ArrayList<>();
    private ArrayList<Item> subData = new ArrayList<>();
    private static final String FILEPATH = "MansgasProgressData.txt";
    private Button btnSave;
    private TextView txtSearch;
    private boolean isSearchActive;
    private int menuPosition;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSave = findViewById(R.id.btnSave);
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.GONE);

        File file = new File(getApplicationContext().getFilesDir(),FILEPATH);
        if(file.exists())
            getFromFile();
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
                        //adapter.notifyDataSetChanged();
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
                                    mainData.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();
                                itemChanged();
                            }
                        });
        recyclerView.addOnItemTouchListener(swipeTouchListener);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getBindingAdapterPosition();
                int position_target = target.getBindingAdapterPosition();

                Collections.swap(mainData, position_dragged, position_target);

                adapter.notifyItemMoved(position_dragged, position_target);
                itemChanged();

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        helper.attachToRecyclerView(recyclerView);
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

    public void onBtnSave_clicked(View view) {
        FileOutputStream fo = null;
        PrintWriter pw = null;

        try {
            fo = openFileOutput(FILEPATH, Context.MODE_PRIVATE);
            pw = new PrintWriter(fo);

            PrintWriter finalPw = pw;
            mainData.forEach(i -> finalPw.println(i.getTitle() + "$" + i.getAmountWatched() + "$" + i.isFinished()));
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            btnSave.setTextColor(Color.parseColor("#EFF6EE"));
            btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save, 0, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "unable to save please try again", Toast.LENGTH_LONG).show();

        } finally {
            try {
                assert pw != null;
                pw.close();
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void getFromFile() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILEPATH);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while((line = br.readLine()) != null) {
                String[] lineData = line.split("\\$");
                System.out.println(Arrays.toString(lineData) + mainData.size());
                boolean finished = false;
                if(lineData[2].equals("true"))
                    finished = true;
                mainData.add(new Item(lineData[0], Integer.parseInt(lineData[1]), finished));
            }

        } catch(Exception e) {
            e.printStackTrace();

        } finally {
            try {
                assert fis != null;
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}