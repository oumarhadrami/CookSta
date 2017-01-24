package com.hadrami.oumar.cooksta;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

public class MyAccountActivity extends AppCompatActivity {

    TextView cookNameTextView;
    TextView cookMobileNumberTextView;
    TextView cookAddressTextView;
    String fullName,mobileNumber,address;
    String currentCookUID;
    FirebaseAuth mAuth;
    DatabaseReference mCooksDatabase;
    CardView logoutCardView;
    ImageButton editImageButton;
    ImageView cookImage;
    private Uri mImageUri = null;
    private static final int GALLERY_REQUEST=1;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    String cookImageURL;
    private StorageReference mStorage;
    StorageReference filepath;
    Uri downloadUrl;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .build()
        );

            cookNameTextView = (TextView)findViewById(R.id.cookname);
            cookMobileNumberTextView = (TextView)findViewById(R.id.cook_number);
            cookAddressTextView = (TextView)findViewById(R.id.cook_address);
            logoutCardView = (CardView)findViewById(R.id.logout_card);
            editImageButton = (ImageButton)findViewById(R.id.edit_info);
            cookImage = (ImageView)findViewById(R.id.cook_image);
            mAuth = FirebaseAuth.getInstance();
            currentCookUID = mAuth.getCurrentUser().getUid();
            mCooksDatabase = FirebaseDatabase.getInstance().getReference("Cooks");
            mStorage = FirebaseStorage.getInstance().getReference();





        mCooksDatabase.child(currentCookUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                fullName = (String) dataSnapshot.child("FullName").getValue();
                mobileNumber = (String) dataSnapshot.child("Mobile Number").getValue();
                address = (String) dataSnapshot.child("Address").getValue();
                cookImageURL =(String) dataSnapshot.child("Image").getValue();
                Log.i("URL","   "+cookImageURL);
                if(cookImageURL!=null)
                    Picasso.with(MyAccountActivity.this).load(cookImageURL).into(cookImage);
                cookNameTextView.setText("" + fullName);
                cookMobileNumberTextView.setText("" + mobileNumber);
                cookAddressTextView.setText(""+address);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.i("URL","   "+cookImageURL);
        logoutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MyAccountActivity.this,LoginOrSignupActivity.class));
            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
        cookImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                cookImage.setImageURI(resultUri);
                mImageUri = resultUri;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
            cookImage.setImageURI(mImageUri);
        }

        filepath = mStorage.child("Cook_Images").child(getRandomString(25));
        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloadUrl = taskSnapshot.getDownloadUrl();
                mCooksDatabase.child(currentCookUID).child("Image").setValue(downloadUrl.toString());
            }
        });

    }


    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }



}
