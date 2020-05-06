package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    EditText emailText, passwordText;
    String email, password;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);

        firebaseUser = firebaseAuth.getCurrentUser();

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
                            Toast.makeText(SignUpActivity.this,"Giriş Başarılı",Toast.LENGTH_LONG).show();

                            ProfileActivity.currentEmail = emailText.getText().toString(); // Kullanıcının emailini tut
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
        email = emailText.getText().toString();
        password = passwordText.getText().toString();

        if(fieldControl(email,password)) // Alanlar Doluysa
        {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>()
            {
                @Override
                public void onSuccess(AuthResult authResult) // İşlem Başarılıysa
                {
                    emailVerification(); //Onay maili gönder
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e) // İşlem Başarısızsa
                {
                    if(e.getLocalizedMessage().toString().matches("The email address is already in use by another account."))
                    {
                        Toast.makeText(SignUpActivity.this,"E-posta Adresi Başka Bir Kullanıcıya Ait!",Toast.LENGTH_LONG).show();
                    }

                    else if(e.getLocalizedMessage().toString().contains("be at least 6"))
                    {
                        Toast.makeText(SignUpActivity.this,"Şifre En Az 6 Karakter Olmalıdır!",Toast.LENGTH_LONG).show();
                    }

                    else if(e.getLocalizedMessage().toString().contains("badly formatted"))
                    {
                        Toast.makeText(SignUpActivity.this,"E-posta Formatı Yanlış!",Toast.LENGTH_LONG).show();
                    }

                    else
                    {
                        Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
    }

    public void emailVerification()
    {
        firebaseAuth.setLanguageCode("tr");
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,"Kayıt Başarılı, Lütfen Eposta Adresinizi Doğrulayınız",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void updPass(View view)
    {
        firebaseAuth.sendPasswordResetEmail(emailText.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,"Yenileme Maili Gönderildi",Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
