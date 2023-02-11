package com.example.animationproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Element> projects;
    private final OnMainItemClickListener omicl;

    public MainListAdapter(Context context, List<Element> projects, OnMainItemClickListener omicl) {
        inflater = LayoutInflater.from(context);
        this.projects = projects;
        this.omicl = omicl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.mainlist_item,parent,false);
        return new ViewHolder(view,projects,omicl);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
Element element = projects.get(position);
holder.projectName.setText(element.getNameOfAnimation());
        int howToLoad = 0;
        if(element.getBmp()!=null) howToLoad = 2;
        if(element.getUriImg()!=null) howToLoad = 0;
        switch (howToLoad){
            case 0:
                Picasso.get().load(element.getUriImg()).into(holder.projectView);
                break;
            case 2:
                holder.projectView.setImageBitmap(element.getBmp());
                break;
            default:
                System.out.println("Ошибка изображения ElementAdapter");
        }
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView projectView;
        final TextView projectName;

        public ViewHolder(@NonNull View itemView, List<Element> itemList, OnMainItemClickListener omicl) {
            super(itemView);
            projectView = itemView.findViewById(R.id.projectPreview);
            projectName = itemView.findViewById(R.id.projectNamePreview);
            itemView.setOnLongClickListener(view -> {
                Element e = itemList.get(getLayoutPosition());
                omicl.onLongItemClick(e);
                return false;
            });
            itemView.setOnClickListener(view -> {
                Element e = itemList.get(getLayoutPosition());
                omicl.onItemClick(e);
            });
        }
    }

    public interface OnMainItemClickListener{
        void onItemClick(Element item);
        void onLongItemClick(Element item);

    }
}
