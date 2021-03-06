package com.team3s.lostpropertyse.Chat;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.AdapterClass;
import com.team3s.lostpropertyse.Profile.AnotherUsersProfiFrag;
import com.team3s.lostpropertyse.Profile.UsersProfiFrag;
import com.team3s.lostpropertyse.Utils.CircleTransform;
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
import java.util.List;

public class CommentFrag extends Fragment {
    private EditText cevap;
    private ImageButton cevaponay;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private DatabaseReference database;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseAnswer;
    private DatabaseReference mDatabasePost;
    private DatabaseReference mDatabasePToken;

    private RecyclerView cevapList;

    private String post_key = null;
    private String post_type = null;

    private String tokenUser = null;
    private String nameFuser = null;
    private String mUID = null;
    private String currentUID = null;

    public String user_key = null;
    public String cevap_val;
    public String currentUid;

    public static Typeface type;

    private SharedPreferences sharedpreferences;
    public static final String PREFS = "MyPrefs" ;
    private static String themeStr;

    public RelativeLayout commentrelative;

    public CommentFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_comment, container, false);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        currentUid = currentUser.getUid();
        cevapList = (RecyclerView) v.findViewById(R.id.comment_list);
        cevap = (EditText) v.findViewById(R.id.edtx_comment);
        cevaponay = (ImageButton) v.findViewById(R.id.comment_submit);
        progressBar = (ProgressBar) v.findViewById(R.id.news_cevap_progressBar);
        commentrelative = (RelativeLayout) v.findViewById(R.id.commentrelative);


        sharedpreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        themeStr = sharedpreferences.getString("theme", "DayTheme");          //eğer null ise DayTheme
        if(themeStr.equals("NightTheme")){
            commentrelative.setBackgroundColor(Color.parseColor("#142634"));
            cevap.setHintTextColor(Color.WHITE);
            cevap.setTextColor(Color.WHITE);

        }else if(themeStr.equals("DayTheme")){
            commentrelative.setBackgroundColor(Color.parseColor("#FFFFFF"));
            cevap.setHintTextColor(Color.BLACK);
            cevap.setTextColor(Color.BLACK);

        }

        Bundle bundlecom = getArguments();                          //mainFragment ten post un keyini çekiyoruz.
        post_key = bundlecom.getString("post_id_key");
        post_type = bundlecom.getString("post_type");

        type = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Ubuntu-B.ttf");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        mDatabaseAnswer = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_type).child(post_key).child("Comments");
        database = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mDatabasePToken = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_type).child(post_key);
        mDatabasePToken.addValueEventListener(new ValueEventListener() {            //postun sahibinin token bilgisini çekiyoruz.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tokenUser = (String) dataSnapshot.child("token").getValue();
                mUID = (String) dataSnapshot.child("uid").getValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        cevapList.setLayoutManager(layoutManager);
        cevaponay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cevaponay.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startPosting();
            }
        });
        return v;
    }

    private void startPosting() {
        cevap_val = cevap.getText().toString().trim();
        if (!TextUtils.isEmpty(cevap_val)) {
            final DatabaseReference newCevap = mDatabaseAnswer.push();      //veritabanına push işlemi
            final Time today = new Time(Time.getCurrentTimezone());         //posta verilen cavabın zamanını çekmek için.
            today.setToNow();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {               //verilen cevabın veritabanına yazılması
                    nameFuser = (String) dataSnapshot.child("username").getValue();
                    currentUID = currentUser.getUid();
                    newCevap.child("commentText").setValue(cevap_val);
                    newCevap.child("uid").setValue(currentUID);
                    newCevap.child("image").setValue(dataSnapshot.child("profileImage").getValue());
                    newCevap.child("post_time").setValue(today.format("%k:%M"));
                    newCevap.child("post_date").setValue(today.format("%d/%m/%Y"));
                    newCevap.child("commentUsername").setValue(nameFuser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                new Send().execute();                           //başarılı ise notification gönderme
                                cevap.getText().clear();
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            progressBar.setVisibility(View.GONE);
            cevaponay.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            cevaponay.setVisibility(View.VISIBLE);
        }
    }

    public void onStart() {         //bu bölümde o postun altında bulunan comment child ına ait cevapları yazdırmak için.
        super.onStart();

        FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder>(
                AdapterClass.class,
                R.layout.comment_row,
                ShareViewHolder.class,
                mDatabaseAnswer
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, AdapterClass model, final int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setcommentText(model.getcommentText());          //comment text
                viewHolder.setcommentUsername(model.getcommentUsername());  //comment user name
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());   //comment user image
                viewHolder.profileclick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseAnswer.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                user_key = (String) dataSnapshot.child("uid").getValue();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("USERKEY_SHARED", user_key);
                                editor.commit();

                                Bundle bundle = new Bundle();
                                bundle.putString("key", user_key); // User ID çekip anotherUserProfile ekranını açmak için
                                if (currentUid.equals(user_key)) {     //User ID ve CurrentUserID aynı ise kendi profil sayfasına gitmek için
                                    UsersProfiFrag fragment2 = new UsersProfiFrag();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.commentlinear, fragment2, "addUserProfile")
                                            .addToBackStack(null)
                                            .commit();
                                } else {
                                    AnotherUsersProfiFrag fragment3 = new AnotherUsersProfiFrag();
                                    fragment3.setArguments(bundle);
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.commentlinear, fragment3, "addAnotherProfile")
                                            .addToBackStack(null)
                                            .commit();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                            }
                        });

                    }
                });
            }
        };
        cevapList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ShareViewHolder extends RecyclerView.ViewHolder {       // burada bizim oluşturmuş olduğumuz comment_row layoutundaki bileşenleri kullandığımız yer
        View mViewRoad;
        FirebaseAuth mAuth;
        TextView mAnswer;
        TextView shaUsername;
        RelativeLayout profileclick;
        public ShareViewHolder(View itemView) {
            super(itemView);
            mViewRoad = itemView;
            mAuth = FirebaseAuth.getInstance();
            mAnswer = (TextView) mViewRoad.findViewById(R.id.commentTxt);
            shaUsername = (TextView) mViewRoad.findViewById(R.id.shaUsernameCom);
            profileclick = (RelativeLayout) mViewRoad.findViewById(R.id.profileclick);

            if(themeStr.equals("NightTheme")){
                mAnswer.setTextColor(Color.WHITE);
                shaUsername.setTextColor(Color.WHITE);
            }else if(themeStr.equals("DayTheme")){
                mAnswer.setTextColor(Color.BLACK);
                shaUsername.setTextColor(Color.BLACK);
            }


        }
        public void setcommentText(String commentText) {            //burada bulunan commentText ile firebasedeki child ın altındaki node aynı olmak zorunda ayrıca bunları AdapterClass.java classında tanımlıyoruz. get fonksiyonları share classdan çekiyoruz.
            mAnswer.setText(commentText);
        }
        public void setcommentUsername(String commentUsername) {

            shaUsername.setText(commentUsername);
            shaUsername.setTypeface(type);

        }
        public void setImage(Context ctx, String image) {
            ImageView user_Pic = (ImageView) mViewRoad.findViewById(R.id.user_profile_com);
            Glide.with(ctx)
                    .load(image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new CircleTransform(ctx))
                    .into(user_Pic);
        }
    }

    class Send extends AsyncTask<String, Void,Long > {          //burası notification kısmı.
        protected Long doInBackground(String... urls) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://aydinserhatsezen.com/fcm/LostP/lpyorum.php");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("tokendevice", tokenUser));
                nameValuePairs.add(new BasicNameValuePair("comment", cevap_val));
                nameValuePairs.add(new BasicNameValuePair("userName", nameFuser));
                nameValuePairs.add(new BasicNameValuePair("post_key", post_key));
                nameValuePairs.add(new BasicNameValuePair("post_type", post_type));

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
}