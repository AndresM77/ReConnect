package com.example.reconnect.Dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.example.reconnect.Activities.RequestMeetingActivity;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private RequestMeetingActivity.TimePickerDoneListener mListener;

    public TimePickerFragment(RequestMeetingActivity.TimePickerDoneListener listener) {
        mListener = listener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, false);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        Calendar datetime = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, i);
        datetime.set(Calendar.MINUTE, i1);
        String am_pm;

        String AM_PM = " AM";
        String mm_precede = "";
        if (i >= 12) {
            AM_PM = " PM";
            if (i >=13 && i < 24) {
                i -= 12;
            }
            else {
                i = 12;
            }
        } else if (i == 0) {
            i = 12;
        }

        mListener.done(i + ":" + i1 + " " + AM_PM);
    }
}