package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class SignUpActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    EditText emailText, passwordText, userNameText;
    String email, password;
    FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        userNameText = findViewById(R.id.registerUserName);

        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if(firebaseUser != null) // Cihazdan Daha Önce Giriş Yapılmışsa
        {
            firebaseUser = firebaseAuth.getCurrentUser();

            if(firebaseUser.isEmailVerified())
            {
                ProfileActivity.currentEmail = firebaseUser.getEmail(); // Kullanıcının emailini tut
                Intent intent = new Intent(SignUpActivity.this,FeedActivity.class);
                startActivity(intent);
                finish(); // Aktiviteyi Tamamen Kapatır
            }
        }
    }

    public void  signInClicked(View view)
    {
        email = emailText.getText().toString();
        password = passwordText.getText().toString();

        if(fieldControl(email,password)) // Alanlar Doluysa
        {
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>()
                {
                    @Override
                    public void onSuccess(AuthResult authResult) // İşlem Başarılıysa
                    {
                        firebaseUser = firebaseAuth.getCurrentUser();

                        if(firebaseUser.isEmailVerified())
                        {
                            ProfileActivity.currentEmail = email; // Kullanıcının emailini tut
                            FeedActivity.updateName = true;
                            Intent intent = new Intent(SignUpActivity.this,FeedActivity.class);
                            startActivity(intent);
                            finish(); // Aktiviteyi Tamamen Kapatır
                        }

                        else
                        {
                            Toast.makeText(SignUpActivity.this,"Lütfen Eposta Adresinizi Onaylayın!",Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e) // İşlem Başarısızsa
                    {
                        if(e.getLocalizedMessage().toString().contains("password is invalid or the user does not have"))
                        {
                            Toast.makeText(SignUpActivity.this,"Şifre Yanlış!",Toast.LENGTH_LONG).show();
                        }

                        else if(e.getLocalizedMessage().toString().contains("There is no user record"))
                        {
                            Toast.makeText(SignUpActivity.this,"E-posta Adresi Kayıtlı Değil!",Toast.LENGTH_LONG).show();
                        }

                        else if(e.getLocalizedMessage().toString().contains("badly formatted"))
                        {
                            Toast.makeText(SignUpActivity.this,"E-posta Formatı Yanlış!",Toast.LENGTH_LONG).show();
                        }

                        else if(e.getLocalizedMessage().toString().contains("We have blocked all requests"))
                        {
                            Toast.makeText(SignUpActivity.this,"Çok Fazla Başarısız Giriş Denemesi. Lütfen Daha Sonra Tekrar Deneyiniz!",Toast.LENGTH_LONG).show();
                        }

                        else
                        {
                            Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

        }
    }

    public void  signUpClicked(View view) // Kullanıcı Kayıt
    {
        Intent intentToRegister = new Intent(SignUpActivity.this,RegisterActivity.class);
        startActivity(intentToRegister);
    }

    public void passwordUpdateMail(View view)
    {
        if(!emailText.getText().toString().matches(""))
        {
            firebaseAuth.sendPasswordResetEmail(emailText.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(SignUpActivity.this,"Şifre Yenileme Maili Gönderildi, Lütfen Kontrol Edin",Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(SignUpActivity.this,"Lütfen Geçerli Bir Hesap ile Tekrar Deneyin!",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

        else
        {
            Toast.makeText(SignUpActivity.this,"Lütfen Eposta Girin!",Toast.LENGTH_LONG).show();
        }
    }

    public boolean fieldControl(String email, String password)
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
            Toast.makeText(SignUpActivity.this,"E-posta Gerekli!",Toast.LENGTH_LONG).show();
        }
        else if(fieldWarning == 2) // Sadece şifre alanı boş
        {
            Toast.makeText(SignUpActivity.this,"Şifre Gerekli!",Toast.LENGTH_LONG).show();
        }
        else if(fieldWarning == 3) // Email ve şifre alanı boş
        {
            Toast.makeText(SignUpActivity.this,"E-posta ve Şifre Gerekli!",Toast.LENGTH_LONG).show();
        }
        else if(fieldWarning == 4) // Şifre 0 ve 7 arasında
        {
            Toast.makeText(SignUpActivity.this,"Şifre En Az 6 Karakter Olmalıdır!",Toast.LENGTH_LONG).show();
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
