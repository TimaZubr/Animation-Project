package com.example.animationproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private final List<Element> elements;
    private final OnFrameClickListener ofcl;

    public FrameAdapter(Context context, List<Element> frameList, OnFrameClickListener ofcl) {
        this.elements = frameList;
        this.inflater = LayoutInflater.from(context);
        this.ofcl = ofcl;
    }

    @NonNull
    @Override
    public FrameAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.frame, parent, false);
        return new ViewHolder(view, elements, ofcl);
    }

    @Override
    public void onBindViewHolder(FrameAdapter.ViewHolder holder, int position) {
       Element element = elements.get(position);
        holder.frameView.setImageBitmap(element.getBmp());
      //holder.frameView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,650,278,false));
    }
    @Override
    public int getItemCount() {
        return elements.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView frameView;

        ViewHolder(View view, List<Element> fl, OnFrameClickListener ofcl){
            super(view);
            frameView = view.findViewById(R.id.image_frame);
            view.setOnClickListener(view1 -> {
                Element element = fl.get(getLayoutPosition());
                ofcl.onFrameClick(element);
            });
            view.setOnLongClickListener(view12 -> {
                Element element = fl.get(getLayoutPosition());
                ofcl.onLongFrameClick(element);
                return false;
            });
        }
    }

    public interface OnFrameClickListener{
        void onFrameClick(Element element);
        void onLongFrameClick(Element element);
    }

}
