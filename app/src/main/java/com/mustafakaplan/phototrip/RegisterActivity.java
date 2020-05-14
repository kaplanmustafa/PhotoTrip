package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity
{
    EditText emailText,passwordText,userNameText;
    String newUserName = "";
    String email, password;

    boolean registerControl = true;
    String profileUserNames = "";
    int control = 0;
    int control2 = 0;
    HashMap<String, Object> userData;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailText = findViewById(R.id.registerEmail);
        passwordText = findViewById(R.id.registerPassword);
        userNameText = findViewById(R.id.registerUserName);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    public void register(View view)
    {
        email = emailText.getText().toString();
        password = passwordText.getText().toString();
        newUserName = userNameText.getText().toString();

        if(fieldControl(email,password,newUserName)) // Alanlar Doluysa
        {
            control = 0;
            control2 = 0;

            if(newUserName.matches(""))
            {
                Toast.makeText(RegisterActivity.this,"Lütfen Kullanıcı Adı Giriniz!",Toast.LENGTH_SHORT).show();
            }

            else
            {
                CollectionReference collectionReference = firebaseFirestore.collection("Users");

                collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                    {
                        if(e != null)
                        {
                            Toast.makeText(RegisterActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            if(queryDocumentSnapshots != null)
                            {
                                for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                                {
                                    Map<String,Object> data = snapshot.getData();

                                    profileUserNames = (String)data.get("username");

                                    if(profileUserNames.matches(newUserName) && control2 == 0) // Kullanıcı adı sistemde varsa
                                    {
                                        Toast.makeText(RegisterActivity.this,"Kullanıcı Adı Alınamaz!",Toast.LENGTH_SHORT).show();
                                        control = 1;
                                        break;
                                    }
                                }

                                if(control == 0 && control2 == 0)
                                {
                                    control2 = 1;
                                    userData = new HashMap<>();
                                    userData.put("username", newUserName);

                                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>()
                                    {
                                        @Override
                                        public void onSuccess(AuthResult authResult) // İşlem Başarılıysa
                                        {
                                            ProfileActivity.currentUserName = newUserName;
                                            emailVerification(); //Onay maili gönder

                                            userData.put("useremail",emailText.getText().toString());
                                            userData.put("downloadurl","null");
                                            userData.put("fullname","Adı Soyadı");
                                            userData.put("aboutme", "Hakkında");
                                            ArrayList<String> empty = new ArrayList<>();

                                            userData.put("followed",empty);

                                            firebaseFirestore.collection("Users").document(emailText.getText().toString()).set(userData).addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    finish();
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
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e) // İşlem Başarısızsa
                                        {
                                            if(e.getLocalizedMessage().toString().matches("The email address is already in use by another account."))
                                            {
                                                Toast.makeText(RegisterActivity.this,"E-posta Adresi Başka Bir Kullanıcıya Ait!",Toast.LENGTH_LONG).show();
                                            }

                                            else if(e.getLocalizedMessage().toString().contains("be at least 6"))
                                            {
                                                Toast.makeText(RegisterActivity.this,"Şifre En Az 6 Karakter Olmalıdır!",Toast.LENGTH_LONG).show();
                                            }

                                            else if(e.getLocalizedMessage().toString().contains("badly formatted"))
                                            {
                                                Toast.makeText(RegisterActivity.this,"E-posta Formatı Yanlış!",Toast.LENGTH_LONG).show();
                                            }

                                            else
                                            {
                                                Toast.makeText(RegisterActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });

                                }
                            }

                        }

                    }

                });

            }

        }
    }

    public void emailVerification()
    {
        firebaseAuth.setLanguageCode("tr");
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(RegisterActivity.this,"Kayıt Başarılı, Lütfen Eposta Adresinizi Doğrulayınız",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean fieldControl(String email, String password, String userName)
    {
        int fieldWarning = 0;

        if(!emailText.getText().toString().matches(""))
        {
            email = emailText.getText().toString();
        }
        else
        {
            fieldWarning++;
        }

        if(!passwordText.getText().toString().matches(""))
        {
            password = passwordText.getText().toString();
        }
        else if(passwordText.getText().toString().length() <= 6 && passwordText.getText().toString().length() >= 1)
        {
            fieldWarning += 4;
        }
        else
        {
            fieldWarning += 2;
        }


        if(fieldWarning == 1 || fieldWarning == 5) // Sadece email alanı boş
        {
            Toast.makeText(RegisterActivity.this,"E-posta Gerekli!",Toast.LENGTH_LONG).show();
        }
        else if(fieldWarning == 2) // Sadece şifre alanı boş
        {
            Toast.makeText(RegisterActivity.this,"Şifre Gerekli!",Toast.LENGTH_LONG).show();
        }
        else if(fieldWarning == 3) // Email ve şifre alanı boş
        {
            Toast.makeText(RegisterActivity.this,"E-posta ve Şifre Gerekli!",Toast.LENGTH_LONG).show();
        }
        else if(fieldWarning == 4) // Şifre 0 ve 7 arasında
        {
            Toast.makeText(RegisterActivity.this,"Şifre En Az 6 Karakter Olmalıdır!",Toast.LENGTH_LONG).show();
        }
        else if(userNameText.getText().toString().matches(""))
        {
            Toast.makeText(RegisterActivity.this,"Kullanıcı Adı Gerekli!",Toast.LENGTH_LONG).show();
            fieldWarning = 1;
        }

        if(fieldWarning == 0) // Alanlar doluysa
        {
            return true;
        }
        else // Alanlar boşsa
        {
            return false;
        }
    }
}
