package com.example.scheduler;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;



public class CustomDialog extends Dialog {

    TextView startTime;
    TextView endTime;
    TextView location;
    TextView memo;
    TextView date;
    Button close;
    int id;

    public CustomDialog(@NonNull Context context, int id) {
        super(context);
        this.id = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.schedule_dialog);

        startTime = findViewById(R.id.tv_startTime);
        endTime = findViewById(R.id.tv_endTime);
        location = findViewById(R.id.tv_location);
        memo = findViewById(R.id.tv_memo);
        date = findViewById(R.id.dialog_tv_date);
        close = findViewById(R.id.dialog_close);

        MyDBHelper dbHelper = new MyDBHelper(getContext());
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cur = sqlDB.rawQuery("select * from tbl_scheduler where id = " + id +";",null);
        if(cur.moveToNext()) {
            startTime.setText(cur.getString(cur.getColumnIndex("startTime")));
            endTime.setText(cur.getString(cur.getColumnIndex("endTime")));
            location.setText(cur.getString(cur.getColumnIndex("location")));
            memo.setText(cur.getString(cur.getColumnIndex("memo")));
            date.setText(cur.getString(cur.getColumnIndex("date")));
        }


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }


}
