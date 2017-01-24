package com.hadrami.oumar.cooksta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mCooksDatabase;
    private DatabaseReference mOrdersDatabase;
    private Query mQueryCurrentCookOrders;
    String currentCookUID;

    StringBuilder ItemsNameandQty;

    AlertDialog.Builder completeOrderDialog;

    TextView cookNameTextView;
    TextView cookMobileNumberTextView;
    ImageView cookImage;
    String fullName;
    String mobileNumber;

    private RecyclerView mOrdersList;
    private ListView mSummaryList;
    ArrayList<Summary> ArrayListSummary;

    String cookImageURL;


    DatabaseReference orderDetailsReference;
    DatabaseReference orderItemsReference;

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Lucci","6");
        mAuth.addAuthStateListener(mAuthListener);
        Log.i("Lucci","7");


        FirebaseRecyclerAdapter<Order, OrdersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, OrdersViewHolder>(
                Order.class,
                R.layout.orderhistorycard,
                OrdersViewHolder.class,
                mQueryCurrentCookOrders

        ) {
            @Override
            protected void populateViewHolder(final OrdersViewHolder viewHolder, Order model, int position) {

                final String itemKey = getRef(position).getKey();

                viewHolder.setDatee(model.getDateee());
                viewHolder.setTotalPricee(model.getTotalPriceee());
                viewHolder.setTotalTimee(model.getTotalTimeee());
                viewHolder.setInstructionss(model.getInstructionssss());

                viewHolder.optionsImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showOrderOptions(viewHolder.optionsImageButton,itemKey);
                    }
                });



                orderDetailsReference = FirebaseDatabase.getInstance().getReference("Orders").child(itemKey);
                orderItemsReference = orderDetailsReference.child("items");
                ItemsNameandQty = new StringBuilder();
                orderItemsReference.limitToFirst(10).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ItemDataSnapShot : dataSnapshot.getChildren()) {

                            ItemsNameandQty.append(ItemDataSnapShot.child("name").getValue() + "(" + ItemDataSnapShot.child("qty").getValue() + ")" + "\n");

                        }

                        viewHolder.setItmess("" + ItemsNameandQty);
                        ItemsNameandQty.setLength(0);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.viewOrderDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOrderDetails(itemKey);
                    }
                });
            }

        };

        mOrdersList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showOrderOptions(ImageButton optionsImageButton, final String itemKey) {

        final AlertDialog.Builder completeOrderDialog = new AlertDialog.Builder(this);
        PopupMenu optionPopup = new PopupMenu(optionsImageButton.getContext(), optionsImageButton);
        final MenuInflater inflater = optionPopup.getMenuInflater();
        inflater.inflate(R.menu.order_options, optionPopup.getMenu());
        optionPopup.show();

        optionPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.chat_with_customer :
                        Intent chatIntent = new Intent(MainActivity.this , ChatActivity.class);
                        chatIntent.putExtra("itemKey",itemKey);
                        startActivity(chatIntent);
                        return true;
                    case R.id.complete_order:

                        completeOrderDialog.setMessage("Are you sure?")
                                .setTitle("Order completed!")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                     // add code for updating order status in database and push notification to customer
                                        Toast.makeText(getApplicationContext(),"Order completed",Toast.LENGTH_LONG).show();

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






    private void showOrderDetails(String itemKey) {

        AlertDialog.Builder orderDetailsDiaolg = new AlertDialog.Builder(MainActivity.this);


        final View DetailsCard = LayoutInflater.from(MainActivity.this).inflate(R.layout.order_details_card, null);
        orderDetailsDiaolg.setView(DetailsCard);
        AlertDialog alertDialog = orderDetailsDiaolg.create();
        alertDialog.getWindow().setLayout(500, 400); //Controlling width and height.
        alertDialog.show();




        DatabaseReference orderDatabase = FirebaseDatabase.getInstance().getReference().child("Orders").child(itemKey);
        orderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String SummaryItemTotalPrice = (String) dataSnapshot.child("totalPrice").getValue();
                String SummaryItemOrderBill = (String) dataSnapshot.child("orderbill").getValue();
                String SummaryItemCess = (String) dataSnapshot.child("cess").getValue();
                String SummaryItemSwachh = (String) dataSnapshot.child("swachh").getValue();
                TextView summaryItemTotalPrice = (TextView) DetailsCard.findViewById(R.id.summary_item_totalprice);
                TextView summaryItemOrderBill = (TextView) DetailsCard.findViewById(R.id.summary_orderbill);
                TextView summaryItemCess = (TextView) DetailsCard.findViewById(R.id.summary_cess);
                TextView summaryItemSwachh = (TextView) DetailsCard.findViewById(R.id.summary_swachh);
                summaryItemTotalPrice.setText(SummaryItemTotalPrice);
                summaryItemOrderBill.setText(SummaryItemOrderBill);
                summaryItemCess.setText(SummaryItemCess);
                summaryItemSwachh.setText(SummaryItemSwachh);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.i("Hero", "0");
        ArrayListSummary = new ArrayList<Summary>();
        mSummaryList = (ListView) DetailsCard.findViewById(R.id.summary_list);
        DatabaseReference nqptDatabse = FirebaseDatabase.getInstance().getReference().child("Orders").child(itemKey).child("items");
        nqptDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String SummaryItemName = (String) itemSnapshot.child("name").getValue();
                    String SummaryItemPrice = (String) itemSnapshot.child("price").getValue();
                    String SummaryItemQty = (String) itemSnapshot.child("qty").getValue();
                    String SummaryItemTime = (String) itemSnapshot.child("time").getValue();
                    ArrayListSummary.add(new Summary(SummaryItemName,SummaryItemPrice,SummaryItemQty,SummaryItemTime));


                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SummaryAdapter adapter = new SummaryAdapter(getApplicationContext() , ArrayListSummary);
        mSummaryList.setAdapter(adapter);
        ArrayListSummary.clear();




    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .build()
        );

Log.i("Lucci","1");
        completeOrderDialog = new AlertDialog.Builder(this);
        mCooksDatabase = FirebaseDatabase.getInstance().getReference().child("Cooks");
        mOrdersDatabase = FirebaseDatabase.getInstance().getReference().child("Orders");

        Log.i("Lucci","2");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.i("Lucci","3");
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent LoginOrsignupIntent = new Intent(MainActivity.this, LoginOrSignupActivity.class);
                    LoginOrsignupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(LoginOrsignupIntent);
                }

            }
        };
        Log.i("Lucci","4");
        mAuth = FirebaseAuth.getInstance();
        currentCookUID = mAuth.getCurrentUser().getUid();
        Log.i("Lucci","5");
        mQueryCurrentCookOrders=mOrdersDatabase.orderByChild("cookUID").equalTo(currentCookUID);


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mOrdersList = (RecyclerView) findViewById(R.id.order_list);
        mOrdersList.setHasFixedSize(true);
        mOrdersList.setLayoutManager(mLayoutManager);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Display the name and the mobile number of the cook inthe navigation drawer header!!!
        View header = navigationView.getHeaderView(0);
        cookNameTextView = (TextView) header.findViewById(R.id.cook_name_nav);
        cookMobileNumberTextView = (TextView) header.findViewById(R.id.cook_number_nav);
        cookImage = (ImageView)header.findViewById(R.id.cookkk_image) ;
        mCooksDatabase.child(currentCookUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                fullName = (String) dataSnapshot.child("FullName").getValue();
                mobileNumber = (String) dataSnapshot.child("Mobile Number").getValue();
                cookImageURL =(String) dataSnapshot.child("Image").getValue();
                Log.i("URL","   "+cookImageURL);
                if(cookImageURL!=null)
                    Picasso.with(MainActivity.this).load(cookImageURL).into(cookImage);
                cookNameTextView.setText("" + fullName);
                cookMobileNumberTextView.setText("" + mobileNumber);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static class OrdersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView viewOrderDetails;
        ImageButton optionsImageButton;
        public OrdersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            viewOrderDetails = (TextView) mView.findViewById(R.id.order_details);
            optionsImageButton = (ImageButton)mView.findViewById(R.id.order_chat_complete_overflow);
        }

        public void setDatee(String Name) {
            TextView orderDate = (TextView) mView.findViewById(R.id.order_date);
            orderDate.setText(Name);
        }

        public void setTotalPricee(String Description) {
            TextView orderTotalPrice = (TextView) mView.findViewById(R.id.order_totalprice);
            orderTotalPrice.setText(Description);
        }

        public void setItmess(String itmes) {
            TextView orderItems = (TextView) mView.findViewById(R.id.order_item);
            orderItems.setText(itmes);
        }

        public void setTotalTimee(String totalTime) {
            TextView orderTotalTime = (TextView) mView.findViewById(R.id.order_totaltime);
            orderTotalTime.setText(totalTime);
        }

        public void setInstructionss(String instructions) {
            TextView orderTotalTime = (TextView) mView.findViewById(R.id.order_instructions);
            orderTotalTime.setText(instructions);
        }


    }


    public static class SummaryAdapter extends ArrayAdapter<Summary> {


        public SummaryAdapter(Context context, ArrayList<Summary> summaries) {
            super(context, 0, summaries);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Check if an existing view is being reused, otherwise inflate the view
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.name_qty_price_time, parent, false);
            }

            Summary summary = getItem(position);
            TextView itemName = (TextView) listItemView.findViewById(R.id.summary_item_name);
            itemName.setText(summary.getNameee());


            TextView itemPrice = (TextView) listItemView.findViewById(R.id.summary_item_price);
            itemPrice.setText(summary.getPriceee());


            TextView itemDescription = (TextView) listItemView.findViewById(R.id.summary_item_qty);
            itemDescription.setText(summary.getQtyyy());


            TextView itemTime = (TextView) listItemView.findViewById(R.id.summary_item_time);
            itemTime.setText(summary.getTimeee());

            return listItemView;
        }
    }







    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.orders) {
            // Handle the camera action
        } else if (id == R.id.menu) {

            startActivity(new Intent(MainActivity.this,MenuActivity.class));

        } else if (id == R.id.myaccount) {

            startActivity(new Intent(MainActivity.this,MyAccountActivity.class));

        }else if (id == R.id.nav_share) {

        } else if (id == R.id.help) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
