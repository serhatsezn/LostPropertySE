package com.team3s.lostpropertyse.Chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmMessage extends AppCompatActivity {

    private LinearLayout layout;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;
    private TextView dmUserNameTxt;
    private DatabaseReference reference1,reference2,mDatabaseToken,mDatabaseUserName;
    private String receiver_name;
    private String senderName;
    private String receiver_uid;
    private String receiverToken;
    private String sender_uid;
    private String dmtxt;
    private String title;

    private String userkeyfornotif;
    private String userschatnamekey;
    public FirebaseUser user;
    public String currentUserId;
    public FirebaseAuth auth;

    private String PREFS = "MyPrefs" ;
    SharedPreferences mPrefs;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        dmUserNameTxt = (TextView)findViewById(R.id.dmUserNameTxt);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        mDatabaseUserName = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mPrefs = getSharedPreferences(PREFS, 0);
        senderName = mPrefs.getString("username", "");
        sender_uid = mPrefs.getString("sender_uid", "");

        Intent bundlecom = getIntent();
        receiver_name = bundlecom.getStringExtra("receiver_name");
        receiver_uid = bundlecom.getStringExtra("receiver_uid");
        title = bundlecom.getStringExtra("title");

        reference2 = FirebaseDatabase.getInstance().getReference().child("messages").child(receiver_name+"_"+senderName);


        if(receiver_name == null) {
            receiver_name = mPrefs.getString("receiver_name", "");
            receiver_name = receiver_name.replaceAll("\\s+", "");
            receiver_name.toLowerCase();
            senderName = senderName.replaceAll("\\s+", "");
            senderName.toLowerCase();
            dmUserNameTxt.setText(receiver_name);

            reference1 = FirebaseDatabase.getInstance().getReference().child("messages").child(senderName+"_"+receiver_name);
            reference2 = FirebaseDatabase.getInstance().getReference().child("messages").child(receiver_name+"_"+senderName);
            reference1.child("readornot").setValue("Okundu");
        }else{

            reference2.child("senderUid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    receiver_uid = String.valueOf(dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            reference1 = FirebaseDatabase.getInstance().getReference().child("messages").child(senderName+"_"+receiver_name);
            reference2 = FirebaseDatabase.getInstance().getReference().child("messages").child(receiver_name+"_"+senderName);
            reference1.child("readornot").setValue("Okundu");

            dmUserNameTxt.setText(receiver_name);
        }


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(receiver_uid != null) {
                    mDatabaseToken = FirebaseDatabase.getInstance().getReference().child("Users").child(receiver_uid);
                    mDatabaseToken.child("token").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            receiverToken = String.valueOf(snapshot.getValue());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                dmtxt = messageArea.getText().toString();
                if(!dmtxt.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", dmtxt);
                    map.put("user", senderName);
                    map.put("sender_uid", sender_uid);
                    map.put("receiver_uid", receiver_uid);
                    reference1.push().setValue(map);
                    reference1.child("readornot").setValue("Okunmadı");
                    reference1.child("lastmsg").setValue(dmtxt);

                    if(sender_uid!=null) {
                        reference1.child("senderUid").setValue(sender_uid);
                        reference1.child("receiver_uid").setValue(receiver_uid);
                    }
                    Map<String, String> maprece = new HashMap<String, String>();
                    maprece.put("message", dmtxt);
                    maprece.put("user", senderName);
                    maprece.put("sender_uid", receiver_uid);
                    maprece.put("receiver_uid", sender_uid);
                    reference2.push().setValue(maprece);
                    reference2.child("readornot").setValue("Okunmadı");
                    reference2.child("lastmsg").setValue(dmtxt);

                    if(sender_uid!=null) {
                        reference2.child("senderUid").setValue(receiver_uid);
                        reference2.child("receiver_uid").setValue(sender_uid);
                    }

                    new Send().execute();                           //başarılı ise notification gönderme
                }

                messageArea.getText().clear();
            }
        });
        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String message = String.valueOf(dataSnapshot.child("message").getValue());
                String userName = String.valueOf(dataSnapshot.child("user").getValue());
                if(message != "null") {
                    if (userName.equals(senderName)) {
                        addMessageBox(/*"" +*/ message, 1);
                    } else {
                        addMessageBox(/*receiver_name + "" +*/ message, 2);
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(DmMessage.this);
        textView.setText(message);

        if(type == 1) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(100, 0, 0, 10);
            lp.gravity = Gravity.RIGHT;
            textView.setLayoutParams(lp);
            textView.setBackgroundResource(R.drawable.chatgreen);
        }
        else{
            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpp.setMargins(0, 0, 100, 10);
            lpp.gravity = Gravity.LEFT;
            textView.setLayoutParams(lpp);
            textView.setBackgroundResource(R.drawable.chatgray);
        }
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
    class Send extends AsyncTask<String, Void,Long > {          //burası notification kısmı.
        protected Long doInBackground(String... urls) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://aydinserhatsezen.com/fcm/LostP/lpdm.php");

            try {

                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("dm", dmtxt));
                nameValuePairs.add(new BasicNameValuePair("userName", senderName));         //ben
                nameValuePairs.add(new BasicNameValuePair("sender_name", senderName));         //ben
                nameValuePairs.add(new BasicNameValuePair("tokeNDevice", receiverToken));
                nameValuePairs.add(new BasicNameValuePair("receiver_name", sender_uid)); //alıcı

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(Long result) {
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DmMessage.this, BottomBarActivity.class);
        startActivity(intent);
    }

}