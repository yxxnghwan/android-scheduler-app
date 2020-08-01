package com.example.scheduler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Date;

public class ScheduleAlarm {
    public static final String CHANNEL_ID = "SchedulerID";
    public static final String CHANNEL_NAME = "Scheduler";

    private Context context;
    public ScheduleAlarm(Context context) {
        this.context = context;
    }

    public void alarmOn(long alarmTime, int id, String location, String schedule) {

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_RESTART_SERVICE);
        intent.putExtra("alarmTime", alarmTime);
        intent.putExtra("id", id);    // db에 저장되는 id값으로 알람 id까지 지정할 것. 그래야 꼬일 일 없고 스케줄 삭제시 알람취소도 같이 하기 쉬움
        intent.putExtra("schedule", schedule);
        intent.putExtra("location", location);

        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 버전에 따라 알람 등록 방식이 다르답니다....
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
           am.setExactAndAllowWhileIdle(am.RTC_WAKEUP, alarmTime, sender);
       else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
           am.setExact(am.RTC_WAKEUP, alarmTime, sender);


        Date date = new Date(alarmTime);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Log.d("", c.get(Calendar.YEAR) + "년" + (c.get(Calendar.MONTH)+1) + "월" + c.get(Calendar.DAY_OF_MONTH) + "일" + c.get(Calendar.HOUR_OF_DAY) +"시" + c.get(Calendar.MINUTE) + "분 알람 설정됨");

    }


}
