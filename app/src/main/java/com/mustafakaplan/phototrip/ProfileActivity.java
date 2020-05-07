package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.PicassoProvider;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
{
    static boolean photoDelete = true;
    static String currentEmail="";
    static boolean updatePhoto=false;
    String showUser;
    String ppUrl = "null";
    String imageName = "";

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    static boolean updateActivity = false;
    static boolean deletePost = false;
    static ArrayList<String> deleteDoc = new ArrayList<>();
    static ArrayList<String> updateDoc = new ArrayList<>();
    private Map<String, Object> docData;

    private FirebaseFirestore firebaseFirestore;

    ProfileRecycleAdapter profileRecycleAdapter;

    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;
    static ArrayList<String> userAddressFromFB;
    static ArrayList<String> userLatitudeFromFB;
    static ArrayList<String> userLongitudeFromFB;
    static ArrayList<String> documentIdFromFB;

    TextView nameText;
    TextView aboutText;
    ImageView profilePhoto;
    Button editButton;
    Button archiveButton;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        showUser = intent.getStringExtra("showUser");

        nameText = findViewById(R.id.nameText);
        aboutText = findViewById(R.id.aboutText);
        profilePhoto = findViewById(R.id.profilePhoto);
        profilePhoto.setImageResource(R.drawable.user);
        editButton = findViewById(R.id.editButton);
        archiveButton = findViewById(R.id.archiveButton);
        recyclerView = findViewById(R.id.recyclerProfileView);

        userCommentFromFB = new ArrayList<>();
        userImageFromFB = new ArrayList<>();
        userAddressFromFB = new ArrayList<>();
        userLatitudeFromFB = new ArrayList<>();
        userLongitudeFromFB = new ArrayList<>();
        documentIdFromFB = new ArrayList<>();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        if(deletePost)
        {
            deletePost();
            deletePost = false;

            startActivity(getIntent());
            finish();
        }

        if(updateActivity)
        {
            updatePost();
            updateActivity = false;

            startActivity(getIntent());
            finish();
        }

        getDataFromFirestore();
        getDataFromFirestore2();

        RecyclerView recyclerView = findViewById(R.id.recyclerProfileView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        profileRecycleAdapter = new ProfileRecycleAdapter(userCommentFromFB,userImageFromFB,userAddressFromFB, documentIdFromFB);

        recyclerView.setAdapter(profileRecycleAdapter);

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(updatePhoto)
        {
            getDataFromFirestore2();
            updatePhoto = false;
        }
    }

    public void followUser(View view)
    {

    }

    @Override
    // geri tuşuna bastığında
    public void onBackPressed()
    {
        Intent intent = new Intent(ProfileActivity.this, FeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
        startActivity(intent);
        finish();
    }

    public void deletePost()
    {
        for(String docs : deleteDoc)
        {
            getImageNameFromFirestore(docs);

            firebaseFirestore.collection("Posts").document(docs).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid)
                {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    System.out.println(e);
                }
            });

        }

        deleteDoc.clear();
    }

    public void updatePost()
    {
        docData = new HashMap<>();
        docData.put("visibility", "true");
        FeedActivity.updateAct = true;

        for(String docs : updateDoc)
        {
            firebaseFirestore.collection("Posts").document(docs).update(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid)
                {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    System.out.println(e);
                }
            });
        }

        updateDoc.clear();

    }

    public void goArchive(View view)
    {
        Intent intentToArchive = new Intent(ProfileActivity.this, ArchiveActivity.class);
        startActivity(intentToArchive);
    }

    public void editProfile(View view)
    {
        EditProfileActivity.oldName = nameText.getText().toString();
        EditProfileActivity.oldAbout = aboutText.getText().toString();

        if(!ppUrl.matches("null"))
        {
            EditProfileActivity.oldImage = ppUrl;
        }

        else
        {
            EditProfileActivity.oldImage = "null";
        }

        Intent intentToEditProfile = new Intent(ProfileActivity.this,EditProfileActivity.class);
        startActivity(intentToEditProfile);
    }

    public void getDataFromFirestore()
    {
        CollectionReference collectionReference = firebaseFirestore.collection("Posts");

        collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    Toast.makeText(ProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            String userEmail = (String) data.get("useremail");

                            if(showUser == null)
                            {
                                if(userEmail.matches(currentEmail)) // Profilde sadece kendi fotoğraflarını göster
                                {
                                    String visibility = (String) data.get("visibility");

                                    if(visibility.matches("true"))
                                    {
                                        String comment = (String) data.get("comment");
                                        String downloadUrl = (String) data.get("downloadurl");
                                        String address = (String) data.get("address");
                                        String latitude = (String) data.get("latitude");
                                        String longitude = (String) data.get("longitude");
                                        String documentId = snapshot.getId();

                                        userCommentFromFB.add(comment);
                                        userImageFromFB.add(downloadUrl);
                                        userAddressFromFB.add(address);
                                        userLatitudeFromFB.add(latitude);
                                        userLongitudeFromFB.add(longitude);
                                        documentIdFromFB.add(documentId);

                                        profileRecycleAdapter.notifyDataSetChanged();
                                    }

                                }
                            }

                            else
                            {
                                photoDelete = true;

                                if(!showUser.matches(currentEmail)) // Başka Profile Bakılınca Düzenle ve Arşivi Kaldır
                                {
                                    editButton.setVisibility(View.INVISIBLE);
                                    archiveButton.setVisibility(View.INVISIBLE);
                                    photoDelete = false;
                                }

                                if(userEmail.matches(showUser)) // Başka Kişinin Profili
                                {
                                    String visibility = (String) data.get("visibility");

                                    if(visibility.matches("true"))
                                    {
                                        String comment = (String) data.get("comment");
                                        String downloadUrl = (String) data.get("downloadurl");
                                        String address = (String) data.get("address");
                                        String latitude = (String) data.get("latitude");
                                        String longitude = (String) data.get("longitude");
                                        String documentId = snapshot.getId();

                                        userCommentFromFB.add(comment);
                                        userImageFromFB.add(downloadUrl);
                                        userAddressFromFB.add(address);
                                        userLatitudeFromFB.add(latitude);
                                        userLongitudeFromFB.add(longitude);
                                        documentIdFromFB.add(documentId);

                                        profileRecycleAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

    }

    public void getImageNameFromFirestore(final String docs)
    {
        CollectionReference collectionReference3 = firebaseFirestore.collection("Posts");

        collectionReference3.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    Toast.makeText(ProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            if(snapshot.getId().matches(docs))
                            {
                                imageName = (String) data.get("imagename");

                                // Resmi Storage'tan sil
                                storageReference.child(imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {

                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        System.out.println(e);
                                    }
                                });

                                break;
                            }
                        }
                    }
                }
            }
        });

    }

    public void getDataFromFirestore2()
    {
        CollectionReference collectionReference2 = firebaseFirestore.collection("Users");

        collectionReference2.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    Toast.makeText(ProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            String userEmail = (String) data.get("useremail");

                            if(showUser == null)
                            {
                                if(userEmail.matches(currentEmail)) // Profil fotoğrafını göster
                                {
                                    String fullName = (String) data.get("fullname");
                                    String aboutMe = (String) data.get("aboutme");
                                    ppUrl = (String) data.get("downloadurl");

                                    nameText.setText(fullName);
                                    aboutText.setText(aboutMe);

                                    if(!ppUrl.matches("null"))
                                    {
                                        Picasso.get().load(ppUrl).into(profilePhoto);
                                    }
                                    else
                                    {
                                        profilePhoto.setImageResource(R.drawable.user);
                                        nameText.setText("Ad Soyad");
                                        aboutText.setText("Hakkımda");
                                    }

                                    break;
                                }
                            }
                            else
                            {
                                if(!showUser.matches(currentEmail)) // Başka Profile Bakılınca Düzenleyi Kaldır
                                {
                                    editButton.setVisibility(View.INVISIBLE);
                                }

                                if(userEmail.matches(showUser))                                 {
                                    String fullName = (String) data.get("fullname");
                                    String aboutMe = (String) data.get("aboutme");
                                    ppUrl = (String) data.get("downloadurl");

                                    nameText.setText(fullName);
                                    aboutText.setText(aboutMe);

                                    if(!ppUrl.matches("null"))
                                    {
                                        Picasso.get().load(ppUrl).into(profilePhoto);
                                    }
                                    else
                                    {
                                        profilePhoto.setImageResource(R.drawable.user);
                                        nameText.setText("Ad Soyad");
                                        aboutText.setText("Hakkımda");
                                    }

                                    break;
                                }
                            }

                        }
                    }
                }
            }
        });
    }
}
