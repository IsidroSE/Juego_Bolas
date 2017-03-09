package com.example.isidr.juego_bolas_isidro;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/*Created by Isidro on 12/10/2015*/

public class creditos extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CON ESTA ORDEN HACES QUE NO APAREZCA LA BARRITA AZUL CON EL NOMBRE DEL PROYECTO
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_creditos);
    }
}
