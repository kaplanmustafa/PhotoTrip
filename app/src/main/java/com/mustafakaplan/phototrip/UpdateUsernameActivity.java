package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
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

public class UpdateUsernameActivity extends AppCompatActivity
{
    EditText updateUserNameText;
    String newUserName = "";
    String profileUserNames = "";
    String currentUsername;
    String currentEmail;
    private FirebaseFirestore firebaseFirestore;

    int control = 0;
    int control2 = 0;
    int control3 = 0;

    HashMap<String, Object> userData;
    ArrayList<String> documentIdFromFB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_username);

        updateUserNameText = findViewById(R.id.updateUserNameText);

        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("currentUsername");
        currentEmail = intent.getStringExtra("currentMail");

        updateUserNameText.setText(currentUsername);

        firebaseFirestore = FirebaseFirestore.getInstance();
        documentIdFromFB = new ArrayList<>();
    }

    @Override
    // geri tuşuna bastığında
    public void onBackPressed()
    {
        Toast.makeText(UpdateUsernameActivity.this, "İşlem İptal Edildi", Toast.LENGTH_LONG).show();

        Intent intentToUpdate = new Intent(UpdateUsernameActivity.this, FeedActivity.class);
        startActivity(intentToUpdate);
        intentToUpdate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
        finish();
    }

    public void save(View view)
    {
        newUserName = updateUserNameText.getText().toString();

        if(newUserName.matches("")) // Alan boşsa
        {
            Toast.makeText(UpdateUsernameActivity.this,"Kullanıcı Adı Giriniz!",Toast.LENGTH_SHORT).show();
        }

        else if(newUserName.matches(currentUsername))
        {
            Toast.makeText(UpdateUsernameActivity.this,"Kullanıcı Adı Aynı!",Toast.LENGTH_SHORT).show();
        }

        else
        {
            control = 0;
            control2 = 0;

            CollectionReference collectionReference = firebaseFirestore.collection("Users");

            collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(UpdateUsernameActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                    } else {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                Map<String, Object> data = snapshot.getData();

                                profileUserNames = (String) data.get("username");

                                if (profileUserNames.matches(newUserName) && control2 == 0) // Kullanıcı adı sistemde varsa
                                {
                                    Toast.makeText(UpdateUsernameActivity.this, "Kullanıcı Adı Alınamaz!", Toast.LENGTH_SHORT).show();
                                    control = 1;
                                    break;
                                }
                            }

                            if(control == 0 && control2 == 0)
                            {
                                control2 = 1;
                                userData = new HashMap<>();
                                userData.put("username", newUserName);

                                firebaseFirestore.collection("Users").document(currentEmail).update(userData).addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        ProfileActivity.currentUserName = newUserName;
                                        ProfileActivity.currentEmail = currentEmail;
                                        updateDoc();
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
                        }
                    }
                }
            });
        }
    }

    public void updateDoc()
    {
        CollectionReference collectionReference = firebaseFirestore.collection("Posts");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(UpdateUsernameActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                }
                else
                    {
                    if (queryDocumentSnapshots != null)
                    {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String, Object> data = snapshot.getData();

                            String userEmail = (String) data.get("useremail");

                            if (userEmail.matches(currentEmail)) // Kullanıcının Bütün Gönderilerini Al
                            {
                                String documentId = snapshot.getId();
                                documentIdFromFB.add(documentId);
                            }
                        }

                        if(!documentIdFromFB.isEmpty() && control3 == 0) // Gönderi Varsa Kullanıcı Adlarını Güncelle
                        {
                            control3 = 1;

                            for (String docs : documentIdFromFB) {
                                firebaseFirestore.collection("Posts").document(docs).update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e);
                                    }
                                });
                            }
                        }
                            Toast.makeText(UpdateUsernameActivity.this, "Kullanıcı Adı Değiştirildi", Toast.LENGTH_LONG).show();

                            Intent intentToUpdate = new Intent(UpdateUsernameActivity.this, FeedActivity.class);
                            startActivity(intentToUpdate);
                            intentToUpdate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
                            finish();
                    }
                }
            }
        });
    }
}
