package com.hadrami.oumar.cooksta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends AppCompatActivity {

    ImageButton messageSend;
    EditText messageText;
    DatabaseReference messagesDatabase ;
    DatabaseReference mCooksDatabase;
    DatabaseReference newMessage;
    FirebaseAuth mAuth;
    String currentCookUID;
    String itemKey;
    String fullName;
    String isMe;

    LinearLayoutManager mLayoutManager;


    RecyclerView MessageList;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .build()
        );



        messageSend = (ImageButton) findViewById(R.id.message_send);
        messageText = (EditText) findViewById( R.id.message_text);

        MessageList = (RecyclerView) findViewById(R.id.chat_list);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(false);
        MessageList.setHasFixedSize(true);
        MessageList.setLayoutManager(mLayoutManager);



        mAuth = FirebaseAuth.getInstance();
        currentCookUID = mAuth.getCurrentUser().getUid();


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            itemKey = getIntent().getExtras().getString("itemKey");
            messagesDatabase = FirebaseDatabase.getInstance().getReference("Orders").child(itemKey).child("messages");


        }




        messageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessageTo(currentCookUID);


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i("Hero","1");
        final FirebaseRecyclerAdapter<Chat,MessageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, MessageViewHolder>(
                Chat.class,
                R.layout.message_card_right,
                MessageViewHolder.class,
                messagesDatabase
        ) {


            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Chat model, final int position) {
                Log.i("Hero","2");
                viewHolder.setTextt(model.getText());
                isMe = model.getUid();


                Log.i("Herooo     "+isMe,currentCookUID);
                if(isMe!=null && !isMe.equals(currentCookUID)) {
                    viewHolder.messageLinearLayout.setGravity(Gravity.START);
                    viewHolder.itemDescription.setTextColor(getResources().getColor(android.R.color.black));
                    viewHolder.messageCardView.setBackgroundResource(R.drawable.bubble_outt);
                }
                else
                {
                    viewHolder.messageLinearLayout.setGravity(Gravity.END);
                    viewHolder.itemDescription.setTextColor(getResources().getColor(android.R.color.white));
                    viewHolder.messageCardView.setBackgroundResource(R.drawable.bubble_in);
                }



            }

        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    MessageList.scrollToPosition(positionStart);
                }
            }
        });

        MessageList.setAdapter(firebaseRecyclerAdapter);
        MessageList.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                MessageList.smoothScrollToPosition(firebaseRecyclerAdapter.getItemCount());
            }
        });

        Log.i("Hero",""+firebaseRecyclerAdapter.getItemCount());




    }

    private void sendMessageTo(String currentCookUID) {

        final String MessageText = messageText.getText().toString();

        if (!TextUtils.isEmpty(MessageText))
        {
            messageSend.setEnabled(true);
            newMessage=messagesDatabase.push();
            newMessage.child("name").setValue(fullName);
            newMessage.child("text").setValue(MessageText);
            newMessage.child("uid").setValue(currentCookUID);
            messageText.getText().clear();

        }
        else
        {
            messageSend.setEnabled(false);
        }



    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View mView;
        LinearLayout messageLinearLayout;
        CardView messageCardView;
        TextView itemDescription;


        public MessageViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            messageLinearLayout = (LinearLayout) mView.findViewById(R.id.chattttt_card);
            itemDescription =(TextView)mView.findViewById(R.id.sender_text_right);
            messageCardView = (CardView) mView.findViewById(R.id.carddddd);

        }



        public void setTextt(String Text)
        {

            itemDescription.setText(Text);
        }
    }


  /*  public static class MessageViewHolderLeft extends RecyclerView.ViewHolder {
        View mView;
        LinearLayout messageLinearLayout;
        CardView messageCardView;
        TextView itemDescription;


        public MessageViewHolderLeft(View itemView) {
            super(itemView);

            mView = itemView;
            messageLinearLayout = (LinearLayout) mView.findViewById(R.id.chattttt_card_left);
            itemDescription =(TextView)mView.findViewById(R.id.sender_text_left);
            messageCardView = (CardView) mView.findViewById(R.id.carddddd_left);

        }



        public void setTextt(String Text)
        {

            itemDescription.setText(Text);
        }
    }
*/
}
