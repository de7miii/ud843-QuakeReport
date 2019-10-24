package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private final String TAG = CustomAdapter.class.getSimpleName();

    private Context mContext;

    private List<Earthquake> mEarthquakes;

    private OnItemClickListener mOnItemClickListener;

    CustomAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_layout, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.CustomViewHolder holder, int position) {
        Earthquake currentEarthquake = mEarthquakes.get(position);
        holder.bind(currentEarthquake);
    }

    @Override
    public int getItemCount() {
        return mEarthquakes != null ? mEarthquakes.size() : 0;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView magnitudeText;
        private TextView placeTextTop;
        private TextView placeTextBottom;
        private TextView timeText;

        CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            magnitudeText = itemView.findViewById(R.id.text_mag);
            placeTextTop = itemView.findViewById(R.id.text_place_top);
            placeTextBottom = itemView.findViewById(R.id.text_place_bottom);
            timeText = itemView.findViewById(R.id.text_time);
            itemView.setOnClickListener(this);
        }

        void bind(Earthquake earthquake) {
            String placeRaw = earthquake.getPlace();
            String [] splitArr = placeRaw.split("(?<=of) ");
            if (splitArr.length > 1){
                placeTextTop.setText(splitArr[0]);
                placeTextBottom.setText(splitArr[1]);
            }else{
                placeTextTop.setText(mContext.getString(R.string.near_the));
                placeTextBottom.setText(splitArr[0]);
            }
            GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeText.getBackground();
            int magnitudeColor = getMagnitudeColor(earthquake.getMagnitude());
            magnitudeCircle.setColor(magnitudeColor);
            magnitudeText.setText(earthquake.getMagnitude());
            timeText.setText(earthquake.getTime());
        }

        @Override
        public void onClick(View v) {
            mOnItemClickListener.onItemClicked(getAdapterPosition());
        }
    }

    void updateEarthquakes(List<Earthquake> earthquakes) {
        mEarthquakes = earthquakes;
    }

    private int getMagnitudeColor(String magnitude){
        int mag = (int) Math.floor(Double.parseDouble(magnitude));
        switch (mag){
            case 1:
                return mContext.getColor(R.color.magnitude1);
            case 2:
                return mContext.getColor(R.color.magnitude2);
            case 3:
                return mContext.getColor(R.color.magnitude3);
            case 4:
                return mContext.getColor(R.color.magnitude4);
            case 5:
                return mContext.getColor(R.color.magnitude5);
            case 6:
                return mContext.getColor(R.color.magnitude6);
            case 7:
                return mContext.getColor(R.color.magnitude7);
            case 8:
                return mContext.getColor(R.color.magnitude8);
            case 9:
                return mContext.getColor(R.color.magnitude9);
            default:
                return mContext.getColor(R.color.magnitude10plus);
        }
    }

    public interface OnItemClickListener{
        void onItemClicked(int position);
    }
}
