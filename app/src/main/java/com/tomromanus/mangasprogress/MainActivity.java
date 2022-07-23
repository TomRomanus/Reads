package com.tomromanus.mangasprogress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnManga = findViewById(R.id.btnManga);
        btnManga.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ListView.class);
            intent.putExtra("type", "Mangas");
            startActivity(intent);
        });

        Button btnAnime = findViewById(R.id.btnAnime);
        btnAnime.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ListView.class);
            intent.putExtra("type", "Animes");
            startActivity(intent);
        });

        Button btnSeries = findViewById(R.id.btnSeries);
        btnSeries.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ListView.class);
            intent.putExtra("type", "Series");
            startActivity(intent);
        });

        Button btnBooks = findViewById(R.id.btnBooks);
        btnBooks.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ListView.class);
            intent.putExtra("type", "Books");
            startActivity(intent);
        });
    }
}