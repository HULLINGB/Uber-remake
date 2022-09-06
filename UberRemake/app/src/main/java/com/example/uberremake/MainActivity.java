package com.example.uberremake;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 *  This project is copied from SimCoder on Youtube
 * To get this project working, we must sign up at Firebase.google.com,
 * create an account and register package name and SHA-1 key.
 */
public class MainActivity extends AppCompatActivity {

    public Button mContractor, mCustomer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContractor = (Button) findViewById(R.id.contractor);
        mCustomer = (Button) findViewById(R.id.customer);


        mContractor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContractorLoginActivity.class);
                startActivity(intent);
                finish();
                //return;
            }
        });

        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                //return;
            }
        });
    }
}