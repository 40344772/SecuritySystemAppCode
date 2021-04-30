package com.example.secuirtysystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    boolean armed = false;
    boolean tripped = false;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParticleCloudSDK.init(this);

        //UiSetup(armed);
        armedDevice();
        particleLogin();
        particleSubscribe();
        //changePasscode();
        checkDevice();
        createNotificationChannel();


    }

    public void UiSetup(boolean armed){

        ImageView padUnlocked = (ImageView)findViewById(R.id.padUnlocked);
        ImageView padLocked = (ImageView)findViewById(R.id.padLocked);
        Button arm = (Button)findViewById(R.id.ArmButton);
        //Button change = (Button)findViewById(R.id.passcodeButton);
        TextView notify = (TextView)findViewById(R.id.textView2);

        if(armed == true){
            padLocked.setVisibility(View.VISIBLE);
            padUnlocked.setVisibility(View.INVISIBLE);
            arm.setVisibility(View.INVISIBLE);
            //change.setVisibility(View.INVISIBLE);
        }

        if(armed == false){
            padLocked.setVisibility(View.INVISIBLE);
            padUnlocked.setVisibility(View.VISIBLE);
            arm.setVisibility(View.VISIBLE);
            //change.setVisibility(View.VISIBLE);
        }

        if(tripped == true){
            notify.setVisibility((View.VISIBLE));
        }

        if(tripped == false || armed == false){
            notify.setVisibility((View.INVISIBLE));
            tripped = false;
        }

    }

    public void particleLogin(){

        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                //  LOG IN TO PARTICLE
                try {
                    // Log in to Particle Cloud using username and password
                    ParticleCloudSDK.getCloud().logIn("40344772@live.napier.ac.uk", "Monokuma7!");
                    return "Logged in!";
                }
                catch(ParticleCloudException e) {
                    Log.e(TAG, "Error logging in: " + e.toString());
                    return "Error logging in!";
                }
            }

            protected void onPostExecute(String msg) {
                // Show Toast containing message from doInBackground
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();

    }

    public void armedDevice() {
        Button arm = (Button) findViewById(R.id.ArmButton);
        arm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        //  LOG IN TO PARTICLE
                        try {
                            // Log in to Particle Cloud using username and password
                            ParticleCloudSDK.getCloud().publishEvent("Arming Device", "true", ParticleEventVisibility.PUBLIC, 60);
                            return "Event Sent!";
                        } catch (ParticleCloudException e) {
                            Log.e(TAG, "Error logging in: " + e.toString());
                            return "Failed Event!";
                        }
                    }

                    protected void onPostExecute(String msg) {
                        // Show Toast containing message from doInBackground
                        Toaster.s(MainActivity.this, msg);
                    }
                }.execute();
                 armed = true;
                 UiSetup(armed);
            }
        });
    }

    public void particleSubscribe(){
            new AsyncTask<Void, Void, String>() {
                protected String doInBackground(Void... params) {
                    //  LOG IN TO PARTICLE
                    try {
                        // Log in to Particle Cloud using username and password
                        ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(null,
                                new ParticleEventHandler() {
                                    public void onEventError(Exception e) {
                                        Log.e(TAG, "Event error: ", e);
                                    }

                                    public void onEvent(String eventName, ParticleEvent event) {
                                        Log.i(TAG, "Received event with payload: " + event.dataPayload);
                                        System.out.println("Before if");
                                        if (eventName.equals("Disarm-Alarm")){
                                            runOnUiThread(new Runnable(){
                                                @Override
                                                public void run(){
                                                    System.out.println("Disarmed");
                                                    armed = false;
                                                    UiSetup(armed);
                                                }
                                            });
                                        }

                                        if (eventName.equals("deviceTripped")){
                                            runOnUiThread(new Runnable(){
                                                @Override
                                                public void run(){
                                                    notification();
                                                    tripped = true;
                                                    UiSetup(tripped);
                                                }
                                            });
                                        }


                                        if (eventName.equals("potentialBreak")){
                                            runOnUiThread(new Runnable(){
                                                @Override
                                                public void run(){
                                                    notification2();
                                                }
                                            });
                                        }

                                    }
                                });
                        return "Event Triggered!";
                    }
                    catch(IOException e) {
                        Log.e(TAG, "Error logging in: " + e.toString());
                        return "Failed Event Disarm!";
                    }
                }

                protected void onPostExecute(String msg) {
                    // Show Toast containing message from doInBackground
                    Toaster.s(MainActivity.this, msg);
                }
            }.execute();

        }

       /* public void changePasscode() {
            Button change = (Button)findViewById(R.id.passcodeButton);
            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(MainActivity.this, Passcode.class);
                    startActivity(intent1);
                }
            });

    }*/

    public void checkDevice() {

                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        //  LOG IN TO PARTICLE
                        try {
                            // Log in to Particle Cloud using username and password
                            ParticleDevice myDevice = ParticleCloudSDK.getCloud().getDevice("e00fce6889688c900d20511d");
                            int aInt = myDevice.getIntVariable("deviceCheck");
                            int notify = myDevice.getIntVariable("notify");
                            if(aInt == 0){
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run(){
                                        armed = false;
                                        UiSetup(armed);
                                    }
                                });
                            }
                            else{
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run(){
                                        armed = true;
                                        UiSetup(armed);
                                    }
                                });
                            }

                            if(notify == 0){
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run(){
                                        tripped = false;
                                        UiSetup(tripped);
                                    }
                                });
                            }
                            else{
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run(){
                                        tripped = true;
                                        UiSetup(tripped);
                                    }
                                });
                            }

                            return "reset!";
                        } catch (ParticleCloudException | IOException | ParticleDevice.VariableDoesNotExistException e) {
                            Log.e(TAG, "Error logging in: " + e.toString());
                            return "Failed Event Load!";
                        }
                    }

                    protected void onPostExecute(String msg) {
                        // Show Toast containing message from doInBackground
                        Toaster.s(MainActivity.this, msg);
                    }
                }.execute();


            }
    public void notification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "devicePing")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentTitle("Alarm Tripped")
                .setContentText("Your Security System Has Been Tripped")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());

    }

    public void notification2(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "devicePing")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentTitle("Alarm Tripped")
                .setContentText("It has been sometime with no password please contact the police")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());

    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "devicePing";
            String description = "Ping to phone";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("devicePing", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}