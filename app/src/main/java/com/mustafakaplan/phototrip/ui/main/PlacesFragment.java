package com.mustafakaplan.phototrip.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import com.mustafakaplan.phototrip.PlacesRecyclerAdapter;
import com.mustafakaplan.phototrip.ProfileActivity;
import com.mustafakaplan.phototrip.R;
import com.mustafakaplan.phototrip.UploadActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class PlacesFragment extends Fragment
{
    PageViewModel pageViewModel;

    ArrayList<String> userEmailFromFB;
    ArrayList<String> userNameFromFB;
    ArrayList<String> userIdFromFB;
    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;
    public static ArrayList<String> userAddressFromFB;
    public static ArrayList<String> userLatitudeFromFB;
    public static ArrayList<String> userLongitudeFromFB;
    public static boolean control = false;
    public static boolean control2 = false;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    String imageName = "";
    PlacesClient placesClient;
    SupportMapFragment mapFragment;

    PlacesRecyclerAdapter placesRecyclerAdapter;

    public static PlacesFragment newInstance()
    {
        return new PlacesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        pageViewModel = ViewModelProviders.of(requireActivity()).get(PageViewModel.class);

        userEmailFromFB = new ArrayList<>();
        userNameFromFB = new ArrayList<>();
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

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        return inflater.inflate(R.layout.fragment_places,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerProfileView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        placesRecyclerAdapter = new PlacesRecyclerAdapter(userEmailFromFB, userNameFromFB,userCommentFromFB,userImageFromFB,userAddressFromFB);

        recyclerView.setAdapter(placesRecyclerAdapter);

        String apiKey = "AIzaSyCArh3iYb0-1ZlsfZSw7Wx907Cmr1uwrTI";

        if (!Places.isInitialized())
        {
            Places.initialize(getContext(),apiKey);
        }

        placesClient = Places.createClient(getContext());

        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(@NonNull Place place)
            {

                String address = place.getName();

                userEmailFromFB.clear();
                userNameFromFB.clear();
                userCommentFromFB.clear();
                userIdFromFB.clear();
                userImageFromFB.clear();
                userAddressFromFB.clear();
                userLatitudeFromFB.clear();
                userLongitudeFromFB.clear();

                getDataFromFirestore(address);

            }

            @Override
            public void onError(@NonNull Status status)
            {

            }

        });

    }

    public void getDataFromFirestore(final String placeName)
    {
        CollectionReference collectionReference = firebaseFirestore.collection("Posts");

        collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>()
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

                                String addressFS = (String) data.get("address");

                                if(addressFS.matches(placeName))
                                {
                                    String visibility = (String) data.get("visibility");

                                    if(visibility.matches("true"))
                                    {
                                        String comment = (String) data.get("comment");
                                        String userEmail = (String) data.get("useremail");
                                        String userName = (String) data.get("username");
                                        String downloadUrl = (String) data.get("downloadurl");
                                        String address = (String) data.get("address");
                                        String latitude = (String) data.get("latitude");
                                        String longitude = (String) data.get("longitude");
                                        String id = snapshot.getId();

                                        userCommentFromFB.add(comment);
                                        userEmailFromFB.add(userEmail);
                                        userNameFromFB.add(userName);
                                        userImageFromFB.add(downloadUrl);
                                        userAddressFromFB.add(address);
                                        userLatitudeFromFB.add(latitude);
                                        userLongitudeFromFB.add(longitude);
                                        userIdFromFB.add(id);

                                        placesRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                        }

                        if(userImageFromFB.size() == 0)
                        {
                            Toast.makeText(getContext(),"Sonuç Bulunamadı",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

    }
}
