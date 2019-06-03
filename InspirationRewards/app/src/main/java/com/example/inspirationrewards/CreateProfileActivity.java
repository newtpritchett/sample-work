package com.example.inspirationrewards;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CreateProfileActivity extends AppCompatActivity {
    private ImageView profile_picture;
    private ImageView replace_image;
    private File img;
    private EditText username;
    private EditText password;
    private EditText first_name;
    private EditText last_name;
    private EditText department;
    private EditText position;
    private EditText your_story;
    private CheckBox admin;
    private TextView char_length;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private int MY_PERM_REQUEST_CODE = 12345;
    private static int MY_LOCATION_REQUEST_CODE = 329;

    String TAG = "CreateProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        profile_picture = findViewById(R.id.profilePicture);
        replace_image = findViewById(R.id.replaceImage);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        first_name = findViewById(R.id.firstName);
        last_name = findViewById(R.id.lastName);
        department = findViewById(R.id.department);
        position = findViewById(R.id.position);
        your_story = findViewById(R.id.yourStory);
        admin = findViewById(R.id.adminCheck);
        char_length = findViewById(R.id.charLength);

        setTitle();
        int charCount = your_story.getText().toString().length();
        String countText = "( " + charCount + " of 360 )";
        char_length.setText(countText);

        your_story.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int charCount = s.toString().length();
                String countText = "( " + charCount + " of 360 )";
                char_length.setText(countText);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int charCount = s.toString().length();
                String countText = "( " + charCount + " of 360 )";
                char_length.setText(countText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int charCount = s.toString().length();
                String countText = "( " + charCount + " of 360 )";
                char_length.setText(countText);
            }
        });

        checkGPSPermission();
    }

    @Override
    public void onBackPressed() {
        createToast(CreateProfileActivity.this, "New Profile Not Created", Toast.LENGTH_SHORT);
        finish();
    }

    private boolean checkPermission() {
        int reqPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (reqPermission != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERM_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkGPSPermission() {
        int reqPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (reqPermission != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == MY_PERM_REQUEST_CODE) {
            if (grantResults.length == 0) {
                return;
            }
            if (grantResults[0] == PERMISSION_GRANTED) {
                openPhotoDialog();
            } else {
                finish();
            }
        } else if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (grantResults.length == 0) {
                return;
            } else if (grantResults[0] == PERMISSION_GRANTED) {
                return;
            } else {
                finish();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_profile_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.replaceImage:
                onClickReplaceImageDialog();
                return true;
            case android.R.id.home:
                //createToast(CreateProfileActivity.this, "New Profile Not Created", Toast.LENGTH_SHORT);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doAsyncTaskCall() {

        UserInfo u = new UserInfo("A20392360",
                first_name.getText().toString(),
                last_name.getText().toString(),
                username.getText().toString(),
                department.getText().toString(),
                your_story.getText().toString(),
                position.getText().toString(),
                password.getText().toString(),
                1000,
                admin.isChecked(),
                getLocation(),
                BitMapToString(),
                new ArrayList<RewardsInfo>()
        );
        CreateProfileAsyncTask createProfileAsyncTask = new CreateProfileAsyncTask(u, this);
        createProfileAsyncTask.execute();
    }

    private String getLocation() {
        return getLocationString();
    }

    public String BitMapToString() {
        profile_picture.buildDrawingCache();
        Bitmap bitmap = profile_picture.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void setImage(View v) {
        boolean havePermission = checkPermission();
        if (havePermission) {
            openPhotoDialog();
        }
    }

    public void openPhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Picture");
        builder.setIcon(R.drawable.logo);
        builder.setMessage("Take picture from: ");
        builder.setNegativeButton("GALLERY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                accessGallery();
            }
        });
        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("CAMERA", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                accessCamera();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF018786);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF018786);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(0xFFE6AF4B);
    }

    public void onClickReplaceImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Changes?");
        builder.setIcon(R.drawable.logo);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doAsyncTaskCall();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF018786);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF018786);
    }

    public void accessCamera() {
        img = new File(getExternalCacheDir(), "appimage_" + System.currentTimeMillis() + ".jpg");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img));
        startActivityForResult(takePictureIntent, REQUEST_CAMERA);
    }

    public void accessGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    private void processCamera() {
        Uri selectedImage = Uri.fromFile(img);
        profile_picture.setImageURI(selectedImage);
        Bitmap bm = ((BitmapDrawable) profile_picture.getDrawable()).getBitmap();

        img.delete();
    }

    private void processGallery(Intent data) {
        Uri galleryImageUri = data.getData();
        if (galleryImageUri == null)
            return;

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(galleryImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final Bitmap userImage = BitmapFactory.decodeStream(imageStream);
        profile_picture.setImageBitmap(userImage);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
            try {
                processGallery(data);
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            try {
                processCamera();
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void setTitle() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.icon);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    public void redirectTOActivity(UserInfo userInfo) {
        createToast(CreateProfileActivity.this, "Profile Creation Success!", Toast.LENGTH_LONG);
        Intent intent = new Intent(CreateProfileActivity.this, ProfileActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    public void redirectTOActivity(String status, String message) {
        createToast(CreateProfileActivity.this, message, Toast.LENGTH_SHORT);
    }


    public static void createToast(Context context, String message, int time) {
        Toast toast = Toast.makeText(context, "" + message, time);
        View toastView = toast.getView();
        toastView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        TextView tv = toast.getView().findViewById(android.R.id.message);
        tv.setPadding(140, 75, 140, 75);
        tv.setTextColor(0xFFFFFFFF);
        toast.show();
    }
    private String displayAddresses(List<Address> addresses) {

        for (Address ad : addresses) {

            String addressString = String.format("%s, %s",
                    (ad.getLocality() == null ? "" : ad.getLocality()),
                    (ad.getAdminArea() == null ? "" : ad.getAdminArea()));

            return addressString;
        }
        return "";
    }

    public String getLocationString() {
        Criteria criteria;

        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String bestProvider = locationManager.getBestProvider(criteria, true);

            Location currentLocation = locationManager.getLastKnownLocation(bestProvider);
            if (currentLocation != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses;
                    addresses = geocoder.getFromLocation(currentLocation.getLatitude(),
                            currentLocation.getLongitude(), 10);
                    return displayAddresses(addresses);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }
        }
        return "";
    }


}
