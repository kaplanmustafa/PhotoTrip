package com.mustafakaplan.phototrip;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArchiveRecyclerAdapter extends RecyclerView.Adapter<ArchiveRecyclerAdapter.PostHolder>
{
    private  ArrayList<String> userCommentList;
    private  ArrayList<String> userImageList;
    private  ArrayList<String> userAddressList;
    private  ArrayList<String> documentIdList;
    Boolean control = false;

    public ArchiveRecyclerAdapter(ArrayList<String> userCommentList, ArrayList<String> userImageList,ArrayList<String> userAddressList,ArrayList<String> documentIdList)
    {
        this.userCommentList = userCommentList;
        this.userImageList = userImageList;
        this.userAddressList = userAddressList;
        this.documentIdList = documentIdList;
    }

    @NonNull
    @Override // Burası oluşturulunca ne olacağı
    public ArchiveRecyclerAdapter.PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row3,parent,false);
        View.OnClickListener onClickListener = null;
        view.setOnClickListener(onClickListener);

        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostHolder holder, final int position)
    {
        if(control)
        {
            holder.imageView.setImageResource(R.drawable.archive);
            holder.deleteButton.setVisibility(View.INVISIBLE);
            holder.restoreButton.setVisibility(View.INVISIBLE);
            holder.addressText.setText("");
            holder.commentText.setText("");

            control = false;
        }
        else
        {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.restoreButton.setVisibility(View.VISIBLE);

            holder.commentText.setText(userCommentList.get(position));
            holder.addressText.setText(userAddressList.get(position));
            Picasso.get().load(userImageList.get(position)).into(holder.imageView);

            // Arşivdeki Gönderiyi Tamamen Sil
            holder.deleteButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                    alert.setTitle("Onay");
                    alert.setMessage("Gönderi Tamamen Silinsin mi?");

                    alert.setPositiveButton("Evet", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            userImageList.remove(position);
                            userAddressList.remove(position);
                            userCommentList.remove(position);

                            ProfileActivity.deletePost = true;
                            ProfileActivity.deleteDoc.add(documentIdList.get(position));

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
            });

            // Arşivdeki Gönderiyi Geri Yükle
            holder.restoreButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                    alert.setTitle("Onay");
                    alert.setMessage("Gönderi Geri Yüklensin mi?");

                    alert.setPositiveButton("Evet", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            userImageList.remove(position);
                            userAddressList.remove(position);
                            userCommentList.remove(position);

                            ProfileActivity.updateActivity = true;
                            ProfileActivity.updateDoc.add(documentIdList.get(position));

                            documentIdList.remove(position);

                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, documentIdList.size());

                            Toast.makeText(v.getContext(),"Gönderi Geri Yüklendi",Toast.LENGTH_LONG).show();
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



    }

    @Override
    public int getItemCount() // Listedeki Eleman-Satır Sayısı
    {
        if(userImageList.size() == 0)
        {
            control = true;
            return userImageList.size()+1;
        }

        return userImageList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder // Elemanlar Tanımlanır
    {
        ImageView imageView;
        TextView commentText;
        TextView addressText;
        Button deleteButton;
        Button restoreButton;

        public PostHolder(@NonNull View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.recyclerview_row_imageview);
            commentText = itemView.findViewById(R.id.recyclerview_row_comment_text);
            addressText = itemView.findViewById(R.id.recyclerview_row_useraddress_text);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            restoreButton = itemView.findViewById(R.id.restoreButton);
        }
    }
}
