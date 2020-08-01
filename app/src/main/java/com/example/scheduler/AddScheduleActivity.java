package com.example.scheduler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

public class AddScheduleActivity extends AppCompatActivity {

    TextView tv_date;
    TimePicker startPicker, endPicker;

    Spinner alarmTimeSpinner;
    final String alarmTimes[] = {"5분 전", "10분 전", "15분 전", "30분 전", "1시간 전", "2시간 전", "3시간 전", "하루 전"};
    ArrayAdapter<String> adapter;
    long alarmMinus = 60*5*1000; // 기본 5분 전

    Button addBtn;
    EditText inputSchedule, inputLocation, inputMemo;
    int selectedYear, selectedMonth, selectedDay;
    MyDBHelper dbHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        tv_date = findViewById(R.id.tv_date);
        startPicker = findViewById(R.id.startPicker);
        startPicker.setIs24HourView(true);
        endPicker = findViewById(R.id.endPicker);
        endPicker.setIs24HourView(true);
        addBtn = findViewById(R.id.addBtn);
        inputSchedule = findViewById(R.id.inputSchedule);
        inputLocation = findViewById(R.id.inputLocation);
        inputMemo = findViewById(R.id.inputMemo);

        dbHelper = new MyDBHelper(this);
        sqlDB = dbHelper.getWritableDatabase();

        alarmTimeSpinner = findViewById(R.id.alarmTime_spnner);
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

        Intent intent = getIntent();
        selectedYear = intent.getIntExtra("year", 0);
        selectedMonth = intent.getIntExtra("month", 0);
        selectedDay = intent.getIntExtra("day", 0);

        tv_date.setText(selectedYear+"-"+selectedMonth+"-"+selectedDay);


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = selectedYear + "-" + selectedMonth + "-" + selectedDay;
                String startTime = (startPicker.getHour() < 10 ? "0"+startPicker.getHour() : startPicker.getHour())
                        +":"+(startPicker.getMinute() < 10 ? "0"+startPicker.getMinute() : startPicker.getMinute());
                String endTime = (endPicker.getHour() < 10 ? "0"+endPicker.getHour() : endPicker.getHour())
                        +":"+(endPicker.getMinute() < 10 ? "0"+endPicker.getMinute() : endPicker.getMinute());
                String schedule = inputSchedule.getText().toString();
                String location = inputLocation.getText().toString();
                String memo = inputMemo.getText().toString();


                sqlDB.execSQL("insert into tbl_scheduler(date, startTime, endTime, schedule, location, memo)" +
                        " values('" + date + "','" + startTime + "','" + endTime + "','" + schedule + "','"+ location +"','"+ memo +"');");

                sqlDB.close();

                sqlDB = dbHelper.getReadableDatabase();
                Cursor cur = sqlDB.rawQuery("select * from tbl_scheduler where date = '" + date
                        + "'and startTime = '" + startTime + "';", null);

                cur.moveToNext();

                int id = cur.getInt(cur.getColumnIndex("id"));

                Calendar calendar = Calendar.getInstance();
                calendar.set(selectedYear, selectedMonth-1, selectedDay, startPicker.getHour(), startPicker.getMinute(), 0);
                // 월이 0~11로 찍혀서 지금까지 1을 더해놨었음 Calendar에 시간계산 할 때는 0~11로 해야함
                long alarmTime = calendar.getTimeInMillis()- alarmMinus ; // 설정된 시간으로 알람설정
                // 알람이 울려야할 시간이 이미 지났다면 그냥 바로 알람 주기
                if(alarmTime < System.currentTimeMillis()){
                    new ScheduleAlarm(getApplicationContext()).alarmOn(System.currentTimeMillis(), id, location, schedule);
                } else {
                    new ScheduleAlarm(getApplicationContext()).alarmOn(alarmTime, id, location, schedule);
                }
                cur.close();
                sqlDB.close();
                dbHelper.close();

                Intent intent = new Intent(AddScheduleActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        sqlDB.close();
        dbHelper.close();
        Intent intent = new Intent(AddScheduleActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

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

