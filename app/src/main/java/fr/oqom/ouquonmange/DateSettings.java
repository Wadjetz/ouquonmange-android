package fr.oqom.ouquonmange;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.Toast;

/**
 * Created by hedhili on 21/05/2016.
 */
public class DateSettings implements DatePickerDialog.OnDateSetListener {
    Context context;
    public DateSettings(Context context){
        this.context = context;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Toast.makeText(context, "calendar : "+year+" / "+monthOfYear+" / "+dayOfMonth, Toast.LENGTH_LONG).show();
    }
}
