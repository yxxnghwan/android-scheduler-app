package com.example.scheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Calendar;

public class EditScheduleActivity extends AppCompatActivity {
    int id;
    TextView tv_date;
    TimePicker startPicker, endPicker;

    Button editBtn, deleteBtn;
    EditText editSchedule, editLocation, editMemo;
    MyDBHelper dbHelper;
    SQLiteDatabase sqlDB;
    Cursor cur;

    Spinner alarmTimeSpinner;
    final String alarmTimes[] = {"5분 전", "10분 전", "15분 전", "30분 전", "1시간 전", "2시간 전", "3시간 전", "하루 전"};
    ArrayAdapter<String> adapter;
    long alarmMinus = 60*5*1000; // 기본 5분 전


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);

        tv_date = findViewById(R.id.edit_tv_date);
        startPicker = findViewById(R.id.edit_startPicker);
        startPicker.setIs24HourView(true);
        endPicker = findViewById(R.id.edit_endPicker);
        endPicker.setIs24HourView(true);
        editBtn = findViewById(R.id.edit_btn);
        editSchedule = findViewById(R.id.edit_schedule);
        editLocation = findViewById(R.id.edit_location);
        editMemo = findViewById(R.id.edit_memo);
        deleteBtn = findViewById(R.id.delete_btn);

        alarmTimeSpinner = findViewById(R.id.edit_alarmTime_spnner);

        adapter = new ArrayAdapter<String>(this, R.layout.alarm_spinner, alarmTimes);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        alarmTimeSpinner.setAdapter(adapter);

        alarmTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        alarmMinus = 60*5*1000;
                        break;
                    case 1:
                        alarmMinus = 60*10*1000;
                        break;
                    case 2:
                        alarmMinus = 60*15*1000;
                        break;
                    case 3:
                        alarmMinus = 60*30*1000;
                        break;
                    case 4:
                        alarmMinus = 60*60*1000;
                        break;
                    case 5:
                        alarmMinus = 60*120*1000;
                        break;
                    case 6:
                        alarmMinus = 60*180*1000;
                        break;
                    case 7:
                        alarmMinus = 60*60*24*1000;
                        break;
                    default:
                        alarmMinus = 60*5*1000;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // 타임피커 커스텀 --> 타임피커는 두개의 넘버피커가 붙어있는 형태로 되어있어서 내부를 커스텀 하려면 각각의 넘버피커를 얻어와야함
        // xml에서 테마 지정도 안먹힘
        int hour_NumberPicker_id = Resources.getSystem().getIdentifier("hour", "id", "android");
        int minute_NumberPicker_id = Resources.getSystem().getIdentifier("minute", "id", "android");
        NumberPicker hourNumberPicker1 = (NumberPicker)startPicker.findViewById(hour_NumberPicker_id);
        NumberPicker minuteNumberPicker1 = (NumberPicker)startPicker.findViewById(minute_NumberPicker_id);
        NumberPicker hourNumberPicker2 = (NumberPicker)endPicker.findViewById(hour_NumberPicker_id);
        NumberPicker minuteNumberPicker2 = (NumberPicker)endPicker.findViewById(minute_NumberPicker_id);

        setNumberPickerTextColor(hourNumberPicker1, Color.WHITE);
        setNumberPickerTextColor(hourNumberPicker2, Color.WHITE);
        setNumberPickerTextColor(minuteNumberPicker1, Color.WHITE);
        setNumberPickerTextColor(minuteNumberPicker2, Color.WHITE);

        setNumberPickerSeperatorColor(hourNumberPicker1, Color.rgb(0,192,192));
        setNumberPickerSeperatorColor(hourNumberPicker2, Color.rgb(0,192,192));
        setNumberPickerSeperatorColor(minuteNumberPicker1, Color.rgb(0,192,192));
        setNumberPickerSeperatorColor(minuteNumberPicker2, Color.rgb(0,192,192));

        dbHelper = new MyDBHelper(this);
        sqlDB = dbHelper.getReadableDatabase();
        cur = sqlDB.rawQuery("select * from tbl_scheduler where id = " + id + ";", null);

        if(cur.moveToNext()) {
            tv_date.setText(cur.getString(cur.getColumnIndex("date")));
            String [] startTimeInfo = cur.getString(cur.getColumnIndex("startTime")).split(":");
            int startHour = Integer.parseInt(startTimeInfo[0]);
            int startMin = Integer.parseInt(startTimeInfo[1]);
            startPicker.setHour(startHour);
            startPicker.setMinute(startMin);
            String [] endTimeInfo = cur.getString(cur.getColumnIndex("endTime")).split(":");
            int endHour = Integer.parseInt(startTimeInfo[0]);
            int endMin = Integer.parseInt(startTimeInfo[1]);
            endPicker.setHour(endHour);
            endPicker.setMinute(endMin);
            editSchedule.setText(cur.getString(cur.getColumnIndex("schedule")));
            editLocation.setText(cur.getString(cur.getColumnIndex("location")));
            editMemo.setText(cur.getString(cur.getColumnIndex("memo")));
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String selectedStartTime = (startPicker.getHour() < 10 ? "0"+startPicker.getHour() : startPicker.getHour())
                        +":"+(startPicker.getMinute() < 10 ? "0"+startPicker.getMinute() : startPicker.getMinute());
                String selectedEndTime = (endPicker.getHour() < 10 ? "0"+endPicker.getHour() : endPicker.getHour())
                        +":"+(endPicker.getMinute() < 10 ? "0"+endPicker.getMinute() : endPicker.getMinute());
                sqlDB.close();
                cur.close();
                sqlDB = dbHelper.getWritableDatabase();
                sqlDB.execSQL("update tbl_scheduler " +
                        "set startTime = '" + selectedStartTime +
                        "', endTime = '" + selectedEndTime +
                        "', schedule = '" + editSchedule.getText() +
                        "', location = '" + editLocation.getText() +
                        "', memo = '" + editMemo.getText() +
                        "' where id = " + id + ";");

                //알람취소
                NotificationManager notificationmanager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    // 이 채널이 다른 채널에서 오는 알람보다 위에 있게 만듬

                    NotificationChannel mChannel = new NotificationChannel(
                            ScheduleAlarm.CHANNEL_ID, ScheduleAlarm.CHANNEL_NAME, importance);

                    notificationmanager.createNotificationChannel(mChannel);
                }
                notificationmanager.cancel(id);

                // 다시 알람

                sqlDB.close();
                sqlDB = dbHelper.getReadableDatabase();
                cur = sqlDB.rawQuery("select * from tbl_scheduler where id = " + id + ";", null);
                cur.moveToNext();
                String dateStr = cur.getString(cur.getColumnIndex("date"));
                int selectedYear = Integer.parseInt(dateStr.split("-")[0]);
                int selectedMonth = Integer.parseInt(dateStr.split("-")[1]);
                int selectedDay = Integer.parseInt(dateStr.split("-")[2]);

                Calendar calendar = Calendar.getInstance();
                calendar.set(selectedYear, selectedMonth-1, selectedDay, startPicker.getHour(), startPicker.getMinute(), 0);
                // 월이 0~11로 찍혀서 지금까지 1을 더해놨었음 Calendar에 시간계산 할 때는 0~11로 해야함
                long alarmTime = calendar.getTimeInMillis()- alarmMinus ; // 알람시간도 다시
                if(alarmTime < System.currentTimeMillis()){
                    new ScheduleAlarm(getApplicationContext()).alarmOn(System.currentTimeMillis(), id, editLocation.getText().toString(), editSchedule.getText().toString());
                } else {
                    new ScheduleAlarm(getApplicationContext()).alarmOn(alarmTime, id, editLocation.getText().toString(), editSchedule.getText().toString());
                }

                Toast.makeText(EditScheduleActivity.this, "수정완료", Toast.LENGTH_SHORT).show();
                cur.close();
                sqlDB.close();
                dbHelper.close();
                Intent intent_main = new Intent(EditScheduleActivity.this, MainActivity.class);
                finish();
                startActivity(intent_main);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB.close();
                cur.close();
                sqlDB = dbHelper.getWritableDatabase();
                sqlDB.execSQL("delete from tbl_scheduler where id = " + id + ";");
                Toast.makeText(EditScheduleActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();

                //알람취소
                NotificationManager notificationmanager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    // 이 채널이 다른 채널에서 오는 알람보다 위에 있게 만듬

                    NotificationChannel mChannel = new NotificationChannel(
                            ScheduleAlarm.CHANNEL_ID, ScheduleAlarm.CHANNEL_NAME, importance);

                    notificationmanager.createNotificationChannel(mChannel);
                }
                notificationmanager.cancel(id);
                sqlDB.close();
                dbHelper.close();
                Intent intent_main = new Intent(EditScheduleActivity.this, MainActivity.class);
                finish();
                startActivity(intent_main);
            }
        });


    }


    @Override
    public void onBackPressed() {
        cur.close();
        sqlDB.close();
        dbHelper.close();
        Intent intent = new Intent(EditScheduleActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        int count = numberPicker.getChildCount();
        for (int i=0;i<count;i++) { // 넘버피커 또한 여러개의 뷰로 이루어진 뷰이다 모든 자식들을 다 받아와서 EditText일 때 색상을 지정함
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setNumberPickerSeperatorColor(NumberPicker numberPicker, int color) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for(Field pf : pickerFields) {
            if(pf.getName().equals("mSelectionDivider")) { //
                pf.setAccessible(true);
                try {
                    ColorDrawable cd = new ColorDrawable(color);
                    pf.set(numberPicker, cd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
