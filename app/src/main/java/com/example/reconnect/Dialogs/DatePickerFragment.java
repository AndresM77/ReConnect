package com.example.reconnect.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.example.reconnect.Activities.RequestMeetingActivity;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private RequestMeetingActivity.DatePickerDoneListener mDatePickerDoneListener;

    public DatePickerFragment(RequestMeetingActivity.DatePickerDoneListener datePickerDoneListener) {
        mDatePickerDoneListener = datePickerDoneListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month,day);
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        // Display the chosen date to app interface
        mDatePickerDoneListener.done(i + "-" + i1 + "-" + i2);
    }
}
