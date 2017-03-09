package com.example.isidr.juego_bolas_isidro;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button jugar, creditos;
    private MediaPlayer reproductor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CON ESTA ORDEN HACES QUE NO APAREZCA LA BARRIT AZUL CON EL NOMBRE DEL PROYECTO
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        jugar = (Button)findViewById(R.id.jugar);
        creditos = (Button)findViewById(R.id.acerca);
        //Declaramos la música
        reproductor = MediaPlayer.create(this, R.raw.menu);
        //Con esta orden si termina la música, se volverá a repetir de nuevo infinitas veces
        reproductor.setLooping(true);
        //Ahora empezaremos a reproducir la música
        reproductor.start();

        //Si pulsamos el botón Jugar, empezará el juego
        jugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nuevoform = new Intent(MainActivity.this, Jugar_Launcher.class);
                startActivity(nuevoform);
            }
        });

        //Si pulsamos el botón AcercaDe, nos llevará a los créditos donde veremos el nombre del desarrollador, versión, etc.
        creditos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nuevoform = new Intent(MainActivity.this, creditos.class);
                startActivity(nuevoform);
            }
        });
    }


    //Si cerramos el programa, la música se parará
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reproductor.isPlaying()) {
            reproductor.stop();
            reproductor.release();
        }
    }

    //Si minimizamos el programa, la música se pausará
    @Override
    protected void onPause() {
        super.onPause();
        reproductor.pause();
    }

    //Cuando reabramos la ventana, la música volvera a reproducirse desde el punto donde se quedó
    @Override
    protected void onResume () {
        super.onResume();
        reproductor.start();
    }
}
