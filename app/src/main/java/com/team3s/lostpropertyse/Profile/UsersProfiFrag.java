package com.team3s.lostpropertyse.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team3s.lostpropertyse.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.Post.EditActivity;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Share;


import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class UsersProfiFrag extends Fragment {

    private StorageReference storageReference;
    private StorageReference backgroundStorageReference;
    private ImageButton headerCover,exit;
    private TextView u_fullname,u_username,u_usernameprof,u_about,u_city;
    private ImageView profileImg;
    private View backgroundView;
    public TextView follower_counter,num_post;

    private ImageButton editprof;

    private StorageReference mStorageImage;

    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private RecyclerView profileList;

    private DatabaseReference database,mDatabaseUsers,databaseFollowers,mDatabaseUsersPostNum;
    private DatabaseReference mDatabaseCurrentUsers;
    private Query mQueryUser;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private Uri mImageUri = null;

    TabLayout tlUserProfileTabs;
    public UsersProfiFrag() {
        // Required empty public constructor
    }



  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.user_profil_frag, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
       FirebaseStorage storage = FirebaseStorage.getInstance();
       storageReference = storage.getReference();
       backgroundStorageReference = storageReference.child("background_images");
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent loginIntent = new Intent(getActivity(),TabsHeaderActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };

        database = FirebaseDatabase.getInstance().getReference().child("Icerik");
        database.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
       // databaseFollowers = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Takip_Edilenler");
        mDatabaseUsersPostNum = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("PostsId");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");


        u_fullname = (TextView) v.findViewById(R.id.fullnameuser);
        u_username = (TextView) v.findViewById(R.id.usernameprof);
        u_city = (TextView) v.findViewById(R.id.city);
        num_post = (TextView) v.findViewById(R.id.find_counter);
        backgroundView = v.findViewById(R.id.background);

        String currentUserId = auth.getCurrentUser().getUid();
        mDatabaseCurrentUsers = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mQueryUser = mDatabaseCurrentUsers.orderByChild("uid").equalTo(currentUserId);

        profileImg = (ImageView) v.findViewById(R.id.ivUserProfilePhoto);

        profileList = (RecyclerView) v.findViewById(R.id.rvUserProfile);

        editprof = (ImageButton) v.findViewById(R.id.edit_prof_btn);
        editprof.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            showDialog();
          }
        });


        mQueryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    num_post.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mDatabaseUsers.child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Glide.with(getActivity().getApplicationContext())
                        .load(String.valueOf(snapshot.getValue()))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CircleTransform(getActivity()))
                        .animate(R.anim.shake)
                        .into(profileImg);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mDatabaseUsers.child("namesurname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                u_fullname.setText(String.valueOf(snapshot.getValue()));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mDatabaseUsers.child("username").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                u_username.setText("@"+String.valueOf(snapshot.getValue())+",");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mDatabaseUsers.child("fullAddress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String fullad = String.valueOf(snapshot.getValue());        //tam adres içinden şehir ve ülke yi yazıyorum.
                String textStr[] = fullad.split("/");
                u_city.setText(textStr[1]);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        profileList.setHasFixedSize(true);
        profileList.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);

        return v;
    }


    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bir işlem seç")
                .setItems(R.array.editButtonOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        System.out.println("*************"+which);
                        switch (which){
                            case 0: //background Pic
                                break;
                            case 1: changeProfilePicture();
                                break;
                            case 2: signOut();
                                break;
                            default: break;
                        }

                    }
                });
        builder.create();
        builder.show();
    }
    public void changeProfilePicture(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                mImageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(mImageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profileImg.setImageBitmap(selectedImage);
                StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
                if (mImageUri != null) {
                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String downloadUri = taskSnapshot.getDownloadUrl().toString();
                            mDatabaseUsers.child("profileImage").setValue(downloadUri);
                        }
                    });
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Bir sorun oluştu", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getActivity(), "Resim Seçmedin",Toast.LENGTH_LONG).show();
        }
    }


    //sign out method
    public void signOut() {
        auth.signOut();
        Intent intent = new Intent(getActivity(), TabsHeaderActivity.class);
        startActivity(intent);
    }

    public void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Share, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Share, ShareViewHolder>(
                Share.class,
                R.layout.profile_row,
                ShareViewHolder.class,
                mQueryUser
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, Share model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setQuestions(model.getQuestions());
                viewHolder.setPost_image(getActivity().getApplicationContext(),model.getPost_image());
                viewHolder.setPost_date(model.getPost_date());
                viewHolder.setPost_time(model.getPost_time());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editActivity = new Intent(getActivity(), EditActivity.class);
                        editActivity.putExtra("post_id", post_key);
                        startActivity(editActivity);
                    }
                });

            }
        };

        profileList.setAdapter(firebaseRecyclerAdapter);

    }


    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
    }


    private void startSetupAccount() {


        final String user_id = auth.getCurrentUser().getUid();

        StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
        if (mImageUri != null) {

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);
                    Glide.with(getActivity()).load(downloadUri).centerCrop().into(profileImg);
                    profileImg.setImageURI(mImageUri);

                }
            });
        }
    }


    public static class ShareViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public ShareViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
        }


        public void setQuestions(String questions){

            TextView questions_title = (TextView) mView.findViewById(R.id.titleProfileText);
            questions_title.setText(questions);
        }
        public void setPost_date(String post_date){

            TextView date = (TextView) mView.findViewById(R.id.dateTxt);
            date.setText(post_date);
        }
        public void setPost_time(String post_time){

            TextView time = (TextView) mView.findViewById(R.id.timeTxt);
            time.setText(post_time);
        }

        public void setPost_image(Context ctx, String post_image){
            ImageView post_img = (ImageView) mView.findViewById(R.id.post_img);
            //Picasso.with(ctx).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).fit().centerCrop().into(post_img);
            Glide.with(ctx)
                    .load(post_image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.shake)
                    .into(post_img);
        }

    }

}
