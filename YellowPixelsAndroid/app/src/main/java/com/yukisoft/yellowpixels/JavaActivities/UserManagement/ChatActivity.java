package com.yukisoft.yellowpixels.JavaActivities.UserManagement;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.MessageAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.AlphabetComparator;
import com.yukisoft.yellowpixels.JavaRepositories.BackgroundServices.Services.MessagingService;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ChatModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ChatModelFull;
import com.yukisoft.yellowpixels.JavaRepositories.Models.MessageModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.MessageModelFull;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    ArrayList<MessageModelFull> messages = new ArrayList<>();
    UserModel business, currentUser = null;
    MessageAdapter messageAdapter;

    ImageView btnSend;
    EditText chatInput;
    private String chatId;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent i = getIntent();
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);
        ChatModelFull chat = (new Gson()).fromJson(i.getStringExtra(MainActivity.CHAT), ChatModelFull.class);

        TextView txtBusinessName = findViewById(R.id.txtBusinessName);
        TextView txtBusinessType = findViewById(R.id.txtBusinessType);
        ImageView imgBack = findViewById(R.id.btnBack);
        ImageView imgBusinessPic = findViewById(R.id.imgBusinessPic);

        buildRecyclerView();

        if (chat != null) {
            business = chat.getBusiness();
            chatId = chat.getId();

            for (MessageModel m : chat.getMessages()){
                MessageModelFull messageModelFull = new MessageModelFull();

                messageModelFull.setMessage(m.getMessage());
                messageModelFull.setTimeSent(m.getTimeSent());

                // TODO: 2020/01/24 GET TIME SENT

                if (m.getFrom().equals(business.getId())){
                    messageModelFull.setFrom(business);
                } else if (m.getFrom().equals(currentUser.getId())){
                    messageModelFull.setFrom(currentUser);
                }
                messages.add(messageModelFull);
            }
            messageAdapter.notifyDataSetChanged();
        } else {
            business = (new Gson()).fromJson(i.getStringExtra(MainActivity.BUSINESS), UserModel.class);
            ArrayList<String> id = new ArrayList<>();
            id.add(business.getId());
            id.add(currentUser.getId());
            Collections.sort(id, new AlphabetComparator());
            chatId = id.get(0) + "_" + id.get(1);

            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            ff.collection(CollectionName.CHATS).document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        ChatModel tempChat = documentSnapshot.toObject(ChatModel.class);

                        if (tempChat != null) {
                            for (MessageModel m : Objects.requireNonNull(tempChat).getMessages()) {
                                UserModel from = null;
                                if (m.getFrom().equals(business.getId()))
                                    from = business;
                                else if (m.getFrom().equals(currentUser.getId()))
                                    from = currentUser;

                                if (from != null) {
                                    MessageModelFull tempMsg = new MessageModelFull(m.getMessage(), from, m.getTimeSent());
                                    messages.add(tempMsg);
                                }
                            }
                        }

                        messageAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error - " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to chat!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (business == null) {
            Toast.makeText(this, "Error\nUnable to load recipient!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (currentUser.getId().equals(business.getId())) {
            Toast.makeText(this, "You cannot send messages to yourself!", Toast.LENGTH_SHORT).show();
            finish();
        }

        txtBusinessName.setText(business.getName());
        
        if (business.isVerified())
            txtBusinessType.setText("Verified");
        else
            txtBusinessType.setText("Unverified");

        imgBack.setOnClickListener(v -> finish());
        Picasso.with(ChatActivity.this)
                .load(currentUser.getDpURI())
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_person)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imgBusinessPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(ChatActivity.this)
                                .load(currentUser.getDpURI())
                                .resize(500, 500)
                                .centerCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_business)
                                .into(imgBusinessPic, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                });

        Log.d("ID_CHAT", business.getId() + " - " + currentUser.getId());

        btnSend = findViewById(R.id.btnSend);
        chatInput = findViewById(R.id.chatInput);
        btnSend.setOnClickListener(v -> {
            String input = chatInput.getText().toString();

            if (input.isEmpty())
                return;

            MessageModelFull msg = new MessageModelFull(input, currentUser, new Date());
            messages.add(msg);

            ArrayList<MessageModel> messageModels = new ArrayList<>();
            for (MessageModelFull m : messages) {
                MessageModel msgUpload = new MessageModel(m.getMessage(), m.getFrom().getId(), m.getTimeSent());
                messageModels.add(msgUpload);
            }

            ChatModel chatModel = new ChatModel(business.getId(), msg.getTimeSent(), messageModels);

            chatModel.setId(chatId);

            messageAdapter.notifyDataSetChanged();
            chatInput.setText("");

            Intent serviceIntent = new Intent(this, MessagingService.class);
            serviceIntent.putExtra("chat", (new Gson().toJson(chatModel)));
            ContextCompat.startForegroundService(this, serviceIntent);
        });
    }

    private void buildRecyclerView(){
        RecyclerView messageRecycler = findViewById(R.id.messageRecycler);
        messageRecycler.setHasFixedSize(false);
        messageAdapter = new MessageAdapter(getBaseContext(), messages);
        RecyclerView.LayoutManager  chatLayoutManager =  new LinearLayoutManager(ChatActivity.this);
        messageRecycler.setLayoutManager(chatLayoutManager);
        messageRecycler.setAdapter(messageAdapter);
        messageAdapter.setOnItemClickListener(position -> {
            MessageModelFull chat = messages.get(position);
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
