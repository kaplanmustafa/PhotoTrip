package com.mustafakaplan.phototrip.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
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
import java.util.Map;

public class DiscoverFragment extends Fragment
{
    PageViewModel pageViewModel;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<String> userEmailFromFB;
    ArrayList<String> userNameFromFB;
    ArrayList<String> userIdFromFB;
    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;
    public static ArrayList<String> userAddressFromFB;
    public static ArrayList<String> userLatitudeFromFB;
    public static ArrayList<String> userLongitudeFromFB;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    String imageName = "";

    DiscoverRecyclerAdapter discoverRecyclerAdapter;

    public static DiscoverFragment newInstance()
    {
        return new DiscoverFragment();
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

        return inflater.inflate(R.layout.fragment_discover,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerProfileView);
        PhotoView photoView = (PhotoView) view.findViewById(R.id.recyclerview_row_discover_imageview);
        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        discoverRecyclerAdapter = new DiscoverRecyclerAdapter(userEmailFromFB,userNameFromFB,userCommentFromFB,userImageFromFB,userAddressFromFB);

        recyclerView.setAdapter(discoverRecyclerAdapter);

        if(userImageFromFB.isEmpty())
        {
            getDataFromFirestore();
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() // Sayfa Yenileme
        {
            @Override
            public void onRefresh()
            {
                swipeRefreshLayout.setRefreshing(false);

                getActivity().recreate();
            }
        });

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
                    Toast.makeText(getContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(queryDocumentSnapshots != null)
                    {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                        {
                            Map<String,Object> data = snapshot.getData();

                            String user = (String) data.get("useremail");

                            if(!user.matches(ProfileActivity.currentEmail))
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

                                    discoverRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
