package com.hadrami.oumar.cooksta;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class AddItemActivity extends AppCompatActivity {

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private ImageButton mSelectImage;
    private EditText mDishName;
    private EditText mDishPrice;
    private EditText mTimeRequired;
    private EditText mDishDescription;
    private EditText mDishCuisine;
    private RadioGroup radioGroup;
    private RadioButton selectetCategory;
    RadioButton vegRadioButton;
    RadioButton nonvegRadioButton;
    private Button mAddItemButton;
    String dishImage;
    String cookName;
    String cookRating;

    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST=1;

    private StorageReference mStorage;
    StorageReference filepath;
    Uri downloadUrl;
    DatabaseReference mDataBase;
    DatabaseReference newItem;
    private ProgressDialog mProgress;
    DatabaseReference mCooksDatabase;


    String itemKey =null;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .build()
        );

        mDishName = (EditText) findViewById(R.id.dishname);
        mDishPrice = (EditText) findViewById(R.id.dishprice);
        mTimeRequired = (EditText) findViewById(R.id.dishtime);
        mDishDescription = (EditText) findViewById(R.id.dishDescription);
        mDishCuisine = (EditText) findViewById(R.id.dishCuisine);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        mAddItemButton=(Button) findViewById(R.id.additem);
        vegRadioButton =(RadioButton)findViewById(R.id.vegradiobutton);
        nonvegRadioButton =(RadioButton)findViewById(R.id.nonvegradiobutton);
        mSelectImage=(ImageButton)findViewById(R.id.imageselect);

        mSelectImage.bringToFront();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDataBase = FirebaseDatabase.getInstance().getReference().child("Items");
        mCooksDatabase = FirebaseDatabase.getInstance().getReference().child("Cooks");

        mCooksDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                 cookName = (String)dataSnapshot.child("FullName").getValue();
                 cookRating = (String)dataSnapshot.child("Rating").getValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }
        );



        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            itemKey = getIntent().getExtras().getString("itemKey");
            mDataBase.child(itemKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    dishImage = (String) dataSnapshot.child("Image").getValue();
                    String dishName = (String) dataSnapshot.child("Name").getValue();
                    String dishruppeePrice = (String) dataSnapshot.child("Price").getValue();
                    String parts[] = dishruppeePrice.split("₹ ");
                    String dishPrice = parts[1];
                    String dishTime = (String) dataSnapshot.child("Time Required").getValue();
                    String dishDescription = (String) dataSnapshot.child("Description").getValue();
                    String dishCuisine = (String)dataSnapshot.child("Cuisine").getValue();
                    String UID = (String) dataSnapshot.child("UID").getValue();
                    String dishCategory = (String) dataSnapshot.child("Category").getValue();
                    if(dishCategory.equals("veg"))
                        vegRadioButton.setChecked(true);
                    else
                        nonvegRadioButton.setChecked(true);

                    mDishName.setText(dishName);
                    mDishPrice.setText(dishPrice);
                    mTimeRequired.setText(dishTime);
                    mDishDescription.setText(dishDescription);
                    mDishCuisine.setText(dishCuisine);
                    Picasso.with(AddItemActivity.this).load(dishImage).into(mSelectImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        mProgress = new ProgressDialog(this);


        mSelectImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });




        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addAnItem();
            }
        });
    }

    private void addAnItem() {

        final String dishName = mDishName.getText().toString();
        final String dishPrice = mDishPrice.getText().toString();
        final String dishTime = mTimeRequired.getText().toString();
        final String dishDescription = mDishDescription.getText().toString();
        final String dishCuisine = mDishCuisine.getText().toString();



        if (mSelectImage.getDrawable() != null) {
            if (radioGroup.getCheckedRadioButtonId() !=-1 ) {
                int category = radioGroup.getCheckedRadioButtonId();
                selectetCategory = (RadioButton) findViewById(category);
                if (!TextUtils.isEmpty(dishName) && !TextUtils.isEmpty(dishPrice) && !TextUtils.isEmpty(dishTime)
                        && !TextUtils.isEmpty(dishDescription)) {

                   filepath = mStorage.child("Items_Images").child(getRandomString(25));
                    Log.i("mImageUri","key is null");
                    mProgress.setMessage("Adding Item to Menu..");
                    mProgress.show();
                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i("Kaku","2");
                            downloadUrl = taskSnapshot.getDownloadUrl();
                            if(itemKey==null)
                                newItem = mDataBase.push();
                            else
                                newItem =mDataBase.child(itemKey);
                            newItem.child("UID").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            newItem.child("Name").setValue(dishName);
                            newItem.child("Price").setValue(getResources().getString(R.string.Rs)+ " "+ dishPrice);
                            newItem.child("Image").setValue(downloadUrl.toString());
                            newItem.child("Time Required").setValue(dishTime);
                            newItem.child("Description").setValue(dishDescription);
                            newItem.child("Cuisine").setValue(dishCuisine);
                            newItem.child("Category").setValue(selectetCategory.getText());
                            newItem.child("CookName").setValue(cookName);
                            newItem.child("Rating").setValue(cookRating);
                            mProgress.dismiss();

                            Intent intent = new Intent(AddItemActivity.this,MenuActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }


                    });
                }
                else
                    Toast.makeText(getApplicationContext(), "Fill all the fields", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getApplicationContext(), "Pick a Category", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Choose an Image", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mSelectImage.setImageURI(resultUri);
                mImageUri = resultUri;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
            mSelectImage.setImageURI(mImageUri);
        }


    }


    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(mImageUri!=null || !TextUtils.isEmpty(mDishName.getText().toString()) || !TextUtils.isEmpty(mDishPrice.getText().toString())
                        && !TextUtils.isEmpty(mTimeRequired.getText().toString())
                        && !TextUtils.isEmpty(mDishDescription.getText().toString()) || radioGroup.getCheckedRadioButtonId() !=-1 ) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("Discard Changes?")
                            .setCancelable(false)
                            .setPositiveButton("DISCARD", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
                else {
                    finish();
                }return true;
        }
        return super.onOptionsItemSelected(item);
    }



}



