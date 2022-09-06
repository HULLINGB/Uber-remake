package com.example.uberremake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ContractorSessionActivity extends AppCompatActivity {


    public Button mBeginFilming, mCancelSession;
    final int REQUEST_VIDEO_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_session);

        mBeginFilming.findViewById(R.id.Begin);
        mBeginFilming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ContractorSessionActivity.this, ContractorFilmingActivity.class);
                startActivity(intent);

            }
        });

        mCancelSession.findViewById(R.id.EndSession);
        mCancelSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContractorSessionActivity.this, ContractorMapActivity.class);
                startActivity(intent);



            }
        });


    }

}
