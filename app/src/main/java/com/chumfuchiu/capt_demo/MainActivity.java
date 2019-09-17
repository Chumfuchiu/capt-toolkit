package com.chumfuchiu.capt_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.chumfuchiu.capt_annotation.BindView;
import com.chumfuchiu.capt_toolkit.BindViewTools;

public class MainActivity extends AppCompatActivity {

    @BindView(resId = R.id.main_tv)
    TextView tvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewTools.bind(this);
//        tvMain.setText("aaaaaaaaaaaa");
    }
}
