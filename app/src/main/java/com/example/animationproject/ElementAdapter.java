package com.example.animationproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class ElementAdapter extends RecyclerView.Adapter<ElementAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private final List<Element> elementList;
    private final OnElementClickListener oecl;

    public ElementAdapter(Context context, List<Element> elementList, OnElementClickListener oecl) {
        this.elementList = elementList;
        this.inflater = LayoutInflater.from(context);
        this.oecl = oecl;
    }

    @NonNull
    @Override
    public ElementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, elementList, oecl);
    }

    @Override
    public void onBindViewHolder(@NonNull ElementAdapter.ViewHolder holder, int position) {
        Element element = elementList.get(position);
        int howToLoad = 0;
        if(element.getBmp()!=null) howToLoad = 2;
        if(element.getUriImg()!=null) howToLoad = 0;
        switch (howToLoad){
            case 0:
                Picasso.get().load(element.getUriImg()).into(holder.frameView);
                break;
            case 2:
                holder.frameView.setImageBitmap(element.getBmp());
                break;
            default:
                System.out.println("Ошибка изображения ElementAdapter");
        }

    }
    @Override
    public int getItemCount() {
        return elementList.size();
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder  {
        final ImageView frameView;

        ViewHolder(View view, List<Element> fl, OnElementClickListener oecl){
            super(view);
            frameView = view.findViewById(R.id.img);
            view.setOnLongClickListener(view1 -> {
                Element element = fl.get(getLayoutPosition());
                oecl.onLongElementClick(element);
                return false;
            });
        }

    }
    public static Bitmap Bytes2Bitmap(byte[] b) {
        if (b == null) {
            return null;
        }
        if (b.length != 0) {
            InputStream is = new ByteArrayInputStream(b);
            return BitmapFactory.decodeStream(is);
        } else {
            return null;
        }
    }
    public interface OnElementClickListener {
        void onLongElementClick(Element element);
    }
}
