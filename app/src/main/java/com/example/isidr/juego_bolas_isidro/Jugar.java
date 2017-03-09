package com.example.isidr.juego_bolas_isidro;

/*Created by Isidro on 25/01/2016*/

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Jugar extends SurfaceView implements SurfaceHolder.Callback {

    // Multitouch: To let control more than one drawings
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    //SONIDOS
    private MediaPlayer musicaFondo;
    private MediaPlayer entraUnaBola;
    private MediaPlayer escapaUnaBola;
    private MediaPlayer acabaElJuego;

    private BouncingBallAnimationThread animThread = null;
    //Ejes XY de las bolas
    private int [] middleW;
    private int [] middleH;
    //Dimensiones de la pantalla
    private int width, high;

    //LAS BOLAS
    private Paint [] circles;
    //Colores de las bolas
    private int[] colores = { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.YELLOW };
    //Radio de las bolas
    private int radio = 50;
    //Direcciones donde se moverán la bolas
    private boolean [] avanzarX;
    private boolean [] direccionX;
    private boolean [] avanzarY;
    private boolean [] direccionY;

    // LA PALETA
    //ancho
    private float ancho = 200;
    //altura
    private float altura = 20;
    //la paleta
    private Paint pPalette = new Paint();
    //Posición de la paleta
    private int posPaletaX;
    private int posPaletaY;
    //Última posición tocada
    private float mLastTouchX;
    private float mLastTouchY;

    //OTROS
    //rebotes: número de veces que una bola ha rebotado en la paleta
    private int rebotes;
    //caidas: númerode veces que una bola se ha caido
    private int caidas;
    //velocidad: velocidad a la que se moveran las bolas
    private int velocidad;

    public Jugar (Context context) {
        super(context);
        getHolder().addCallback(this);

        //BOLAS
        //La bolas saldrán de la parte superior de la pantalla, pero variará la dirección y posición
        middleW = new int [5];
        middleH = new int [5];
        //Bola 1: parte superior izquierda
        middleW[0] = 0;
        middleH[0] = 0;
        //Bola 2: parte superior derecha
        middleW[1] = 700;
        middleH[1] = 0;
        //Bola 3: parte superior media
        middleW[2] = 500;
        middleH[2] = 0;
        //Bola 4: parte superior derecha
        middleW[3] = 200;
        middleH[3] = 0;
        //Bola 5: parte superior derecha
        middleW[4] = 600;
        middleH[4] = 0;
        //La paleta aparecerá en esta posición por primera vez
        posPaletaX = 250;
        posPaletaY = 800;
        //Direcciones a las que se dirigirán las bolas
        avanzarX = new boolean[5];
        direccionX = new boolean[5];
        avanzarY = new boolean[5];
        direccionY = new boolean[5];
        //Bola 1: Hacia abajo y hacia la derecha
        avanzarX[0] = true;
        direccionX[0] = true;
        avanzarY[0] = true;
        direccionY[0] = true;
        //Bola 2: Hacia abajo y hacia la derecha
        avanzarX[1] = false;
        direccionX[1] = false;
        avanzarY[1] = true;
        direccionY[1] = true;
        //Bola 3: Hacia abajo y hacia la derecha
        avanzarX[2] = true;
        direccionX[2] = true;
        avanzarY[2] = true;
        direccionY[2] = true;
        //Bola 4: Hacia abajo y hacia la derecha
        avanzarX[3] = true;
        direccionX[3] = true;
        avanzarY[3] = true;
        direccionY[3] = true;
        //Bola 5: Hacia abajo y hacia la derecha
        avanzarX[4] = false;
        direccionX[4] = false;
        avanzarY[4] = true;
        direccionY[4] = true;

        //PALETA
        //La paleta aparecerá en esta posición por primera vez
        posPaletaX = 250;
        posPaletaY = 800;

        //SONIDOS
        //Iniciaremos la música que sonará durante el transcurso del juego
        musicaFondo = MediaPlayer.create(context, R.raw.game);
        musicaFondo.setLooping(true);
        musicaFondo.start();
        //Y iniciaremos los otros sonidos del juego (aunque no se reproduciran hasta que pasen ciertos eventos)
        //escapaUnaBola: saldrá cuando se escape una bola
        escapaUnaBola = MediaPlayer.create(context, R.raw.escapingball);
        escapaUnaBola.setLooping(false);
        //acabaElJuego: saldrá cuando acabe el juego junto a un pop-up diciendo si has ganado o perdido
        acabaElJuego = MediaPlayer.create(context, R.raw.finish);
        acabaElJuego.setLooping(false);

        //OTROS
        rebotes = 0;
        caidas = 0;
        velocidad = 9;
        circles = new Paint[5];
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Start the thread
        if (animThread != null) return;
        animThread = new BouncingBallAnimationThread(getHolder());
        animThread.start(); // To run the animation
    }

    @Override
    public void surfaceChanged (SurfaceHolder holder,int format, int width, int height){

    }

    @Override
    public void surfaceDestroyed (SurfaceHolder holder) {
        animThread.stop = true; // Stops the thread
    }

    //ACCIONES DE LA PALETA AL TOCAR LA PANTALLA
    public boolean onTouchEvent ( MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //Cogemos la posición donde han tocado
                final float x = event.getX();
                final float y = event.getY();
                //Guardar la posición donde hemos tocado
                mLastTouchX = x;
                mLastTouchY = y;
                // Guardar la ID de este puntero
                mActivePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                // Calcular la distancia movida
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;
                // Mover la paleta
                posPaletaX += dx;
                //Si la posición de la paleta es más alta que el máximo de la pantalla o menor que 0, la paleta se quedará
                // ahí. Esto lo que hace es prevenir que la paleta se salga de la pantalla
                if (posPaletaX < 0) posPaletaX = 0;
                if (posPaletaX + ancho >= width) posPaletaX = (int)(width-ancho);
                posPaletaY += dy;
                if (posPaletaY < 0) posPaletaY = 0;
                if (posPaletaY + altura >= high) posPaletaY = (int)(high-altura);
                // Recordar la posición tocada para el próximo evento de movimiento
                mLastTouchX = x;
                mLastTouchY = y;

                invalidate();
                break;
            }
        }
        return true;
    } //end onTouchEvent

    //DIBUJAR TODAS LAS FIGURAS EN LA PANTALLA
    @Override
    public void onDraw (Canvas canvas) {

        if (rebotes >= 50) {

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText("¡Has ganado!", width / 4, high / 2, paint);

            String msg;

            msg = "Rebotes:  "+rebotes+"/50";

            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText(msg, width/4, high/2+100, paint);

            msg = "Caidas:  "+caidas+"/10";

            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText(msg, width/4, high/2+200, paint);

            acabaElJuego.start();
            musicaFondo.stop();
            musicaFondo.release();
        }
        else if (caidas >= 10) {

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText("¡Has perdido!", width / 4, high / 2, paint);

            String msg;

            msg = "Rebotes:  "+rebotes+"/50";

            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText(msg, width/4, high/2+100, paint);

            msg = "Caidas:  "+caidas+"/10";

            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText(msg, width/4, high/2+200, paint);

            acabaElJuego.start();
            musicaFondo.stop();
            musicaFondo.release();
        }
        else {

            //Cogemos las dimensiones de la pantalla
            width = canvas.getWidth();
            high = canvas.getHeight();
            //Pintar el fondo de la pantalla
            canvas.drawColor(Color.WHITE);

            //PALETA
            pPalette.setColor(Color.CYAN);
            canvas.drawRect(posPaletaX, posPaletaY, posPaletaX + ancho, posPaletaY + altura, pPalette);

            //BOLAS
            //Crear y dibujar las bolas
            //Bola 1
            circles[0] = new Paint();
            circles[0].setColor(colores[0]);
            circles[0].setStyle(Paint.Style.FILL);
            canvas.drawCircle(middleW[0], middleH[0], radio, circles[0]);
            //Bola 2
            if (rebotes >= 5) {
                circles[1] = new Paint();
                circles[1].setColor(colores[1]);
                circles[1].setStyle(Paint.Style.FILL);
                canvas.drawCircle(middleW[1], middleH[1], radio, circles[1]);
            }
            //Bola 3
            if (rebotes >= 10) {
                circles[2] = new Paint();
                circles[2].setColor(colores[2]);
                circles[2].setStyle(Paint.Style.FILL);
                canvas.drawCircle(middleW[2], middleH[2], radio, circles[2]);
            }
            //Bola 4
            if (rebotes >= 15) {
                circles[3] = new Paint();
                circles[3].setColor(colores[3]);
                circles[3].setStyle(Paint.Style.FILL);
                canvas.drawCircle(middleW[3], middleH[3], radio, circles[3]);
            }
            //Bola 5
            if (rebotes >= 20) {
                circles[4] = new Paint();
                circles[4].setColor(colores[4]);
                circles[4].setStyle(Paint.Style.FILL);
                canvas.drawCircle(middleW[4], middleH[4], radio, circles[4]);
            }
        }
    }

    private class BouncingBallAnimationThread extends Thread {

        public boolean stop = false;
        private SurfaceHolder surfaceHolder;

        public BouncingBallAnimationThread (SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void run() {
            while (!stop) {

                Canvas c = null;
                try { // Obteniendo el canvas para dibujar
                    c = surfaceHolder.lockCanvas(null);
                    // Dibujando en el canvas
                    synchronized (surfaceHolder) {
                        onDraw(c);
                    }
                }
                finally { //Mostrando el canvas por pantalla
                    if (c != null)
                        surfaceHolder.unlockCanvasAndPost(c);
                }

                if (rebotes >= 50) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
                else if (caidas >= 10) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }

                //Mover las bolas
                // ------------------------------------------------
                //BOLA 1
                moverBola(0);
                chocarBolas(0);
                //BOLA 2
                if (rebotes >= 5) {
                    moverBola(1);
                    /*if (choque(middleW[0], middleH[0], middleW[1], middleH[1], radio)) {
                        if (avanzarX[0] == true && avanzarX[1] == false) {
                            avanzarX[0] = false;
                            direccionX[0] = false;

                            avanzarX[1] = true;
                            direccionX[1] = true;
                        }
                        if (avanzarX[0] == false && avanzarX[1] == true) {
                            avanzarX[0] = true;
                            direccionX[0] = true;

                            avanzarX[1] = false;
                            direccionX[1] = false;
                        }
                        if (avanzarY[0] == true && avanzarY[1] == false) {
                            avanzarY[0] = false;
                            direccionY[0] = false;

                            avanzarY[1] = true;
                            direccionY[1] = true;
                        }
                        if (avanzarY[0] == false && avanzarY[1] == true) {
                            avanzarY[0] = true;
                            direccionY[0] = true;

                            avanzarY[1] = false;
                            direccionY[1] = false;
                        }

                    }*/
                    chocarBolas(1);
                }
                //BOLA 3
                if (rebotes >= 10) {
                    moverBola(2);
                    chocarBolas(2);
                }
                //BOLA 4
                if (rebotes >= 15) {
                    moverBola(3);
                    chocarBolas(3);
                }
                //BOLA 5
                if (rebotes >= 20) {
                    moverBola(4);
                    chocarBolas(4);
                }

            }
        }

        public void moverBola (int numCircle) {

            //Dreta
            if (avanzarX[numCircle]) {
                moverBolaDerecha(numCircle);
            }
            //Esquerre
            else {
                moverBolaIzquierda(numCircle);
            }

            //Baix
            if (avanzarY[numCircle]) {
                moverBolaAbajo(numCircle);
            }
            //Dalt
            else {
                moverBolaArriba(numCircle);
            }

            //Choque de las bolas
            //chocarBolas(numCircle);

            //Menejarse per dreta (true) o esquerre (false)
            if (direccionX[numCircle]) {
                middleW[numCircle] = middleW[numCircle] + velocidad;
            }
            else {
                middleW[numCircle] = middleW[numCircle] - velocidad;
            }

            //Menejarse per baix (true) o dalt (false)
            if (direccionY[numCircle]) {
                middleH[numCircle] = middleH[numCircle] + velocidad;
            }
            else {
                middleH[numCircle] = middleH[numCircle] - velocidad;
            }

        }

        public void moverBolaDerecha (int numCircle) {
            if (middleW[numCircle]+radio < width) {
                direccionX[numCircle] = true;

            }
            else {
                direccionX[numCircle] = false;
                avanzarX[numCircle] = false;
            }
        }

        public void moverBolaIzquierda (int numCircle) {
            if (middleW[numCircle]-radio > 0) {
                direccionX[numCircle] = false;
            }
            else {
                direccionX[numCircle] = true;
                avanzarX[numCircle] = true;
            }
        }

        public void moverBolaAbajo (int numCircle) {
            if (middleH[numCircle]+radio < high) {
                if (middleH[numCircle]+radio >= posPaletaY && middleH[numCircle]+radio <= posPaletaY + altura) {
                    if ((middleW[numCircle]+radio < posPaletaX + ancho && middleW[numCircle]+radio > posPaletaX)
                    || (middleW[numCircle]-radio < posPaletaX + ancho && middleW[numCircle]-radio > posPaletaX)) {
                        rebotes++;
                        direccionY[numCircle] = false;
                        avanzarY[numCircle] = false;
                    }
                    else {
                        direccionY[numCircle] = true;
                    }
                }
                else {
                    direccionY[numCircle] = true;
                }
            }
            else {

                //Si les boles cauen, reapareixeran dalt de tot
                if (numCircle == 0) {
                    middleH[numCircle] = 0;
                    middleW[numCircle] = 0;

                    avanzarX[numCircle] = true;
                    direccionX[numCircle] = true;
                    avanzarY[numCircle] = true;
                    direccionY[numCircle] = true;
                }
                if (numCircle == 1) {
                    middleH[numCircle] = 0;
                    middleW[numCircle] = (int)width;

                    avanzarX[numCircle] = false;
                    direccionX[numCircle] = false;
                    avanzarY[numCircle] = true;
                    direccionY[numCircle] = true;
                }
                if (numCircle == 2) {
                    middleH[numCircle] = 0;
                    middleW[numCircle] = (int)(width/2);

                    avanzarX[numCircle] = true;
                    direccionX[numCircle] = true;
                    avanzarY[numCircle] = true;
                    direccionY[numCircle] = true;
                }
                if (numCircle == 3) {
                    middleH[numCircle] = 0;
                    middleW[numCircle] = (int)(width/4);

                    avanzarX[numCircle] = true;
                    direccionX[numCircle] = true;
                    avanzarY[numCircle] = true;
                    direccionY[numCircle] = true;
                }
                if (numCircle == 4) {
                    middleH[numCircle] = 0;
                    middleW[numCircle] = (int)(width/4)*2;

                    avanzarX[numCircle] = false;
                    direccionX[numCircle] = false;
                    avanzarY[numCircle] = true;
                    direccionY[numCircle] = true;
                }

                caidas++;
                escapaUnaBola.start();

            }
        }

        public void moverBolaArriba (int numCircle) {
            if (middleH[numCircle]-radio > 0) {
                direccionY[numCircle] = false;
            }
            else {
                direccionY[numCircle] = true;
                avanzarY[numCircle] = true;
            }
        }

        public void chocarBolas (int numCircle) {

            //CHOQUE
            // -----------------------------------------------------
            int numBoles = 0;
            if (rebotes >= 5 && rebotes < 10) {
                numBoles = 1;
            }
            else if (rebotes >= 10 && rebotes < 15) {
                numBoles = 2;
            }
            else if (rebotes >= 15 && rebotes < 20) {
                numBoles = 3;
            }
            else if (rebotes >= 20) {
                numBoles = 4;
            }

            for (int i=0 ; i<numBoles ; i++) {
                if (numCircle != i) {
                    if (choque(middleW[numCircle], middleH[numCircle], middleW[i], middleH[i], radio)) {
                        if (avanzarX[numCircle] == true && avanzarX[i] == false) {
                            avanzarX[numCircle] = false;
                            direccionX[numCircle] = false;
                            middleW[numCircle] = middleW[numCircle] - velocidad;

                            avanzarX[i] = true;
                            direccionX[i] = true;
                            middleW[i] = middleW[i] + velocidad;
                        }
                        if (avanzarX[numCircle] == false && avanzarX[i] == true) {
                            avanzarX[numCircle] = true;
                            direccionX[numCircle] = true;
                            middleW[numCircle] = middleW[numCircle] + velocidad;

                            avanzarX[i] = false;
                            direccionX[i] = false;
                            middleW[i] = middleW[i] - velocidad;
                        }
                        if (avanzarY[numCircle] == true && avanzarY[i] == false) {
                            avanzarY[numCircle] = false;
                            direccionY[numCircle] = false;
                            middleH[numCircle] = middleH[numCircle] - velocidad;

                            avanzarY[i] = true;
                            direccionY[i] = true;
                            middleH[i] = middleH[i] + velocidad;
                        }
                        if (avanzarY[numCircle] == false && avanzarY[i] == true) {
                            avanzarY[numCircle] = true;
                            direccionY[numCircle] = true;
                            middleH[numCircle] = middleH[numCircle] + velocidad;

                            avanzarY[i] = false;
                            direccionY[i] = false;
                            middleH[i] = middleH[i] - velocidad;
                        }
                    }
                }
            }
        }

        //Bola 1: x1/y1 /// Bola 2: x2/y2
        public boolean choque (int x1, int y1, int x2, int y2, int radio) {
            double distancia = Math.sqrt(Math.pow((x2-x1), 2)+Math.pow(y2-y1, 2));

            if(distancia > 2*radio) {
                return false;
            }
            else {
                return true;
            }

        }
    }
}
