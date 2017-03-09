package com.example.isidr.juego_bolas_isidro;

/*Created by Isidro on 28/01/2016.*/

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class Jugar_Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new Jugar(this));
    }
}
