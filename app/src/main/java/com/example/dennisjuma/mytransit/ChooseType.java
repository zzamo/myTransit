package com.example.dennisjuma.mytransit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseType extends AppCompatActivity {

    Button User, Sacco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_type);

        Sacco = (Button) findViewById(R.id.buttonSacco);
        User = (Button) findViewById(R.id.buttonUser);

        Sacco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseType.this, SaccoSignUp.class);
                startActivity(intent);
                finish();
            }
        });

        User.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseType.this, UserSignUp.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
