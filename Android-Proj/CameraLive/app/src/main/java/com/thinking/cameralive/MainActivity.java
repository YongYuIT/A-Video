package com.thinking.cameralive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_live_push) {
            Intent liveIntent = new Intent(this, CameraPushActivity.class);
            startActivity(liveIntent);
        }
    }
}
