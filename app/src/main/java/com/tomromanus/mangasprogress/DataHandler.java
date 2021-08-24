package com.tomromanus.mangasprogress;

import android.content.Context;

import java.util.ArrayList;

public interface DataHandler {
    boolean saveData(ArrayList<Item> data, Context context);
    ArrayList<Item> getData(Context context);
    boolean addData(Item item, Context context);
}
