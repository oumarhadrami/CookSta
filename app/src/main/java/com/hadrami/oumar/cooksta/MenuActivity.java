package com.hadrami.oumar.cooksta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView mMenu;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private Query mQueryCurrentCookItems;

    AlertDialog.Builder removeAlert;
    String currentCookUID;


    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Hero","0");
        FirebaseRecyclerAdapter<Item,ItemViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(
                Item.class,
                R.layout.item_card,
                ItemViewHolder.class,
                mQueryCurrentCookItems
        ) {
            @Override
            protected void populateViewHolder(final ItemViewHolder viewHolder, Item model, final int position) {
                final String itemKey = getRef(position).getKey();
                Log.i("Hero","1");
                viewHolder.setNamee(model.getNameee());
                viewHolder.setDescriptionn(model.getDescriptionnn());
                viewHolder.setPricee(model.getPriceee());
                viewHolder.setImage(getApplicationContext(),model.getImageee());


                viewHolder.optionsImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showItemOptions(viewHolder.optionsImageButton,itemKey);
                    }
                });


            }
        };

        mMenu.setAdapter(firebaseRecyclerAdapter);
    }

    private void showItemOptions(ImageButton optionsImageButton, final String itemKey) {

        PopupMenu optionPopup = new PopupMenu(optionsImageButton.getContext(),optionsImageButton);
        final MenuInflater inflater = optionPopup.getMenuInflater();
        inflater.inflate(R.menu.item_options , optionPopup.getMenu());
        optionPopup.show();
        optionPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.edit_item :
                        Intent itemEditIntent = new Intent(MenuActivity.this , AddItemActivity.class);
                        itemEditIntent.putExtra("itemKey",itemKey);
                        startActivity(itemEditIntent);
                        return true;
                    case R.id.delete_item:

                        removeAlert.setMessage("Are you sure?")
                                .setTitle("Delete item")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDatabase.child(itemKey).removeValue();

                                    }
                                })
                                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                        return true;
                }


                return false;
            }
        });

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .build()
        );

        removeAlert = new AlertDialog.Builder(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MenuActivity.this,AddItemActivity.class);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mDatabase = FirebaseDatabase.getInstance().getReference().child("Items");
        mAuth = FirebaseAuth.getInstance();
        currentCookUID = mAuth.getCurrentUser().getUid();
        mQueryCurrentCookItems = mDatabase.orderByChild("UID").equalTo(currentCookUID);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMenu = (RecyclerView)findViewById(R.id.menu_list);
        mMenu.setHasFixedSize(true);
        mMenu.setLayoutManager(mLayoutManager);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder
    {
        ImageButton optionsImageButton;
        View mView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            mView =itemView;
           optionsImageButton = (ImageButton)mView.findViewById(R.id.item_optionssss);
        }



        public void setNamee(String Name){
            TextView itemName = (TextView)mView.findViewById(R.id.item_name);
            itemName.setText(Name);
        }

        public void setDescriptionn(String Description)
        {
            TextView itemDescription =(TextView)mView.findViewById(R.id.item_Description);
            itemDescription.setText(Description);
        }

        public void setPricee(String Price)
        {
            TextView itemPrice =(TextView)mView.findViewById(R.id.item_price);
            itemPrice.setText(Price);
        }

        public void setImage(Context context ,String Image)
        {
            ImageView itemImage =(ImageView)mView.findViewById(R.id.item_image);
            Picasso.with(context).load(Image).into(itemImage);


        }
    }

}
