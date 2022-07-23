package com.tomromanus.mangasprogress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class AddActivity extends AppCompatActivity {
    private TextView txtTitle, txtAmountEntered;
    private String type;
    private DataHandler dataHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        txtTitle = findViewById(R.id.txtTitle);
        txtAmountEntered = findViewById(R.id.txtAmountEntered);

        type = getIntent().getStringExtra("type");
        dataHandler = new TextFileDataHandler(type);

        TextView txtAmountRead = findViewById(R.id.txtAmountRead);
        if(type.equals("Animes") || type.equals("Series"))
            txtAmountRead.setText(getResources().getString(R.string.episodes_watched));
        else if(type.equals("Books"))
            txtAmountRead.setText(getResources().getString(R.string.pages_read));
        else
            txtAmountRead.setText(getResources().getString(R.string.chapters_read));
    }

    public void onBtnCancel_clicked(View view) {
        Intent intent = new Intent(this, ListView.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    public void onBtnSaveAdd_clicked(View view) {
        if (saveToFile()) {
            Intent intent = new Intent(this, ListView.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }
    }

    private boolean saveToFile() {
        String title = txtTitle.getText().toString();
        String amountWatched = txtAmountEntered.getText().toString();

        if(!title.isEmpty())
        {
            title = toUpperCase(title);
            if(amountWatched.isEmpty()) amountWatched = "0";

            Item item = new Item(title, Integer.parseInt(amountWatched),false);
            return dataHandler.addData(item, this);
        }
        else
        {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Please fill in title", Snackbar.LENGTH_SHORT)
                    .setAnchorView(txtTitle)
                    .show();
            return false;
        }
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