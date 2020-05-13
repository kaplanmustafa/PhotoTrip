package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArchiveActivity extends AppCompatActivity
{
    ArchiveRecyclerAdapter archiveRecyclerAdapter;

    int control = 0;

    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;
    static ArrayList<String> userAddressFromFB;
    static ArrayList<String> userLatitudeFromFB;
    static ArrayList<String> userLongitudeFromFB;
    static ArrayList<String> documentIdFromFB;
    static boolean updateAct = false;

    static ArrayList<String> deleteDoc = new ArrayList<>();;
    static boolean deleteItem = false;
    private Map<String, Object> docData;

    private FirebaseFirestore firebaseFirestore;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        PhotoView photoView = (PhotoView) findViewById(R.id.recyclerview_row_imageview);

        userCommentFromFB = new ArrayList<>();
        userImageFromFB = new ArrayList<>();
        userAddressFromFB = new ArrayList<>();
        userLatitudeFromFB = new ArrayList<>();
        userLongitudeFromFB = new ArrayList<>();
        documentIdFromFB = new ArrayList<>();

        firebaseFirestore = FirebaseFirestore.getInstance();

        if(deleteItem)
        {
            deletePost();
            deleteItem = false;
            FeedActivity.deleteItem = false;
            FeedActivity.updateAct = true;
        }

        getDataFromFirestore();

        recyclerView = findViewById(R.id.recyclerArchiveView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        archiveRecyclerAdapter = new ArchiveRecyclerAdapter(userCommentFromFB,userImageFromFB,userAddressFromFB, documentIdFromFB);
        recyclerView.setAdapter(archiveRecyclerAdapter);
    }

    @Override
    // geri tuşuna bastığında
    public void onBackPressed()
    {
        control = 1;
        Intent intent = new Intent(ArchiveActivity.this, ProfileActivity.class);
        startActivity(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
        finish();
    }

    public void deletePost()
    {
        docData = new HashMap<>();
        docData.put("visibility", "false");

        for(String docs : deleteDoc)
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

        deleteDoc.clear();
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
                    Toast.makeText(ArchiveActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            String userEmail = (String) data.get("useremail");

                                if(userEmail.matches(ProfileActivity.currentEmail)) // Profilde sadece kendi fotoğraflarını göster
                                {
                                    String visibility = (String) data.get("visibility");

                                    if(visibility.matches("false") && control == 0)
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

                                        archiveRecyclerAdapter.notifyDataSetChanged();
                                    }

                                }

                        }
                    }
                }
            }
        });

    }
}
