package com.example.myweather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeatherModel> weatherModelArrayList;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModelArrayList) {
        this.context = context;
        this.weatherModelArrayList = weatherModelArrayList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_weather_rv,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        WeatherModel model= weatherModelArrayList.get(position);
        holder.tvtmp.setText(model.getTemperature()+"°C");
        SimpleDateFormat input=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output=new SimpleDateFormat("hh:mm aa");
        try {
            Date t=input.parse(model.getTime());
            holder.timetv.setText(output.format(t));
        }catch (ParseException e){
            e.printStackTrace();
        }


        Picasso.get().load("http:".concat(model.getIcon())).into(holder.icon);
        holder.windtv.setText(model.getWindSpeed()+"Km/h");
    }

    @Override
    public int getItemCount() {
        return weatherModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView windtv,tvtmp,timetv;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            windtv=itemView.findViewById(R.id.tvwind);
            tvtmp=itemView.findViewById(R.id.tvtemp);
            timetv=itemView.findViewById(R.id.tvtime);
            icon=itemView.findViewById(R.id.ivIconCondition);
        }
    }
}
