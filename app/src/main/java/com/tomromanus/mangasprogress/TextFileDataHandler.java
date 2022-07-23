package com.tomromanus.mangasprogress;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TextFileDataHandler implements DataHandler{
    private String filepath = "ProgressData.txt";
    //[title, amountWatched, isFinished]


    public TextFileDataHandler(String type) {
        this.filepath = type + filepath;
    }

    @Override
    public boolean saveData(ArrayList<Item> data, Context context) {
        FileOutputStream fo = null;
        PrintWriter pw = null;
        boolean returnBoolean = false;

        try {
            fo = context.openFileOutput(filepath, Context.MODE_PRIVATE);
            pw = new PrintWriter(fo);

            PrintWriter finalPw = pw;
            data.forEach(i -> finalPw.println(
                    i.getTitle() + "$" +
                    i.getAmountWatched() + "$" +
                    i.isFinished()));
            returnBoolean = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert pw != null;
                pw.close();
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnBoolean;
    }

    @Override
    public ArrayList<Item> getData(Context context) {
        ArrayList<Item> data = new ArrayList<>();

        File file = new File(context.getApplicationContext().getFilesDir(), filepath);
        if(file.exists()) {
            FileInputStream fis = null;

            try {
                fis = context.openFileInput(filepath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line;

                while ((line = br.readLine()) != null) {
                    String[] lineData = line.split("\\$");
                    boolean finished = lineData[2].equals("true");
                    data.add(new Item(lineData[0], Integer.parseInt(lineData[1]), finished));
                }

            } catch (Exception e) {
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
        return data;
    }

    @Override
    public boolean addData(Item item, Context context) {
        ArrayList<Item> tempList = new ArrayList<>();
        tempList.add(item);
        tempList.addAll(getData(context));
        return saveData(tempList, context);
    }
}
