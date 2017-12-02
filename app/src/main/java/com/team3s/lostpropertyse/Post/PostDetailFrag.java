package com.team3s.lostpropertyse.Post;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.Chat.CommentFrag;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.MainPage.MainPage;
import com.team3s.lostpropertyse.Maps.PropMaps;
import com.team3s.lostpropertyse.Profile.UsersProfiFrag;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.UniversalImageLoader;

import java.io.File;

public class PostDetailFrag extends Fragment{

    private String post_key = null;
    private String post_type = null;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private ImageView mPostImage;
    private TextView mPostDesc;
    private TextView mPostCity;
    private TextView mPostCommentCounter;
    private Button mPostDelete;
    private Button mPostUpdate;
    private ImageButton comments;
    private Intent intent;

    private FirebaseAuth auth;

    private Button showMap;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";


    public PostDetailFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post_detail, container, false);

        auth = FirebaseAuth.getInstance();

        Bundle bundlecom = getArguments();                          //mainFragment ten post un keyini çekiyoruz.
        post_key = bundlecom.getString("post_id");
        post_type = bundlecom.getString("post_type");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_type);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");


        mPostImage = (ImageView) v.findViewById(R.id.post_image_btn);
        mPostDesc = (TextView) v.findViewById(R.id.post_descET);
        mPostCity = (TextView) v.findViewById(R.id.post_cityName);
        mPostCommentCounter = (TextView) v.findViewById(R.id.commentArticleCounter);
        mPostDelete = (Button) v.findViewById(R.id.deleteBtnSc);
        mPostUpdate = (Button) v.findViewById(R.id.editBtnSc);
        comments = (ImageButton) v.findViewById(R.id.btnArticleComments);
        showMap = (Button) v.findViewById(R.id.showMap);

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundleComment = new Bundle();
                bundleComment.putString("post_id_key",post_key);
                bundleComment.putString("post_type", post_type);

                CommentFrag fragmentCom = new CommentFrag();
                fragmentCom.setArguments(bundleComment);
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.detailfrag, fragmentCom, TAG_FRAGMENT)
                        .addToBackStack(null)
                        .commit();

            }
        });
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(getActivity(), PropMaps.class);
                editActivity.putExtra("post_id", post_key);
                editActivity.putExtra("post_type", post_type);
                startActivity(editActivity);
            }
        });

        mPostDelete.setVisibility(View.GONE);
        mPostUpdate.setVisibility(View.GONE);
        mDatabase.child(post_key).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    mPostCommentCounter.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_desc = (String) dataSnapshot.child("questions").getValue();
                String post_img = (String) dataSnapshot.child("post_image").getValue();
                String post_id = (String) dataSnapshot.child("uid").getValue();
                String post_city = (String) dataSnapshot.child("city").getValue();


                mPostDesc.setText(post_desc);
                mPostCity.setText(post_city);

                Glide.with(getActivity().getApplicationContext())
                        .load(post_img)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .animate(R.anim.shake)
                        .into(mPostImage);


                if(auth.getCurrentUser().getUid().equals(post_id)){
                    mPostDelete.setVisibility(View.VISIBLE);
                    mPostUpdate.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPostDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(post_key).removeValue();
                mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                MainPage fragmentMain = new MainPage();
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_fragmentholder, fragmentMain,TAG_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return v;
    }

}