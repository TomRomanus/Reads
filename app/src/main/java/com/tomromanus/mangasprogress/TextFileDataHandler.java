package com.tomromanus.mangasprogress;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TextFileDataHandler implements DataHandler{
    private static final String FILEPATH = "MangasProgressData.txt";
    //[title, amountWatched, isFinished, type]

    @Override
    public boolean saveData(ArrayList<Item> data, Context context) {
        FileOutputStream fo = null;
        PrintWriter pw = null;
        boolean returnBoolean = false;

        try {
            fo = context.openFileOutput(FILEPATH, Context.MODE_PRIVATE);
            pw = new PrintWriter(fo);

            PrintWriter finalPw = pw;
            data.forEach(i -> finalPw.println(
                    i.getTitle() + "$" +
                    i.getAmountWatched() + "$" +
                    i.isFinished() + "$" +
                    i.getType()));
            returnBoolean = true;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "unable to save please try again", Toast.LENGTH_LONG).show();

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

        File file = new File(context.getApplicationContext().getFilesDir(),FILEPATH);
        if(file.exists()) {
            FileInputStream fis = null;

            try {
                fis = context.openFileInput(FILEPATH);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line;

                while ((line = br.readLine()) != null) {
                    String[] lineData = line.split("\\$");
                    boolean finished = false;
                    if (lineData[2].equals("true"))
                        finished = true;
                    data.add(new Item(lineData[0], Integer.parseInt(lineData[1]), finished, lineData[3]));
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
        boolean returnBoolean = false;

            FileOutputStream fo = null;
            PrintWriter pw = null;

            try {
                fo = context.openFileOutput(FILEPATH, Context.MODE_APPEND);
                pw = new PrintWriter(fo);

                pw.println(item.getTitle() + "$" +
                        item.getAmountWatched() +
                        "$False" + "$" +
                        item.getType());

                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                returnBoolean = true;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "unable to save please try again", Toast.LENGTH_LONG).show();

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
}
