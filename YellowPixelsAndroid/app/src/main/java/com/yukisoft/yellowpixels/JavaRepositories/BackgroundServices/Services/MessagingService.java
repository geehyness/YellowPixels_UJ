package com.yukisoft.yellowpixels.JavaRepositories.BackgroundServices.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ChatModel;
import com.yukisoft.yellowpixels.R;

import static com.yukisoft.yellowpixels.JavaRepositories.BackgroundServices.App.CHANNEL_ID;

public class MessagingService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ChatModel chatModel = (new Gson().fromJson(intent.getStringExtra(MainActivity.CHAT), ChatModel.class));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Yellow Pixels")
                .setContentText("Sending message")
                .setSmallIcon(R.drawable.ic_pixel)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        ff.collection(CollectionName.CHATS).document(chatModel.getId()).set(chatModel)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MessagingService.this, "Message sent!", Toast.LENGTH_SHORT).show();
                    stopSelf();
                })
                .addOnFailureListener(e -> Toast.makeText(MessagingService.this, "Unable to send message!\nCheck your internet connection.", Toast.LENGTH_SHORT).show());


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}