package com.hadrami.oumar.cooksta;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginOrSignupActivity extends AppCompatActivity {

    TextView cookstaTextView;
    Button loginButton;
    Button TosignupButton;
    EditText emailEditText;
    EditText passwordEditText;

    TextView signupcookstaTextview;
    TextView loginTextView;
    Button signupButton;
    EditText fullname;
    EditText mobileNumberEditText;
    EditText emailUp ;
    EditText passwordUp ;
    EditText confirmPassword;
    LinearLayout loginpageLayout, signuppageLayout;
    CheckBox terms;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    GPSTracker gps;
    Geocoder geocoder;
    List<Address> addresses;
    double latitude;
    double longitude;

    String finalAddress ;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_signup);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .build()
        );

        geocoder = new Geocoder(this);

        mAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Cooks");
        mProgress = new ProgressDialog(this);

        loginpageLayout = (LinearLayout) findViewById(R.id.login_page);
        cookstaTextView = (TextView) findViewById(R.id.textview);
        loginButton = (Button) findViewById(R.id.login_button);
        emailEditText = (EditText) findViewById(R.id.email_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);


        TosignupButton = (Button) findViewById(R.id.signup_button);
        signupButton = (Button)findViewById(R.id.signup);
        signuppageLayout = (LinearLayout) findViewById(R.id.signup_page);
        signupcookstaTextview = (TextView) findViewById(R.id.signupcooksta);
        loginTextView = (TextView) findViewById(R.id.existlogin);
        fullname = (EditText)findViewById(R.id.fullname);
        mobileNumberEditText =(EditText)findViewById(R.id.mobile_number);
        emailUp = (EditText)findViewById(R.id.signup_email_edittext);
        passwordUp = (EditText)findViewById(R.id.signup_password_edittext);
        confirmPassword = (EditText)findViewById(R.id.signup_password_confirm_edittext);
        terms = (CheckBox)findViewById(R.id.terms);

        TosignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginpageLayout.setVisibility(View.GONE);
                signuppageLayout.setVisibility(View.VISIBLE);
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signuppageLayout.setVisibility(View.GONE);
                loginpageLayout.setVisibility(View.VISIBLE);
            }
        });

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/lickerlione.ttf");
        cookstaTextView.setTypeface(custom_font);
        signupcookstaTextview.setTypeface(custom_font);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gps = new GPSTracker(LoginOrSignupActivity.this);

                if(gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();
                        finalAddress = address + " " +city + " "
                                + state+" "  +country+" "  +postalCode+" " +knownName;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    gps.showSettingsAlert();
                }

                startSigningUp();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin();
            }
        });

    }

    private void startLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
        {
            mProgress.setMessage("Logging In..");
            mProgress.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        Intent MainIntent = new Intent(LoginOrSignupActivity.this,MainActivity.class);
                        MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(MainIntent);
                        mProgress.dismiss();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Error Loging In",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void startSigningUp() {
        final String fullnamee = fullname.getText().toString();
        String emailup = emailUp.getText().toString();
        String passwordup = passwordUp.getText().toString();
        String confirmpass = confirmPassword.getText().toString();
        final String mobileNumber = mobileNumberEditText.getText().toString();


        if(!TextUtils.isEmpty(fullnamee) && !TextUtils.isEmpty(emailup) &&
                !TextUtils.isEmpty(passwordup) && !TextUtils.isEmpty(confirmpass) && !TextUtils.isEmpty(mobileNumber))
        {
            if(passwordup.equals(confirmpass))
            {
                mProgress.setMessage("Signing Up...");
                mProgress.show();

                mAuth.createUserWithEmailAndPassword(emailup , passwordup).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if( task.isSuccessful() )
                        {
                            String cook_id =mAuth.getCurrentUser().getUid();
                            DatabaseReference current_cook_db = mDatabase.child(cook_id);
                            current_cook_db.child("FullName").setValue(fullnamee);
                            current_cook_db.child("Mobile Number").setValue(mobileNumber);
                            current_cook_db.child("Address").setValue(finalAddress);
                            current_cook_db.child("Longitude").setValue(longitude);
                            current_cook_db.child("Latitude").setValue(latitude);
                            current_cook_db.child("UID").setValue(cook_id);
                            current_cook_db.child("Rating").setValue("0");

                            mProgress.dismiss();
                            Intent MainIntent = new Intent(LoginOrSignupActivity.this,MainActivity.class);
                            MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(MainIntent);

                        }
                    }
                });
            }

        }
        else
            Toast.makeText(getApplicationContext(),"Fill all the fields",Toast.LENGTH_LONG).show();

    }
}
