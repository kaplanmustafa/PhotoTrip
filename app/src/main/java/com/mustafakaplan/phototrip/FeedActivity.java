package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeedActivity<recyclerView> extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    String imageName = "";

    static ArrayList<String> deleteDoc = new ArrayList<>();
    static boolean updateAct = false;
    static boolean deleteItem = false;
    private Map<String, Object> docData;
    FirebaseUser firebaseUser;
    static ArrayList<String> deleteAccountId = new ArrayList<>();;

    ArrayList<String> userEmailFromFB;
    ArrayList<String> userIdFromFB;
    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;
    static ArrayList<String> userAddressFromFB;
    static ArrayList<String> userLatitudeFromFB;
    static ArrayList<String> userLongitudeFromFB;

    FeedRecyclerAdapter feedRecyclerAdapter;
    Intent intent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) // Menüyü Aktiviteye Bağlama
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.insta_options_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void deletePost()
    {
        docData = new HashMap<>();
        docData.put("visibility", "false");

        for(String docs : deleteDoc)
        {
            firebaseFirestore.collection("Posts").document(docs).update(docData).addOnSuccessListener(new OnSuccessListener<Void>()
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
        }

        deleteDoc.clear();
    }

    public void deleteAccount()
    {
        for(String docs : deleteAccountId)
        {
            System.out.println("**"+ docs);
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

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        firebaseUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FeedActivity.this,"Hesap Başarıyla Silindi",Toast.LENGTH_LONG).show();
                            Intent intentToProfile = new Intent(FeedActivity.this, SignUpActivity.class);
                            startActivity(intentToProfile);
                            intentToProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
                            finish();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) // Menüde Seçim Yapılırsa Yapılacaklar
    {
        if(item.getItemId() == R.id.add_post) // Gönderi Ekle
        {
            Intent intentToUpload = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intentToUpload);
        }
        else if(item.getItemId() == R.id.profile) // Profile Git
        {
            Intent intentToProfile = new Intent(FeedActivity.this, ProfileActivity.class);
            startActivity(intentToProfile);
            intentToProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
            finish();
        }
        else if(item.getItemId() == R.id.updatepassword) // Şifre Değiştir
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(FeedActivity.this);

            alert.setTitle("Onay");
            alert.setMessage("Şifre Değiştirme Maili Almak İstiyor musunuz?");

            alert.setPositiveButton("Evet", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    firebaseAuth.sendPasswordResetEmail(ProfileActivity.currentEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(FeedActivity.this,"Şifre Yenileme Maili Gönderildi, Lütfen Kontrol Edin",Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(FeedActivity.this,"Mail Gönderilirken Sorun Oluştu!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                }
            });

            alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });

            alert.show();
        }
        else if(item.getItemId() == R.id.deleteaccount) // Hesabı Sil
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(FeedActivity.this);

            alert.setTitle("Onay");
            alert.setMessage("Hesabınızı Kalıcı Olarak Silmek İstiyor musunuz?");

            alert.setPositiveButton("Evet", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    for(int i =0; i<userEmailFromFB.size(); i++)
                    {
                        if(ProfileActivity.currentEmail.matches(userEmailFromFB.get(i)))
                        {
                            deleteAccountId.add(userIdFromFB.get(i));
                        }
                    }
                    deleteAccount();
                }
            });

            alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });

            alert.show();

        }
        else if(item.getItemId() == R.id.signout) // Çıkış Yap
        {
            firebaseAuth.signOut();

            Intent intentToSignup = new Intent(FeedActivity.this,SignUpActivity.class);
            startActivity(intentToSignup);
            intentToSignup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        userEmailFromFB = new ArrayList<>();
        userCommentFromFB = new ArrayList<>();
        userImageFromFB = new ArrayList<>();
        userAddressFromFB = new ArrayList<>();
        userLatitudeFromFB = new ArrayList<>();
        userLongitudeFromFB = new ArrayList<>();
        userIdFromFB = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        if(deleteItem)
        {
            deletePost();
            deleteItem = false;
            ArchiveActivity.deleteItem = false;
            finish();
            startActivity(getIntent());
        }

        if(updateAct)
        {
            updateAct = false;
            finish();
            startActivity(getIntent());
        }

        if(!deleteItem && !updateAct)
        {
            getDataFromFirestore();
        }


        RecyclerView recyclerView = findViewById(R.id.recyclerProfileView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        feedRecyclerAdapter = new FeedRecyclerAdapter(userEmailFromFB,userCommentFromFB,userImageFromFB,userAddressFromFB);

        recyclerView.setAdapter(feedRecyclerAdapter);
    }

    public void goLocation(int position, Context context)
    {
        intent = new Intent(context,MapsActivity.class);

        intent.putExtra("locationLatitude",userLatitudeFromFB.get(position));
        intent.putExtra("locationLongitude",userLongitudeFromFB.get(position));
        intent.putExtra("locationAddress",userAddressFromFB.get(position));

        startActivity(intent);
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
                    Toast.makeText(FeedActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            String visibility = (String) data.get("visibility");

                            if(visibility.matches("true"))
                            {
                                String comment = (String) data.get("comment");
                                String userEmail = (String) data.get("useremail");
                                String downloadUrl = (String) data.get("downloadurl");
                                String address = (String) data.get("address");
                                String latitude = (String) data.get("latitude");
                                String longitude = (String) data.get("longitude");
                                String id = snapshot.getId();

                                userCommentFromFB.add(comment);
                                userEmailFromFB.add(userEmail);
                                userImageFromFB.add(downloadUrl);
                                userAddressFromFB.add(address);
                                userLatitudeFromFB.add(latitude);
                                userLongitudeFromFB.add(longitude);
                                userIdFromFB.add(id);

                                feedRecyclerAdapter.notifyDataSetChanged();
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
                    Toast.makeText(FeedActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
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

}
