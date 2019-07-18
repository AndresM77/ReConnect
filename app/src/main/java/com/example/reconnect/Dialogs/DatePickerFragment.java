package com.example.reconnect.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.reconnect.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

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
        // Do something with the chosen date
        EditText meetingDate = getActivity().findViewById(R.id.meetingDate);

        // Display the chosen date to app interface
        meetingDate.setText(i + "-" + i1 + "-" + i2);
    }
}
