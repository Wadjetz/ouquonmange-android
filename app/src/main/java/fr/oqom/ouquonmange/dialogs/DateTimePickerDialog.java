package fr.oqom.ouquonmange.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import fr.oqom.ouquonmange.utils.Callback2;

public class DateTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private Callback2<Integer, Integer> callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void setCallback(Callback2<Integer, Integer> callback) {
        this.callback = callback;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (callback != null) {
            callback.apply(hourOfDay, minute);
        }
    }
}
