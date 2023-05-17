package com.example.atarigame;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    GameSurface gameSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener{
        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap ball;
        Bitmap background;
        int ballX = 0;
        int ballY = 0;
        int flip = 0;
        int x = 200;
        Paint paintProperty;
        int screenWidth;
        int screenHeight;
        private SensorManager sensorManager;
        private Sensor sensor;

        public GameSurface(Context context) {
            super(context);
            holder = getHolder();
            ball = BitmapFactory.decodeResource(getResources(), R.drawable.car);
            background = BitmapFactory.decodeResource(getResources(), R.drawable.street);
            ball = Bitmap.createScaledBitmap(ball, 200,330,/*filter=*/false);
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);

            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;
            paintProperty = new Paint();

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            flip = (int) (-20 * sensorEvent.values[2]);
            Log.d("Sensor", ""+sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        public void resume(){
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        public void pause(){
            sensorManager.unregisterListener(this);
            running = false;
            while(true){
                try{
                    gameThread.join();
                }
                catch(Exception e){

                }
            }
        }

        @Override
        public void run() {
            Canvas canvas = null;
            Drawable d = getResources().getDrawable(R.drawable.street, null);
            while(running){
                if(holder.getSurface().isValid() == false)
                    continue;
                canvas = holder.lockCanvas(null);
                d.setBounds(getLeft(), getTop(), getRight(), getBottom());
                d.draw(canvas);

                canvas.drawBitmap(ball, (screenWidth/2)-(ball.getWidth()/2)+ballX, (screenHeight)-2*ball.getHeight(), null);
                if(ballX >= screenWidth/2-ball.getWidth() && flip > 0) {
                    flip = 0;
                }
                if(ballX <= -1*screenWidth/2+ball.getWidth() && flip < 0){
                    flip = 0;
                }
                ballX += flip;
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}