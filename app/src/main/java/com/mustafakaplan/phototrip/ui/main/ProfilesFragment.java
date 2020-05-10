package com.mustafakaplan.phototrip.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustafakaplan.phototrip.DiscoverRecyclerAdapter;
import com.mustafakaplan.phototrip.ProfileActivity;
import com.mustafakaplan.phototrip.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfilesFragment extends Fragment
{
    PageViewModel pageViewModel;

    ArrayList<String> userEmailFromFB;
    ArrayList<String> userNameFromFB;

    private FirebaseFirestore firebaseFirestore;
    final static String[] dizi = new String[]{"Ankara","İstanbul"};

    AutoCompleteTextView editText;
    ImageView deleteTextButton;
    ArrayAdapter<String> arrayAdapter;

    public static ProfilesFragment newInstance()
    {
        return new ProfilesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        pageViewModel = ViewModelProviders.of(requireActivity()).get(PageViewModel.class);

        userEmailFromFB = new ArrayList<>();
        userNameFromFB = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getDataFromFirestore();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        return inflater.inflate(R.layout.fragment_profiles,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deleteTextButton = view.findViewById(R.id.deleteTextButton);
        editText = view.findViewById(R.id.actv);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,userNameFromFB);
        editText.setAdapter(arrayAdapter);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(userNameFromFB.contains(s.toString()))
                {
                    Intent intentToProfile = new Intent(getContext(),ProfileActivity.class);
                    intentToProfile.putExtra("showUser",userEmailFromFB.get(userNameFromFB.indexOf(s.toString())));
                    intentToProfile.putExtra("activity","places");
                    startActivity(intentToProfile);
                }
            }
        });

        deleteTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

    }

    public void getDataFromFirestore()
    {
        CollectionReference collectionReference = firebaseFirestore.collection("Users");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    Toast.makeText(getContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            String userEmail = snapshot.getId();
                            String userName = (String) data.get("username");

                            userEmailFromFB.add(userEmail);
                            userNameFromFB.add(userName);
                        }
                    }
                }
            }
        });
    }
}
