package fr.oqom.ouquonmange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Intent intent = getIntent();
        String uuid =  intent.getStringExtra("uuid_community");
        Toast.makeText(getApplicationContext(), uuid, Toast.LENGTH_LONG).show();

    }
}
