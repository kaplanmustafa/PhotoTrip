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
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity
{
    Button uploadButton;
    EditText commentText;
    ImageView selectImage, selectLocation;
    Bitmap selectedImage;
    Uri imageData;

    static String address="";
    static LatLng location = null;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    static TextView locationText;
    static Switch locationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadButton = findViewById(R.id.upload);
        commentText = findViewById(R.id.commentText);
        selectImage = findViewById(R.id.selectImage);
        selectLocation = findViewById(R.id.selectLocation);
        locationText = findViewById(R.id.addressText);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        locationSwitch = findViewById(R.id.locationSwitch);
    }

    public void upload(View view)
    {
        if(imageData != null)
        {
            Toast.makeText(UploadActivity.this,"Tamamlanıyor",Toast.LENGTH_LONG).show();

            UUID uuid = UUID.randomUUID();
            final String imageName = "images/" + uuid + ".jpg";

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

                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            String userEmail = firebaseUser.getEmail();
                            String comment = commentText.getText().toString();

                            HashMap<String, Object> postData = new HashMap<>();
                            postData.put("useremail",userEmail);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());
                            postData.put("visibility","true");
                            postData.put("imagename",imageName);

                            if(locationText.getText().toString().matches("") || !locationSwitch.isChecked())
                            {
                                postData.put("address","");
                                postData.put("latitude","");
                                postData.put("longitude","");
                            }
                            else
                            {
                                postData.put("address",address);
                                postData.put("latitude",String.valueOf(location.latitude));
                                postData.put("longitude",String.valueOf(location.longitude));
                            }

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                            {
                                @Override
                                public void onSuccess(DocumentReference documentReference)
                                {

                                    Toast.makeText(UploadActivity.this,"Tamamlandı",Toast.LENGTH_LONG).show();
                                    UploadActivity.address = null;
                                    UploadActivity.location = null;
                                    Intent intent = new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
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
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    // geri tuşuna bastığında
    public void onBackPressed()
    {
        UploadActivity.address = null;
        UploadActivity.location = null;

        Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Bütün aktiviteleri kapat
        startActivity(intent);
        finish();
    }

    public void locationActivity(View view)
    {
        if(locationSwitch.isChecked())
        {
            locationText.setEnabled(true);
        }

        else
        {
            locationText.setEnabled(false);
        }
    }

    public void selectLocation(View view)
    {
        Intent intent = new Intent(UploadActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    public void selectImage(View view)
    {   // Dosyalara erişme izni yoksa
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1); // İzin iste
        }
        else // İzin varsa
        {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }
    }

    @Override // İzin ilk verilince yapılacaklar
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override // Görsel seçildikten sonra yapılacaklar
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == 2 && resultCode == RESULT_OK && data != null)
        {
           imageData = data.getData(); // Seçilen resmin urisini al

            try
            {
                if(Build.VERSION.SDK_INT >= 28)
                {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    selectImage.setImageBitmap(selectedImage); // Seçilen resmi imageView'e koy
                }
                else
                {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData); // Resmi bitmap'e çevir
                    selectImage.setImageBitmap(selectedImage); // Seçilen resmi imageView'e koy
                }

                uploadButton.setVisibility(View.VISIBLE);
                commentText.setVisibility(View.VISIBLE);
                selectLocation.setVisibility(View.VISIBLE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
