package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity
{
    Bitmap selectedImage;
    Uri imageData;
    ImageView selectImage;

    EditText nameEditText;
    EditText aboutMeText;

    static String oldName;
    static String oldAbout;
    static String oldImage;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    HashMap<String, Object> userData;

    boolean control = false;
    boolean ppControl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        selectImage = findViewById(R.id.selectImage);
        selectImage.setImageResource(R.drawable.user);
        nameEditText = findViewById(R.id.nameEditText);
        aboutMeText = findViewById(R.id.aboutMeText);

        nameEditText.setText("Ad Soyad");
        aboutMeText.setText("Hakkımda");

        if(oldName != null)
        {
            nameEditText.setText(oldName);
        }

        if(oldAbout != null)
        {
            aboutMeText.setText(oldAbout);
        }

        if(oldImage != null && !oldImage.matches("null"))
        {
            Picasso.get().load(oldImage).into(selectImage);
            control = true;
        }

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void deletePp(View view)
    {
        selectImage.setImageResource(R.drawable.user);
        ppControl = true;
        control = false;
    }

    public void saveChange(View view)
    {
        if(imageData != null || control)
        {
            if(imageData != null)
            {
                Toast.makeText(EditProfileActivity.this,"Tamamlanıyor",Toast.LENGTH_LONG).show();

                final String imageName = "profileimages/" + ProfileActivity.currentEmail + ".jpg";

                // Resmi Storage'a yükle
                storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) // İşlme Başarılı
                    {
                        StorageReference newReference = FirebaseStorage.getInstance().getReference(imageName);

                        newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                String downloadUrl = uri.toString(); // Kaydedilen resmin urlsini al

                                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                String userEmail = firebaseUser.getEmail();
                                String fullName = nameEditText.getText().toString();
                                String aboutMe = aboutMeText.getText().toString();

                                userData = new HashMap<>();
                                userData.put("useremail",userEmail);
                                userData.put("downloadurl",downloadUrl);
                                userData.put("fullname",fullName);
                                userData.put("aboutme", aboutMe);

                                firebaseFirestore.collection("Users").document(userEmail).update(userData).addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        ProfileActivity.updatePhoto = true;

                                        Toast.makeText(EditProfileActivity.this,"Kaydedildi",Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(EditProfileActivity.this,ProfileActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(EditProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e) // İşlem Başarısız
                    {
                        Toast.makeText(EditProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            else if(control)
            {
                int number = 0;

                if(oldName != null)
                {
                    if(oldName.matches(nameEditText.getText().toString()))
                    {
                        number++;
                    }
                }

                if(oldAbout != null)
                {
                    if(oldAbout.matches(aboutMeText.getText().toString()))
                    {
                        number++;
                    }
                }

                if(number == 2)
                {
                    Toast.makeText(EditProfileActivity.this,"Değişiklik Yapılmadı!",Toast.LENGTH_LONG).show();
                }

                else
                {
                    Toast.makeText(EditProfileActivity.this,"Tamamlanıyor",Toast.LENGTH_LONG).show();

                    final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    String userEmail = firebaseUser.getEmail();
                    String fullName = nameEditText.getText().toString();
                    String aboutMe = aboutMeText.getText().toString();

                    userData = new HashMap<>();
                    userData.put("useremail",userEmail);
                    userData.put("fullname",fullName);
                    userData.put("aboutme", aboutMe);

                    firebaseFirestore.collection("Users").document(userEmail).update(userData).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            ProfileActivity.updatePhoto = true;

                            Toast.makeText(EditProfileActivity.this,"Kaydedildi",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(EditProfileActivity.this,ProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(EditProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

        }

        else // İlk kez düzenlemeye girerse ve foto olmadan kaydederse
        {
            if(nameEditText.getText().toString().matches("") && aboutMeText.getText().toString().matches("") && !ppControl) // Değişiklik yapılmadıysa
            {
                Toast.makeText(EditProfileActivity.this,"Değişiklik Yapılmadı!",Toast.LENGTH_LONG).show();
            }

            else
            {
                Toast.makeText(EditProfileActivity.this,"Tamamlanıyor",Toast.LENGTH_LONG).show();

                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                String userEmail = firebaseUser.getEmail();
                String fullName = nameEditText.getText().toString();
                String aboutMe = aboutMeText.getText().toString();

                userData = new HashMap<>();
                userData.put("useremail",userEmail);
                userData.put("downloadurl","null");
                userData.put("fullname",fullName);
                userData.put("aboutme", aboutMe);

                firebaseFirestore.collection("Users").document(userEmail).update(userData).addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        ProfileActivity.updatePhoto = true;

                        Toast.makeText(EditProfileActivity.this,"Kaydedildi",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(EditProfileActivity.this,ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(EditProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            if(ppControl)
            {
                final String imageName = "profileimages/" + ProfileActivity.currentEmail + ".jpg";

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


            }
        }

    }

    public Bitmap makeSmallerImage(Bitmap image, int maxSize)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if(bitmapRatio > 1)
        {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        }

        else
        {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void selectImage(View view)
    {   // Dosyalara erişme izni yoksa
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},3); // İzin iste
        }
        else // İzin varsa
        {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,4);
        }
    }

    @Override // İzin ilk verilince yapılacaklar
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 3)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,4);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override // Görsel seçildikten sonra yapılacaklar
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == 4 && resultCode == RESULT_OK && data != null)
        {
            imageData = data.getData(); // Seçilen resmin urisini al

            try
            {
                if(Build.VERSION.SDK_INT >= 28)
                {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    Bitmap smallImage = makeSmallerImage(selectedImage,300);
                    selectImage.setImageBitmap(smallImage); // Seçilen resmi imageView'e koy
                }
                else
                {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData); // Resmi bitmap'e çevir
                    selectImage.setImageBitmap(selectedImage); // Seçilen resmi imageView'e koy
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
