package com.tomromanus.mangasprogress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class AddActivity extends AppCompatActivity {
    private TextView txtTitle, txtAmountEntered;
    private static final String FILEPATH = "MansgasProgressData.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        txtTitle = findViewById(R.id.txtTitle);
        txtAmountEntered = findViewById(R.id.txtAmountEntered);
    }

    public void onBtnCancel_clicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onBtnSaveAdd_clicked(View view) {
        if(saveToFile()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    private boolean saveToFile() {
        boolean returnBoolean = false;
        String title = txtTitle.getText().toString();
        String amountWatched = txtAmountEntered.getText().toString();

        if (!title.equals("")) {
            title = toUpperCase(title);
            if(amountWatched.equals(""))
                amountWatched = "0";

            FileOutputStream fo = null;
            PrintWriter pw = null;

            try {
                fo = openFileOutput(FILEPATH, MODE_APPEND);
                pw = new PrintWriter(fo);

                pw.println(title + "$" + amountWatched + "$False");

                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                returnBoolean = true;

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
            return returnBoolean;

        } else {
            Toast.makeText(this, "Please fill in title", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private String toUpperCase(String text) {
        char[] textArray = text.toCharArray();
        boolean foundSpace = true;
        for(int i=0; i< textArray.length; i++) {
            if (Character.isLetter(textArray[i])) {
                if (foundSpace) {
                    textArray[i] = Character.toUpperCase(textArray[i]);
                    foundSpace = false;
                }
            } else {
                foundSpace = true;
            }
        }
        return String.valueOf(textArray);
    }
}