package com.yukisoft.yellowpixels.JavaActivities.Home.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.AddAccountActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.ChatActivity;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ChatAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ChatModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ChatModelFull;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {
    private CollectionReference businessRef = FirebaseFirestore.getInstance().collection(CollectionName.USERS);
    private CollectionReference chatRef = FirebaseFirestore.getInstance().collection(CollectionName.CHATS);

    private ArrayList<UserModel> BusinessList = new ArrayList<>();
    private ArrayList<ChatModel> ChatList = new ArrayList<>();
    private ArrayList<ChatModelFull> displayChatList = new ArrayList<>();

    private ChatAdapter chatAdapter;
    private UserModel currentUser;

    private ProgressBar loading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        Intent i = getActivity().getIntent();
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);

        ConstraintLayout userChat = v.findViewById(R.id.userChat);
        userChat.setVisibility(View.GONE);

        if (currentUser == null){
            startActivity(new Intent(getContext(), AddAccountActivity.class));
            getActivity().finish();
        } else {
            userChat.setVisibility(View.VISIBLE);
            loading = v.findViewById(R.id.loading);
            getBusinesses();
            buildRecyclerView(v);
        }

        return v;
    }

    

    private void getBusinesses() {
        displayChatList = new ArrayList<>();
        BusinessList = new ArrayList<>();
        ChatList = new ArrayList<>();

        businessRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null){
                for(DocumentSnapshot msg : queryDocumentSnapshots){
                    UserModel tempUser = msg.toObject(UserModel.class);
                    assert tempUser != null;
                    tempUser.setId(msg.getId());

                    boolean exists = false;

                    for (UserModel m : BusinessList)
                        if(m.getId().equals(tempUser.getId()))
                            exists = true;

                    if(!exists)
                        BusinessList.add(tempUser);
                }

                chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null){
                                ArrayList<ChatModel> tempChatList = new ArrayList<>();
                                for(DocumentSnapshot msg : queryDocumentSnapshots){
                                    ChatModel chatModel = msg.toObject(ChatModel.class);
                                    assert chatModel != null;
                                    chatModel.setId(msg.getId());

                                    boolean exists = false;

                                    for (ChatModel m : tempChatList)
                                        if(m.getId().equals(chatModel.getId()))
                                            exists = true;

                                    if(!exists)
                                        tempChatList.add(chatModel);
                                }

                                for (ChatModel c : tempChatList){
                                    if (c.getId().replace("_", " ").contains(currentUser.getId())) {
                                        ChatModelFull chat = new ChatModelFull();
                                        chat.setId(c.getId());
                                        chat.setLastMessageDate(c.getLastMessageDate());
                                        chat.setMessages(c.getMessages());

                                        for (UserModel u : BusinessList)
                                            if (u.getId().equals(c.getId().replace(currentUser.getId(), "")
                                                    .replace("_", ""))) {
                                                chat.setBusiness(u);
                                                break;
                                            }

                                        if (chat.getBusiness() != null) {
                                            ChatList.add(c);
                                            displayChatList.add(chat);
                                        }
                                    }
                                }
                            }
                            //Collections.sort(displayBusinessList, new AlphabetComparator());
                            //Collections.sort(MTList, new AlphabetComparator());

                            checkList();

                            chatAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void buildRecyclerView(View v){

        RecyclerView chatRecyclerView = v.findViewById(R.id.chatsRecycler);
        chatRecyclerView.setHasFixedSize(false);
        chatAdapter = new ChatAdapter(getContext(), displayChatList);
        RecyclerView.LayoutManager  chatLayoutManager =  new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(chatLayoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
        chatAdapter.setOnItemClickListener(position -> {
            ChatModelFull chat = displayChatList.get(position);
            UserModel business = chat.getBusiness();

            startActivity(new Intent(getActivity(), ChatActivity.class)
                    .putExtra(MainActivity.CHAT, (new Gson()).toJson(chat))
                    .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser))
                    .putExtra(MainActivity.BUSINESS, (new Gson()).toJson(business)));
        });
    }

    private void checkList(){
        if (displayChatList.isEmpty())
            loading.setVisibility(View.VISIBLE);
        else
            loading.setVisibility(View.GONE);
    }
}
