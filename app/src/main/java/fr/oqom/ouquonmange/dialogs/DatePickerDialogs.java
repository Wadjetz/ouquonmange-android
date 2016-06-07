package fr.oqom.ouquonmange.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import fr.oqom.ouquonmange.utils.Callback;

public class DatePickerDialogs extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Callback<Calendar> callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        if (callback != null) {
            callback.apply(calendar);
        }
    }

    public void setCallback(Callback<Calendar> callback) {
        this.callback = callback;
    }
}
