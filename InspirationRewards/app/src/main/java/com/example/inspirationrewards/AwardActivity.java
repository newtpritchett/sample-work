package com.example.inspirationrewards;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AwardActivity extends AppCompatActivity {
    private UserInfo userInfo;
    private UserInfo currentUserInfo;
    private String userName;
    private TextView username;
    private String password;
    private String studentId;
    private TextView department;
    private TextView position;
    private TextView story;
    private TextView pointsReceived;
    private EditText comments;
    private TextView charLength;
    private EditText score;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award);

        userInfo = (UserInfo) getIntent().getSerializableExtra("targetedUserDetails");
        currentUserInfo = (UserInfo) getIntent().getSerializableExtra("currentUserInfo");

        userName = currentUserInfo.getUsername();
        password = currentUserInfo.getPassword();
        studentId = currentUserInfo.getStudentID();

        setTitle();
        setParameters();
    }

    public void onClickSave() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String body = "Add rewards for " + userInfo.getFirst_name() + " " + userInfo.getLast_name() + "?";
        builder.setTitle("Reward point quantity");
        builder.setMessage(body);
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF0397E6);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF0397E6);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_profile_menu, menu);
        return true;
    }

    private void setParameters() {
        username = findViewById(R.id.username);
        username.setText(userInfo.getLast_name() + ", " + userInfo.getFirst_name());

        pointsReceived =findViewById(R.id.pointsReceived);
        pointsReceived.setText(String.valueOf(userInfo.getPoints()));

        department = findViewById(R.id.department);
        department.setText(userInfo.getDepartment());

        position = findViewById(R.id.positionLabel);
        position.setText(userInfo.getPosition());

        story = findViewById(R.id.story);
        story.setText(userInfo.getStory());

        comments = findViewById(R.id.comments);
        charLength = findViewById(R.id.charLength);

        score = findViewById(R.id.score);

        userImage = findViewById(R.id.userImage);
        Bitmap bitmap = convertImage(userInfo.getImage());
        userImage.setImageBitmap(bitmap);


        comments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence seq, int start, int count, int after) {
                int charCount = seq.toString().length();
                String textLength = "( " + charCount + " of 80 )";
                charLength.setText(textLength);
            }

            @Override
            public void onTextChanged(CharSequence seq, int start, int before, int count) {
                int charCount = seq.toString().length();
                String textLength = "( " + charCount + " of 80 )";
                charLength.setText(textLength);
            }

            @Override
            public void afterTextChanged(Editable str) {
                int charCount = str.toString().length();
                String textLength = "( " + charCount + " of 80 )";
                charLength.setText(textLength);
            }
        });

    }

    public void setTitle() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(userInfo.getFirst_name() + " " + userInfo.getLast_name());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_with_logo);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_icon:
                if(checkPoints())
                    onClickSave();
                else
                    createToast(this, "Insufficient Points", Toast.LENGTH_LONG);
                return true;
            case android.R.id.home:
                Intent data = new Intent();
                setResult(RESULT_OK, data);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkPoints() {
        if(currentUserInfo.getPoints_to_award() > Integer.parseInt(score.getText().toString()))
            return true;
        return false;
    }

    public void doAsyncTaskCall() {
        String targetStudentId = userInfo.getStudentID();
        String targetUsername = userInfo.getUsername();
        String targetName = currentUserInfo.getFirst_name() + " " + currentUserInfo.getLast_name();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String targetDate = simpleDateFormat.format(new Date());
        String targetNote = comments.getText().toString();

        String targetValue = score.getText().toString();
        String[] parameter = new String[] {targetStudentId,targetUsername,targetName,
                targetDate,targetNote,targetValue,
                userName, password, studentId};

        AwardAsyncTask awardAsyncTask = new AwardAsyncTask(this);
        awardAsyncTask.execute(parameter);
    }

    public Bitmap convertImage(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static void createToast(Context context, String message, int time) {
        Toast toast = Toast.makeText(context, "" + message, time);
        View toastView = toast.getView();
        toastView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        TextView tv = toast.getView().findViewById(android.R.id.message);
        tv.setPadding(120, 80, 120, 80);
        tv.setTextColor(0xFF0397E6);
        toast.show();
    }


    public void toActivity(String connectionResult) {


        Intent intent = new Intent();
        intent.putExtra("currentUserInfo", currentUserInfo);
        setResult(RESULT_OK, intent);
        createToast(this,"Added Reward", Toast.LENGTH_LONG);
        finish();
    }



}