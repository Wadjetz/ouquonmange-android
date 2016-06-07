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
import fr.oqom.ouquonmange.utils.Callback3;

public class DatePickerDialogs extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Callback3<Integer, Integer, Integer> callback;

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
        if (callback != null) {
            callback.apply(year, monthOfYear, dayOfMonth);
        }
    }

    public void setCallback(Callback3<Integer, Integer, Integer> callback) {
        this.callback = callback;
    }
}
