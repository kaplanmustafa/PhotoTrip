package com.mustafakaplan.phototrip;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mustafakaplan.phototrip.ui.main.DiscoverFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class DiscoverRecyclerAdapter extends RecyclerView.Adapter<DiscoverRecyclerAdapter.PostHolder>
{
    private ArrayList<String> userEmailList;
    private ArrayList<String> userNameList;
    private ArrayList<String> userCommentList;
    private ArrayList<String> userImageList;
    private ArrayList<String> userAddressList;

    public DiscoverRecyclerAdapter(ArrayList<String> userEmailList, ArrayList<String> userNameList, ArrayList<String> userCommentList, ArrayList<String> userImageList,ArrayList<String> userAddressList)
    {
        this.userEmailList = userEmailList;
        this.userNameList = userNameList;
        this.userCommentList = userCommentList;
        this.userImageList = userImageList;
        this.userAddressList = userAddressList;
    }

    @NonNull
    @Override // Burası oluşturulunca ne olacağı
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_discover,parent,false);
        View.OnClickListener onClickListener = null;
        view.setOnClickListener(onClickListener);
        return new PostHolder(view);
    }


    @Override // Buraya bağlanınca ne olacağı
    public void onBindViewHolder(@NonNull final PostHolder holder, final int position)
    {
        holder.userEmailText.setText(userNameList.get(position));
        holder.commentText.setText(userCommentList.get(position));
        holder.addressText.setText(userAddressList.get(position));
        Picasso.get().load(userImageList.get(position)).into(holder.imageView);

        // Ana Sayfadan Profile Gitme
        holder.userEmailText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.getContext().startActivity(new Intent(v.getContext(), ProfileActivity.class).putExtra("showUser",userEmailList.get(position)).putExtra("activity","discover"));
            }
        });

        // Ana Sayfadan Konuma Gitme
        holder.addressText.setOnClickListener(new View.OnClickListener() // Konum Bilgisi Click
        {
            @Override
            public void onClick(View v)
            {
                if (!holder.addressText.getText().toString().matches(""))
                {
                    v.getContext().startActivity(new Intent(v.getContext(), MapsActivity.class).putExtra("locationLatitude", DiscoverFragment.userLatitudeFromFB.get(position)).putExtra("locationLongitude",DiscoverFragment.userLongitudeFromFB.get(position)).putExtra("locationAddress",DiscoverFragment.userAddressFromFB.get(position)));
                }
            }
        });
    }

    @Override
    public int getItemCount() // Listedeki Eleman-Satır Sayısı
    {
        return userEmailList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder // Elemanlar Tanımlanır
    {
        ImageView imageView;
        TextView userEmailText;
        TextView commentText;
        TextView addressText;

        public PostHolder(@NonNull View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.recyclerview_row_discover_imageview);
            userEmailText = itemView.findViewById(R.id.recyclerview_row_discover_location_text);
            commentText = itemView.findViewById(R.id.recyclerview_row_discover_comment_text);
            addressText = itemView.findViewById(R.id.recyclerview_row_discover_useraddress_text);
        }
    }
}
