package com.example.secuirtysystem;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.utils.Toaster;

public class Passcode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);

        //ParticleCloudSDK.init(this);
        //ParticleCloudSDK.getCloud().logIn("40344772@live.napier.ac.uk", "Monokuma7!");
        //Toaster.s(Passcode.this, "Logged in!");

        Button set = (Button)findViewById(R.id.confirmPass);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Passcode.this, MainActivity.class);
                startActivity(intent1);
            }
        });
    }
}