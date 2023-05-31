package com.example.atarigame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.graphics.Color;
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
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    GameSurface gameSurface;
    int counter = 30;
    int score = -1;
    TextView endText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        new CountDownTimer(30000, 1000){
            public void onTick(long millisUntilFinished){
                counter--;
            }
            public void onFinish(){
                gameSurface.terminate();
                setContentView(R.layout.activity_main);
                endText = findViewById(R.id.textView);
                endText.setText("The Game is Over!\nYour Final Score Is: "+score);
            }
        }.start();
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
        Bitmap bike;
        Bitmap wreck;
        float ballX = 440f;
        float ballY = 0;
        float bikeY = 10000f;
        float bikeX = 0f;
        float tempX, tempY;
        boolean alive = true;
        int flip = 0;
        int x = 200;
        Paint paintProperty, timePaint;
        int screenWidth;
        int screenHeight;
        private SensorManager sensorManager;
        private Sensor sensor;
        boolean end = false;

        public GameSurface(Context context) {
            super(context);
            holder = getHolder();
            ball = BitmapFactory.decodeResource(getResources(), R.drawable.car);
            background = BitmapFactory.decodeResource(getResources(), R.drawable.street);
            wreck = BitmapFactory.decodeResource(getResources(), R.drawable.wreckage);
            bike = BitmapFactory.decodeResource(getResources(), R.drawable.bike);
            ball = Bitmap.createScaledBitmap(ball, 200,330,/*filter=*/false);
            bike = Bitmap.createScaledBitmap(bike, 150,200,/*filter=*/false);
            wreck = Bitmap.createScaledBitmap(wreck, 150,200,/*filter=*/false);
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);

            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            paintProperty = new Paint();
            paintProperty.setColor(Color.BLACK);
            paintProperty.setTextSize(80);
            paintProperty.setFakeBoldText(true);
            timePaint = new Paint();
            timePaint.setColor(Color.BLACK);
            timePaint.setTextSize(50);

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            flip = (int) (30 * sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        public void resume(){
            if(!end)
            {
                running = true;
                gameThread = new Thread(this);
                gameThread.start();
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        public void pause(){
            if(!end)
            {
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
        }

        public void terminate(){
            end = true;
            sensorManager.unregisterListener(this);
            running = false;
            gameThread.interrupt();
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
                ballY = (screenHeight) - 2 * ball.getHeight();

                Log.d("here",""+screenWidth);

                canvas.drawBitmap(ball, ballX, ballY, null);

                if(alive) {
                    canvas.drawBitmap(bike,bikeX,bikeY,null);
                } else {
                    canvas.drawBitmap(wreck,tempX,tempY,null);
                }

                if(ballX >= screenWidth-ball.getWidth()-30 && flip > 0) {
                    flip = 0;
                }
                if(ballX <= 30 && flip < 0){
                    flip = 0;
                }
                ballX += flip;

                if(bikeY > screenHeight)
                {
                    bikeY = -200;
                    bikeX = (((int)(Math.random()*3) * 2 + 1)* (screenWidth / 6)) - (bike.getWidth() / 2);
                    if(alive)
                    {
                        score++;
                    }
                    alive = true;
                }

                if((bikeY + bike.getHeight()) >= ballY && bikeY <= (ballY + ball.getHeight()))
                {
                    if((bikeX + bike.getWidth()) >= ballX && bikeX <= (ballX + ball.getWidth())) {
                        if(alive) {
                            alive = false;
                            tempX = bikeX;
                            tempY = bikeY;
                            score--;
                        }
                    }
                }
                bikeY += 10;
                canvas.drawText("Score: "+score, screenWidth/2-140, 140, paintProperty);
                canvas.drawText("Time Left: "+counter, screenWidth/2-140, 220, timePaint);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}