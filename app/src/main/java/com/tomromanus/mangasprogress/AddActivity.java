package com.tomromanus.mangasprogress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AddActivity extends AppCompatActivity {
    private TextView txtTitle, txtAmountEntered;
    private Switch swMangaAnime;
    private DataHandler dataHandler;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        type = getIntent().getStringExtra("type");
        dataHandler = new TextFileDataHandler(type);

        txtTitle = findViewById(R.id.txtTitle);
        txtAmountEntered = findViewById(R.id.txtAmountEntered);
        swMangaAnime = findViewById(R.id.swMangaAnime);
    }

    public void onBtnCancel_clicked(View view) {
        Intent intent = new Intent(this, ListView.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    public void onBtnSaveAdd_clicked(View view) {
        if(saveToFile()) {
            Intent intent = new Intent(this, ListView.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }
    }

    private boolean saveToFile() {
        boolean returnBoolean = false;
        String title = txtTitle.getText().toString();
        String amountWatched = txtAmountEntered.getText().toString();
        String type;

        if(swMangaAnime.isChecked())
            type = "A";
        else type = "M";

        if (!title.equals("")) {
            title = toUpperCase(title);
            if(amountWatched.equals(""))
                amountWatched = "0";

            Item item = new Item(title, Integer.parseInt(amountWatched),false, type);
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