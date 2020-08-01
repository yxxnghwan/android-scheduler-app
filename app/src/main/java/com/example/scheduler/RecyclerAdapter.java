package com.example.scheduler;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    // adapter에 들어갈 list 입니다.
    private ArrayList<Data> listData = new ArrayList<>();
    MyDBHelper dbHelper;
    SQLiteDatabase sqlDB;

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return listData.size();
    }

    void addItem(Data data) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView time;
        private ImageView btn;



        ItemViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.RecyclerViewTitle);
            time = itemView.findViewById(R.id.RecyclerViewTime);
            btn = itemView.findViewById(R.id.editButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    int id = listData.get(pos).getId();
                    Activity activity = (Activity) v.getContext();
                    CustomDialog customDialog = new CustomDialog(activity, id);
                    customDialog.show();
                }
            });
        }

        void onBind(final Data data) {
            title.setText(data.getSchedule());
            Date d;
            Calendar c;
            String start = "";
            String end = "";
            try {
                d = new SimpleDateFormat("hh:mm").parse(data.getStartTime());
                c = Calendar.getInstance();
                c.setTimeInMillis(d.getTime());
                start = (c.get(Calendar.AM_PM) == 0 ? "am" : "pm") + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                d = new SimpleDateFormat("hh:mm").parse(data.getEndTime());
                c = Calendar.getInstance();
                c.setTimeInMillis(d.getTime());
                end = (c.get(Calendar.AM_PM) == 0 ? "am" : "pm") + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            time.setText(start + " - " + end);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn.setImageResource(R.drawable.edit_btn_pressed);
                    Activity activity = (Activity) v.getContext();
                    Intent intent = new Intent(activity, EditScheduleActivity.class);
                    intent.putExtra("id", data.getId());
                    activity.finish();
                    activity.startActivity(intent);
                }
            });

        }


    }



}
