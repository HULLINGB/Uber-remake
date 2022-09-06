package com.example.uberremake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CustomerContractActivity extends AppCompatActivity {



    public Button mAccept, mDecline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_contract);


        mAccept.findViewById(R.id.Accept2);
        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerContractActivity.this, CustomerSessionActivity.class);
                startActivity(intent);

            }
        });


        mDecline.findViewById(R.id.Decline2);
        mDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(CustomerContractActivity.this, CustomerMapActivity.class);
                startActivity(intent);
            }
        });


    }


}
