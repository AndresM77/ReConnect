package com.example.reconnect.Dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.example.reconnect.RequestMeeting;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private RequestMeeting.TimePickerDoneListener mListener;

    public TimePickerFragment(RequestMeeting.TimePickerDoneListener listener) {
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

        if (datetime.get(Calendar.AM_PM) == Calendar.AM) {
            am_pm = "AM";
        }
        else {
            am_pm = "PM";
        }

        mListener.done(i + ":" + i1 + " " + am_pm);
    }
}