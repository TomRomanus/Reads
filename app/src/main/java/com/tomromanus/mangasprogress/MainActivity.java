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
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {
    private ArrayList<Item> data = new ArrayList<>();
    private ArrayList<Item> oldData;
    private static final String FILEPATH = "MansgasProgressData.txt";
    private Button btnSave;
    private int menuPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSave = findViewById(R.id.btnSave);

        File file = new File(getApplicationContext().getFilesDir(),FILEPATH);
        if(file.exists())
            getFromFile();
        oldData = data;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        MyAdapter adapter = new MyAdapter(this, data);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnBtnSubstractWatchedClicked(int position) {
                itemChanged();
                data.get(position).substractAmountWatched();
                adapter.dataChanged(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void OnBtnSubstractWatchedLongClicked(int position) {
                itemChanged();
                data.get(position).resetAmountWatched();
                adapter.dataChanged(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void OnBtnAddWatchedClicked(int position) {
                itemChanged();
                data.get(position).addAmountWatched();
                adapter.dataChanged(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void OnItemClicked(int position) {
                String text = data.get(position).getTitle();
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
                Item item = data.get(menuPosition);
                ArrayList<Item> newData = new ArrayList<>();
                switch (menuItem.getItemId()) {
                    case R.id.copyTitle:
                        String text = data.get(menuPosition).getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(text, text);
                        clipboard.setPrimaryClip(clip);
                        return true;

                    case R.id.moveToTop:
                        newData.add(item);
                        data.remove(item);
                        newData.addAll(data);
                        data = newData;
                        adapter.dataChanged(data);
                        adapter.notifyItemMoved(menuPosition, 0);
                        itemChanged();
                        return true;

                    case R.id.moveToBottom:
                        data.remove(item);
                        newData.addAll(data);
                        newData.add(item);
                        data = newData;
                        adapter.dataChanged(data);
                        adapter.notifyItemMoved(menuPosition, data.size()-1);
                        itemChanged();
                        return true;

                    case R.id.finished:
                        data.get(menuPosition).toggleFinished();
                        adapter.dataChanged(data);
                        adapter.notifyItemChanged(menuPosition);
                        //adapter.notifyDataSetChanged();
                        itemChanged();
                        return true;

                    case R.id.deleteItem:
                        data.remove(menuPosition);
                        adapter.dataChanged(data);
                        adapter.notifyItemRemoved(menuPosition);
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
                                    data.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();
                                itemChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    data.remove(position);
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

                Collections.swap(data, position_dragged, position_target);

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
    }

    public void onBtnAdd_clicked(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }

    public void onBtnSave_clicked(View view) {
        FileOutputStream fo = null;
        PrintWriter pw = null;

        try {
            fo = openFileOutput(FILEPATH, Context.MODE_PRIVATE);
            pw = new PrintWriter(fo);

            PrintWriter finalPw = pw;
            data.forEach(i -> finalPw.println(i.getTitle() + "$" + i.getAmountWatched() + "$" + i.isFinished()));
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
                System.out.println(Arrays.toString(lineData) + data.size());
                boolean finished = false;
                if(lineData[2].equals("true"))
                    finished = true;
                data.add(new Item(lineData[0], Integer.parseInt(lineData[1]), finished));
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