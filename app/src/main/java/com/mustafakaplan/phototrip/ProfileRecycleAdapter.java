package com.mustafakaplan.phototrip;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileRecycleAdapter extends RecyclerView.Adapter<ProfileRecycleAdapter.PostHolder>
{
    private ArrayList<String> userCommentList;
    private ArrayList<String> userImageList;
    private ArrayList<String> userAddressList;
    private ArrayList<String> documentIdList;

    public ProfileRecycleAdapter(ArrayList<String> userCommentList, ArrayList<String> userImageList,ArrayList<String> userAddressList,ArrayList<String> documentIdList)
    {
        this.userCommentList = userCommentList;
        this.userImageList = userImageList;
        this.userAddressList = userAddressList;
        this.documentIdList = documentIdList;
    }

    @NonNull
    @Override // Burası oluşturulunca ne olacağı
    public ProfileRecycleAdapter.PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.profile_recycler_row,parent,false);
        View.OnClickListener onClickListener = null;
        view.setOnClickListener(onClickListener);

        return new PostHolder(view);
    }


    @Override // Buraya bağlanınca ne olacağı
    public void onBindViewHolder(@NonNull final PostHolder holder, final int position)
    {
        holder.commentText.setText(userCommentList.get(position));
        holder.addressText.setText(userAddressList.get(position));
        Picasso.get().load(userImageList.get(position)).into(holder.imageView);

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(final View v)
            {

                if(ProfileActivity.photoDelete) // Kendi Profilinde
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                    alert.setTitle("Onay");
                    alert.setMessage("Gönderi Silinsin mi?");

                    alert.setPositiveButton("Evet", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            userImageList.remove(position);
                            userAddressList.remove(position);
                            userCommentList.remove(position);

                            FeedActivity.deleteItem = true;
                            ArchiveActivity.deleteItem = true;

                            FeedActivity.deleteDoc.add(documentIdList.get(position));
                            ArchiveActivity.deleteDoc.add(documentIdList.get(position));
                            documentIdList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, documentIdList.size());

                            Toast.makeText(v.getContext(),"Gönderi Silindi",Toast.LENGTH_LONG).show();
                        }
                    });

                    alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });

                    alert.show();

                }

                return false;
            }
        });

        holder.addressText.setOnClickListener(new View.OnClickListener() // Konum Bilgisi Click
        {
            @Override
            public void onClick(View v)
            {
                if (!holder.addressText.getText().toString().matches(""))
                {
                    v.getContext().startActivity(new Intent(v.getContext(), MapsActivity.class).putExtra("locationLatitude",ProfileActivity.userLatitudeFromFB.get(position)).putExtra("locationLongitude",ProfileActivity.userLongitudeFromFB.get(position)).putExtra("locationAddress",ProfileActivity.userAddressFromFB.get(position)));
                }
            }
        });
    }

    @Override
    public int getItemCount() // Listedeki Eleman-Satır Sayısı
    {
        return userImageList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder // Elemanlar Tanımlanır
    {
        ImageView imageView;
        TextView commentText;
        TextView addressText;

        public PostHolder(@NonNull View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.recyclerview_row_imageview);
            commentText = itemView.findViewById(R.id.recyclerview_row_comment_text);
            addressText = itemView.findViewById(R.id.recyclerview_row_useraddress_text);
        }
    }
}
