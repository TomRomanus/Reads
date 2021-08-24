package com.tomromanus.mangasprogress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {
    private TextView txtTitle, txtAmountEntered;
    private final DataHandler dataHandler = new TextFileDataHandler();

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

            Item item = new Item(title, Integer.parseInt(amountWatched),false);
            if(dataHandler.addData(item, this))
                returnBoolean = true;

        } else {
            Toast.makeText(this, "Please fill in title", Toast.LENGTH_SHORT).show();
            return false;
        }
        return returnBoolean;
    }

    private String toUpperCase(String text) {
        char[] textArray = text.toCharArray();
        boolean foundSpace = true;
        for(int i=0; i< textArray.length; i++) {
            if (textArray[i] == ' ')
                foundSpace = true;
            else if (foundSpace) {
                textArray[i] = Character.toUpperCase(textArray[i]);
                foundSpace = false;
            }
        }
        return String.valueOf(textArray);
    }
}