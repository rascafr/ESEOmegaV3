/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseomega;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by François L. on 15/08/2015.
 */
public class GantierActivity extends Activity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private GPGameSurfaceView view;
    private UserProfile profile;

    /** Sensors values **/
    // angular speeds from gyro
    private float[] gyro = new float[3];

    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];

    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];

    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    // Thread position computing
    public static final int TIME_CONSTANT = 25;
    public static final float FILTER_COEFFICIENT = 0.97f;
    private Timer fuseTimer = new Timer();

    // MP3 Player
    private MediaPlayer mediaPlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new GPGameSurfaceView(this);
        setContentView(view);

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        profile = new UserProfile();
        profile.readProfilePromPrefs(this);

        // Create media player only if not playing
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onBackPressed() {
        finish();
        if (mediaPlayer != null)
            mediaPlayer.reset();
    }

    public void initListeners() {
        senSensorManager.registerListener(this,
                senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        senSensorManager.registerListener(this,
                senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        senSensorManager.registerListener(this,
                senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

            /*
             * Fix for 179° <--> -179° transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360° (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360° from the result
             * if it is greater than 180°. This stabilizes the output in positive-to-negative-transition cases.
             */

            // azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }

            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }

            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

            // update sensor output in GUI
            //mHandler.post(updateOreintationDisplayTask);

            // -30 ... +30
            if (view != null && view.getThread() != null)
                //if (Math.abs(x) > 0.2)
                    view.getThread().moveSprite_x(fusedOrientation[2]);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        senSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        senSensorManager.unregisterListener(this);
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);*/
        initListeners();

        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);

        // if not playing
        if (!mediaPlayer.isPlaying()) {
            AssetFileDescriptor descriptor = null;
            try {
                descriptor = getAssets().openFd("wabe.mp3");
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Start music !
        mediaPlayer.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (view != null && view.getThread() != null)
                if (Math.abs(x) > 0.2)
                    view.getThread().moveSprite_x(-x);

            //mView.setBall_y(screen_y);
        }*/

        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array
                // then calculate new orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
    }

    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    public static final float EPSILON = 0.000000001f;

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /** View for game **/
    public class GPGameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

        // Sizes
        private static final double RATIO_MAIN_PLANE = 0.3;
        private static final double RATIO_BOMB = 0.1201;
        private static final double RATIO_LIFE = 0.1201;
        private static final double RATIO_MISSILE = 0.035;
        private static final double RATIO_PATROL = 0.15;
        private static final double RATIO_HEART = 0.090;
        private static final double RATIO_HEART_SPACE = 0.055;
        private static final double RATIO_BUTTON = 0.25788;
        private static final double RATIO_BUG = 0.5;
        private static final int COEF_MOVE_SENSOR = 70;
        private static final double COEF_ORIENTATION_SENSOR = 0.050;
        private static final int MAX_MISSILES = 4;
        private static final int MAX_BOMBS = 4;

        // Game & graphics
        private Context ctx;
        private SurfaceHolder sh;
        private GPGameThread thread;
        private boolean retry;
        private int screen_w = 1000, screen_h = 1000, marginL, marginR, marginB, marginT1, marginT2, marginH1, marginH2, marginB1, marginB2, marginBB1, marginBB2;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint backgrPaint = new Paint();
        private final Paint textPaint = new Paint();
        private final Paint textHeader = new Paint();
        private final Paint scorePaint = new Paint();
        private final Paint bluePaint = new Paint();
        private Random rand = new Random();
        private Bitmap mainPlaneLeft, mainPlaneRight, background, bomb, missile, missile_red, heart, life, bpRestart, bpQuit, bug, patrol;
        private MainPlane mainPlane;
        private BombSprite [] bombSprites = new BombSprite[MAX_BOMBS];
        private MissileSprite [] missileSprites = new MissileSprite[MAX_MISSILES];
        private LifeSprite lifeSprite = new LifeSprite();
        private Vibrator v;
        private Typeface fontScore, fontEnd;

        public GPGameSurfaceView (Context ctx) {
            super(ctx);
            this.ctx = ctx;
            sh = getHolder();
            sh.addCallback(this);
            setFocusable(true);
            v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            fontScore = Typeface.createFromAsset(getAssets(),"fonts/ka1.ttf");
            fontEnd = Typeface.createFromAsset(getAssets(),"fonts/PerfectDOSVGA437.ttf");

            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.FILL);
            backgrPaint.setColor(0xdf151728);
            backgrPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(28);
            scorePaint.setColor(Color.WHITE);
            textHeader.setColor(Color.WHITE);
            textHeader.setTypeface(fontEnd);
            scorePaint.setTypeface(fontScore);
            textPaint.setTypeface(fontEnd);
            bluePaint.setStyle(Paint.Style.FILL);
            bluePaint.setColor(0xff1b488c);
        }

        public GPGameThread getThread () {
            return this.thread;
        }


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            screen_w = getWidth();
            screen_h = getHeight();

            mainPlaneLeft = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_gp_left), (int) (screen_w * RATIO_MAIN_PLANE));
            mainPlaneRight = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_gp_right), (int) (screen_w * RATIO_MAIN_PLANE));
            background = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_sky_better), screen_h);
            bomb = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_bomb), (int) (screen_w * RATIO_BOMB));
            missile = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_missile), (int) (screen_w * RATIO_MISSILE));
            heart = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_heart), (int) (screen_w * RATIO_HEART));
            life = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_matlab), (int) (screen_w * RATIO_LIFE));
            bpRestart = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_retry), (int) (screen_w * RATIO_BUTTON));
            bpQuit = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_quit), (int) (screen_w * RATIO_BUTTON));
            bug = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_bug), (int) (screen_w * RATIO_BUG));
            patrol = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_patrol), (int) (screen_w * RATIO_PATROL));

            mainPlane = new MainPlane();
            mainPlane.x = screen_w/2;
            mainPlane.w = mainPlaneLeft.getWidth();
            mainPlane.h = mainPlaneLeft.getHeight();
            marginL = mainPlane.w/2;
            marginR = screen_w-marginL;
            marginB = screen_h-mainPlane.h/2-mainPlane.h/3;
            mainPlane.y = marginB;
            marginT1 = (int)(screen_h*0.09257);
            marginT2 = (int)(screen_h*0.11995);
            marginH1 = (int)(screen_w*RATIO_HEART);
            marginH2 = (int)(screen_w*RATIO_HEART_SPACE);
            marginB1 = (int)(screen_w*0.21335);
            marginB2 = (int)(screen_w*0.53246);
            marginBB1 = (int) (0.70833*screen_h);
            marginBB2 = (int) (0.70833 * screen_h);
            scorePaint.setTextSize((int) (0.073 * screen_h));

            textHeader.setTextSize((int)(0.085*screen_w));
            textPaint.setTextSize((int)(0.065*screen_w));

            thread = new GPGameThread(sh, ctx, new Handler(), screen_w, screen_h);
            thread.doStart();
            thread.setRunning(true);
            thread.start();
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (thread != null) {

                        if (thread.getStatus() == GPGameThread.GAME_LOOSE) {
                            float x = event.getX();
                            float y = event.getY();

                            if (x >= marginB1 && x < marginB1+bpQuit.getWidth() && y >= marginBB1 && y <= marginBB1+bpQuit.getHeight()) {
                                thread.setRunning(false);
                                senSensorManager.unregisterListener(GantierActivity.this);
                                if (mediaPlayer != null)
                                    mediaPlayer.reset();
                                GantierActivity.this.finish();
                            } else if (x >= marginB2 && x < marginB2+bpRestart.getWidth() && y >= marginBB2 && y <= marginBB2+bpRestart.getHeight()) {
                                thread.setStatus(GPGameThread.GAME_PLAY);
                                thread.doStart();
                            }
                        } else {
                            thread.setGunRunning(true);
                        }

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (thread != null)
                        if (thread.getStatus() == GPGameThread.GAME_PLAY)
                            thread.setGunRunning(false);
                    break;

            }

            return true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            thread.setSurfaceSize(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            thread.setRunning(false);
            retry = true;
            while(retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }



        /** Thread for game operations **/
        public class GPGameThread extends Thread {
            private boolean run = false;
            private int screen_w, screen_h;
            private Handler handler;
            private final SurfaceHolder sh;
            private Context ctx;
            private int backgrY;
            private long s1 = 0, s2, cnt = 0, latchCnt = 0;
            private boolean orientation = false;
            private boolean runGun = false;
            private int score = 0, lives = 3;
            public static final int GAME_PLAY = 0;
            public static final int GAME_LOOSE = 1;
            private int status = GAME_PLAY;
            private boolean stOnce = false;
            private JSONObject jsonScoresObj = null;
            private long lastLive = 0;
            private long delayBomb = 0;
            private boolean netError = false;
            private boolean srvError = false;

            public int getStatus () {
                return status;
            }

            public void setStatus (int status) {
                this.status = status;
            }


            public GPGameThread (SurfaceHolder sh, Context ctx, Handler handler, int screen_w, int screen_h) {
                this.screen_w = screen_w;
                this.screen_h = screen_h;
                this.sh = sh;
                this.handler = handler;
                this.ctx = ctx;
            }

            public void moveSprite_x(float dx) {

                if (status == GAME_PLAY) {
                    mainPlane.x += dx * COEF_MOVE_SENSOR;
                    if (mainPlane.x > marginR)
                        mainPlane.x = marginR;
                    if (mainPlane.x < marginL)
                        mainPlane.x = marginL;

                    if (dx > COEF_ORIENTATION_SENSOR) {
                        orientation = true;
                    } else if (dx < -COEF_ORIENTATION_SENSOR) {
                        orientation = false;
                    }
                }
            }

            public void setGunRunning(boolean runGun) {
                this.runGun = runGun;
            }


            public void setRunning(boolean run) {
                this.run = run;
            }

            // Init game
            public void doStart() {
                synchronized (sh) {
                    backgrY = 0;
                    for (int i=0;i<MAX_BOMBS;i++) {
                        bombSprites[i] = new BombSprite();
                        bombSprites[i].x = screen_w/2;
                        bombSprites[i].y = 0;
                        bombSprites[i].wb = bomb.getWidth();
                        bombSprites[i].hb = bomb.getHeight();
                        bombSprites[i].hp = patrol.getWidth();
                        bombSprites[i].wp = patrol.getHeight();
                        bombSprites[i].step = (int)(screen_h/67.37);
                        bombSprites[i].stepAlien = (int)(screen_h/202.11);
                    }

                    for (int i=0;i<MAX_MISSILES;i++) {
                        missileSprites[i] = new MissileSprite();
                        missileSprites[i].x = screen_w/2;
                        missileSprites[i].y = marginB;
                        missileSprites[i].destroyed = false;
                        missileSprites[i].visible = false;
                        missileSprites[i].w = missile.getWidth();
                        missileSprites[i].h = missile.getHeight();
                        missileSprites[i].step = (int)(screen_h/58);
                    }

                    lifeSprite.visible = false;
                    lifeSprite.y = 0;
                    lifeSprite.w = life.getWidth();
                    lifeSprite.h = life.getHeight();
                    lifeSprite.step = (int)(screen_h/141);
                    stOnce = false;
                    lives = 3;
                    score = 0;
                    jsonScoresObj = null;
                    netError = false;
                    srvError = false;
                    delayBomb = System.currentTimeMillis();
                }
            }

            // Play functions
            public void doDraw(Canvas canvas) {

                canvas.restore();

                // Draw background
                //canvas.drawPaint(backgrPaint);
                //canvas.drawPaint(bluePaint);
                canvas.drawBitmap(background, 0, - screen_h+backgrY, null);
                canvas.drawBitmap(background, 0, backgrY, null);

                // Draw life
                if (lifeSprite.visible) {
                    canvas.drawBitmap(life, lifeSprite.x-lifeSprite.w/2, lifeSprite.y-lifeSprite.h/2, null);
                }

                // Draw bombs
                for (int i=0;i<MAX_BOMBS;i++) {
                    if (bombSprites[i].visible) {
                        if (bombSprites[i].isAlien())
                            canvas.drawBitmap(patrol, bombSprites[i].x-bombSprites[i].getWidth()/2, bombSprites[i].y-bombSprites[i].getHeight()/2, null);
                        else
                            canvas.drawBitmap(bomb, bombSprites[i].x-bombSprites[i].getWidth()/2, bombSprites[i].y-bombSprites[i].getHeight()/2, null);

                        // Debug
                        //canvas.drawText("Missile" + i, missileSprites[i].x+100, missileSprites[i].y, textPaint);
                    }
                }

                // Draw missiles
                for (int i=0;i<MAX_MISSILES;i++) {
                    if (missileSprites[i].visible) {
                        canvas.drawBitmap(missile, missileSprites[i].x - missileSprites[i].w / 2, missileSprites[i].y - missileSprites[i].h / 2, null);
                    }
                }

                // Draw plane
                if (orientation) canvas.drawBitmap(mainPlaneRight, mainPlane.x-mainPlane.w/2, mainPlane.y-mainPlane.h/2, null);
                else canvas.drawBitmap(mainPlaneLeft, mainPlane.x - mainPlane.w / 2, mainPlane.y - mainPlane.h / 2, null);

                // Draw score
                canvas.drawText(String.format("%05d", score), marginH2, marginT1, scorePaint);

                // Draw lifes
                for (int i=0;i<lives;i++) {
                    canvas.drawBitmap(heart, marginH2+i*marginH1, marginT2, null);
                }

                if (status == GAME_LOOSE) {

                    canvas.drawPaint(backgrPaint);
                    canvas.drawText("**** SCORES ****", (int)(screen_w*0.1187), (int)(screen_h*0.13), textHeader);

                    canvas.drawBitmap(bpQuit, marginB1, (int) (0.70833*screen_h), null);
                    canvas.drawBitmap(bpRestart, marginB2, (int) (0.70833 * screen_h), null);

                    // do this once :
                    if (!stOnce) {
                        SyncScores syncScores = new SyncScores(score);
                        syncScores.execute();
                        stOnce = true;
                    }

                    // Now draw scores if != null
                    if (jsonScoresObj != null) {

                        try {
                            int rank = jsonScoresObj.getInt("rank");
                            int bscore = jsonScoresObj.getInt("bscore");
                            JSONArray array = jsonScoresObj.getJSONArray("best");

                            Paint paintHigh = new Paint(textPaint);
                            paintHigh.setColor(0xc0e5e5e5);
                            double dy = screen_h * 0.0375;
                            boolean inRank = false;
                            for (int i = 0; i < 10; i++) {

                                if (i < array.length()) {
                                    JSONObject o = array.getJSONObject(i);
                                    int scoreUser = o.getInt("score");
                                    String user = o.getString("login");
                                    if (user.equals(profile.getId())) {
                                        inRank = true;
                                        canvas.drawText(String.format("%02d", i + 1) + "  " + String.format("%05d", scoreUser) + "  " + user, (int) (screen_w * 0.124), (int) (screen_h * 0.224 + dy * i), textPaint);
                                    }else
                                        canvas.drawText(String.format("%02d", i + 1) + "  " + String.format("%05d", scoreUser) + "  " + user, (int) (screen_w * 0.124), (int) (screen_h * 0.224 + dy * i), paintHigh);
                                } else {
                                    canvas.drawText(String.format("%02d", i + 1) + "  -----  ---------", (int) (screen_w * 0.124), (int) (screen_h * 0.224 + dy * i), paintHigh);
                                }
                            }

                            if (!inRank) canvas.drawText(String.format("%02d", rank) + "  " + String.format("%05d", bscore) + "  " + profile.getId(), (int) (screen_w * 0.124), (int) (screen_h * 0.224 + dy * 11), textPaint);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (netError) {
                        canvas.drawText(" -- Pas de reseau --", (int)(screen_w*0.124), (int)(screen_h*0.224), textPaint);
                        canvas.drawBitmap(bug, screen_w/2-bug.getWidth()/2, screen_h/2-bug.getHeight()/2, null);
                    } else if (srvError) {
                        canvas.drawText(" -- Err.  serveur --", (int)(screen_w*0.124), (int)(screen_h*0.224), textPaint);
                        canvas.drawBitmap(bug, screen_w/2-bug.getWidth()/2, screen_h/2-bug.getHeight()/2, null);
                    }
                }

                // Debug
                //canvas.drawText(latchCnt + " FPS", 10, screen_h-10, textPaint);

                //canvas.drawCircle(bubbleX, bubbleY, 50, paint);
            }

            public final static int     MAX_FPS = 58;
            private final static int    MAX_FRAME_SKIPS = 0;
            private final static int    FRAME_PERIOD = 1000 / MAX_FPS;


            public void run() {

                long beginTime;     // the time when the cycle begun
                long timeDiff;      // the time it took for the cycle to execute
                int sleepTime;      // ms to sleep (<0 if we're behind)
                int framesSkipped;  // number of frames being skipped
                sleepTime = 0;

                while (run) {
                    Canvas c = null;
                    try {
                        c = sh.lockCanvas(null);
                        synchronized (sh) {
                            if (c!=null) {
                                beginTime = System.currentTimeMillis();
                                framesSkipped = 0;
                                c.save();
                                if (status == GAME_PLAY) {
                                    updatePhysics();
                                }
                                doDraw(c);
                                cnt++;
                                timeDiff = System.currentTimeMillis() - beginTime;
                                sleepTime = (int)(FRAME_PERIOD - timeDiff);

                                if (sleepTime > 0) {
                                    try { Thread.sleep(sleepTime); } catch (InterruptedException e) {}
                                }

                                while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                                    if (status == GAME_PLAY || status == GAME_LOOSE) {
                                        updatePhysics();
                                    }
                                    sleepTime += FRAME_PERIOD;
                                    framesSkipped++;
                                }
                            }
                        }
                    } finally {
                        if (c != null) {
                            sh.unlockCanvasAndPost(c);
                        }
                    }
                    if (System.currentTimeMillis() - s1 > 1000) {
                        latchCnt = cnt;
                        s1 = System.currentTimeMillis();
                        cnt = 0;
                    }
                }
            }

            public void setSurfaceSize(int width, int height) {
                synchronized (sh) {
                    screen_w = width;
                    screen_h = height;
                    doStart();
                }
            }

            public void updatePhysics() {

                // Collisions
                double dist;

                backgrY = (backgrY + 2);
                if (backgrY > screen_h)
                    backgrY = 0;

                for (int i=0;i<MAX_MISSILES;i++) {
                    for (int j=0;j<MAX_BOMBS;j++) {
                        dist = Math.sqrt((missileSprites[i].x - bombSprites[j].x) * (missileSprites[i].x - bombSprites[j].x) +
                                         (missileSprites[i].y - bombSprites[j].y) * (missileSprites[i].y - bombSprites[j].y));

                        if (bombSprites[j].visible && isThereCollision(bombSprites[j].x, bombSprites[j].y, bombSprites[j].getWidth(), bombSprites[j].getHeight())) { // Alien attack
                            bombSprites[j].visible = false;
                            lives --;
                            if (lives == -1) {
                                status = GAME_LOOSE;
                            }
                            v.vibrate(100);
                        }

                        if (missileSprites[i].visible && bombSprites[j].visible && dist < bombSprites[i].getWidth()/1.8) {
                            bombSprites[j].actionMissile();
                            if (bombSprites[j].isDead()) {
                                score += bombSprites[j].addedLifes();
                                bombSprites[j].visible = false;
                                bombSprites[j].y = 0;
                            }
                            missileSprites[i].destroyed = true;
                            missileSprites[i].visible = false;
                            //missileSprites[i].y = marginB;

                        }
                    }
                }

                if (lifeSprite.visible && isThereCollision(lifeSprite.x, lifeSprite.y, lifeSprite.w, lifeSprite.h)) { // extra life
                    lifeSprite.visible = false;
                    lifeSprite.y = 0;
                    lives++;
                    v.vibrate(200);
                }

                // Lives bonus : appears if not visible, random boolean agreed and more than 15+ delay and +15 score
                if (lifeSprite.visible){ // life bonus is visible : move it down
                    lifeSprite.y += lifeSprite.step;

                    if (lifeSprite.y > screen_h) {
                        lifeSprite.visible = false;
                        lifeSprite.y = 0;
                    }
                } else if (!lifeSprite.visible && score > 15 && (System.currentTimeMillis()-lastLive) > 8000) {
                    //if (!extendLive.visible && score > 5 && (System.currentTimeMillis()-lastLive) > 15000) {
                    if (rand.nextBoolean()) {
                        lifeSprite.visible = true;
                        lastLive = System.currentTimeMillis();
                        lifeSprite.x = marginL+rand.nextInt(marginR);
                    }
                    lastLive = System.currentTimeMillis();
                }


                // Missiles
                for (int i = 0; i < MAX_MISSILES; i++) {
                    // DO something only if missile is concerned
                    if (!missileSprites[i].visible && !missileSprites[i].destroyed) { // Makes missile out if not destroyed
                        // If previous missile is far away
                        // If i = 0, previous is 3
                        int prev;
                        if (i==0) prev = 3;
                        else prev = i - 1;

                        boolean isFirst = (!missileSprites[0].visible && !missileSprites[1].visible && !missileSprites[2].visible && !missileSprites[3].visible);

                        // Make missile out only if destroy = false
                        if ((isFirst || (marginB - missileSprites[prev].y) >= (marginB/MAX_MISSILES)) && runGun && !missileSprites[i].destroyed) {
                            missileSprites[i].visible = true; // Set active
                            missileSprites[i].destroyed = false; // Fully functional
                            missileSprites[i].x = mainPlane.x; // Set at the bottom of screen, at the top of main sprite
                            missileSprites[i].y = marginB;
                        }
                    } else if (missileSprites[i].visible || (!missileSprites[i].visible && missileSprites[i].destroyed)) { // Missile is visible
                        missileSprites[i].y -= missileSprites[i].step;

                        // Missile is out of screen
                        if (missileSprites[i].y < 0) {
                            missileSprites[i].visible = false;
                            missileSprites[i].destroyed = false;
                            missileSprites[i].y = marginB;
                        }
                    }
                }

                // Aliens
                for (int i=0;i<MAX_BOMBS;i++) {
                    // DO something only if alien is concerned
                    if (!bombSprites[i].visible) { // Alien is invisible

                        // If previous alien is far away
                        // If i = 0, previous is 2
                        int prev;
                        if (i==0) prev = 2;
                        else prev = i - 1;

                        boolean isFirst = (!bombSprites[0].visible && !bombSprites[1].visible && !bombSprites[2].visible);

                        boolean timeoutOk = (System.currentTimeMillis() - delayBomb) > 2000;
                        if (timeoutOk && (isFirst || (bombSprites[prev].y) > marginB/4)) {

                            // If score > 40 et random ~ score 1000% : new kind of alien
                            if (score > 40 && rand.nextInt(1000) < score) {
                                bombSprites[i].setIsAlien(true);
                            } else {
                                bombSprites[i].setIsAlien(false);
                            }
                            bombSprites[i].visible = true; // Set active
                            bombSprites[i].x = marginL+rand.nextInt(marginR); // Set at the bottom of screen, at the top of main sprite
                            bombSprites[i].y = 0;
                        }
                    } else { // Alien is visible
                        //if (alien[i].isPatrol)
                            //alien[i].my += PATROL_STEP_PX;
                        //else
                            bombSprites[i].y += bombSprites[i].getStep();

                        // Alien is out of screen
                        if (bombSprites[i].y > screen_h) {
                            bombSprites[i].visible = false;
                            bombSprites[i].y = 0;
                        }
                    }
                }
            }

            public boolean isThereCollision(int objX, int objY, int objW, int objH) {
                boolean pl1 = (Math.abs(objX - mainPlane.x) < mainPlane.w/7.3 + objW/2) && (Math.abs(objY - mainPlane.y) < (mainPlane.h/2.1 + objH/2.1));
                boolean pl2 = (Math.abs(objX - mainPlane.x) < mainPlane.w/2 + objW/2) && (Math.abs(objY - mainPlane.y) < (mainPlane.h/3.8 + objH/2.1));

                return pl1 || pl2;
            }

            /**
             * Async task to synchronize sores
             */
            private class SyncScores extends AsyncTask<String,String,String> {

                private int score;

                public SyncScores (int score) {
                    this.score = score;
                }

                @Override
                protected String doInBackground(String... params) {

                    HashMap<String, String> pairs = new HashMap<>();
                    pairs.put(GantierActivity.this.getResources().getString(R.string.client), profile.getId());
                    pairs.put(GantierActivity.this.getResources().getString(R.string.password), profile.getPassword());
                    pairs.put(GantierActivity.this.getResources().getString(R.string.score), String.valueOf(score));
                    pairs.put(GantierActivity.this.getResources().getString(R.string.hash),
                            EncryptUtils.sha256(GantierActivity.this.getResources().getString(R.string.HEADER_SYNC_SCORES) + profile.getId() + score + profile.getPassword()));

                    return ConnexionUtils.postServerData(Constants.URL_API_GANTIER_SCORES, pairs, GantierActivity.this);
                }

                @Override
                protected void onPostExecute(String data) {

                    int retCode = -1;
                    String err = "inconnue";

                    if (Utilities.isNetworkDataValid(data)) {
                        try {
                            JSONObject obj = new JSONObject(data);
                            retCode = obj.getInt("status");
                            err = obj.getString("cause");

                            if (retCode == 1) {
                                jsonScoresObj = obj.getJSONObject("data");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            retCode = -8;
                        }
                    }

                    if (retCode != 1) {
                        if (retCode == -8) { // server error
                            srvError = true;
                            netError = false;
                        } else {
                            netError = true;
                            srvError = false;
                        }
                    }
                }
            }
        }
    }

    /** Game JAVA dedicated Classes **/
    private class MainPlane {
        public int x, y, w, h;
    }

    private class BombSprite {
        public int x, y, wb, hb, wp, hp;
        public boolean visible;
        public boolean isAlien = false;
        private int step, stepAlien;
        private int bombLife = 1;
        public void actionMissile () {
            bombLife--;
        }
        public boolean isDead() {
            return bombLife == 0;
        }
        public int addedLifes () {
            return isAlien?4:1;
        }
        public void setSteps (int step, int stepAlien) {
            this.step = step;
            this.stepAlien = stepAlien;
        }
        public boolean isAlien() {
            return isAlien;
        }
        public void setIsAlien(boolean isAlien) {
            this.isAlien = isAlien;
            if (isAlien)
                bombLife = 4;
            else
                bombLife = 1;
        }
        public int getStep () {
            return isAlien?stepAlien:step;
        }
        public int getWidth () {
            return isAlien?wp:wb;
        }
        public int getHeight () {
            return isAlien?hp:hb;
        }
    }

    private class LifeSprite {
        public int x, y, w, h;
        public boolean visible;
        public int step = 12;
    }

    private class MissileSprite {
        public int x, y, w, h;
        public boolean visible;
        public boolean destroyed;
        public int step = 22;
    }

    // width scaling
    public Bitmap getResizedBitmap(Bitmap bm, int newSize) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scale = ((float) newSize) / width;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale, scale);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    public Bitmap getScaledResizedBitmap(Bitmap bm, int newSize) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scale;

        if (height > width) { // portrait bitmap
            scale = ((float) newSize) / height;
        } else { // landscape bitmap
            scale = ((float) newSize) / width;
        }

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale, scale);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }
}