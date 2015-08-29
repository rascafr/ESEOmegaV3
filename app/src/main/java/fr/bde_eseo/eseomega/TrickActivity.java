package fr.bde_eseo.eseomega;

import android.app.Activity;
import android.content.Context;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.rascafr.test.matdesignfragment.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.bde_eseo.eseomega.model.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;

/**
 * Created by François on 20/04/2015.
 */
public class TrickActivity extends Activity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private MyView mView;
    private final static int GAME_PLAY = 0;
    private final static int GAME_LOOSE = 1;
    private int gameStatus = GAME_PLAY;
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profile = new UserProfile();
        profile.readProfilePromPrefs(this);
        mView = new MyView(this, Typeface.createFromAsset(getAssets(),"fonts/ka1.ttf"), Typeface.createFromAsset(getAssets(),"fonts/PerfectDOSVGA437.ttf"));
        //mView.
        setContentView(mView);
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN)
                    mView.setMissile(true);
                else if(action == MotionEvent.ACTION_UP)
                    mView.setMissile(false);
                return true;
            }
        });

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            mView.moveSprite_x(-x);
            //mView.setBall_y(screen_y);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class Missile {
        public boolean visible;
        public int mx, my;
        public Canvas c;

        public Missile () {
            visible = false;
            my = 0;
        }
    }

    public class Alien {
        public boolean visible;
        public int mx, my;
        public Canvas c;
        public boolean isPatrol;
        public int lives;
        public final static int LIVES_MINE = 1;
        public final static int LIVES_PATROL = 4;

        public Alien (boolean isPatrol) {
            visible = false;
            my = 0;
            this.isPatrol = isPatrol;
            if (isPatrol) lives = LIVES_PATROL;
            else lives = LIVES_MINE;
        }

        public Alien () {
            this(false);
        }

        public void setPatrol () {
            isPatrol = true;
            lives = LIVES_PATROL;
        }

        public void resetPatrol () {
            isPatrol = false;
            lives = LIVES_MINE;
        }

        public boolean isDead () {
            return lives == 0;
        }

    }

    public class ExtendLive {
        public boolean visible;
        public int mx, my;

        public ExtendLive () {
            visible = false;
            my = 0;
        }
    }

    public class MyView extends SurfaceView implements SurfaceHolder.Callback {

        private int screen_x = 1000, screen_y = 1000;
        private final static int SPRITE_MAIN_SIZE = 200;
        private final static int SPRITE_CUBE_SIZE = 90;
        private final static int SPRITE_MISSILE_SIZE = 60;
        private final static int SPRITE_HEART_SIZE = 70;
        private final static int SPRITE_LIVE_SIZE = 70;
        private final static int LIMITS_BORDER = SPRITE_MAIN_SIZE / 2;
        private final static int MISSILES_SIZE_X = 8;
        private final static int MISSILES_SIZE_Y = 40;
        private final static int MISSILES_STEP_PX = 60;
        private final static int MAX_MISSILES = 4;

        private final static int ALIEN_SIZE_X = 40;
        private final static int ALIEN_SIZE_Y = 40;
        private final static int ALIEN_STEP_PX = 30;
        private final static int PATROL_STEP_PX = 23;
        private final static int LIVES_STEP_PX = 20;
        private final static int MAX_ALIENS = 3;

        private final static String LOOSE_MSG = "YOU LOOSE !";

        private Random rand = new Random();
        private int score = 0;
        private int lives = 3;
        private long lastLive = 0;
        private int sprite_x = 300;
        private int sprite_y = 300;
        private boolean orientation = false; // false = left
        private boolean isMissileEnabled = false;
        private boolean isStarted = false;
        private Typeface font, fontEnd;
        private Vibrator v;
        private long[] pattern = {0, 150, 200, 150, 200, 150, 200};
        private Paint paint, paintEnd;
        private long startTime;
        private boolean st = false;
        private double playtime = 0.0;
        private SurfaceHolder sh;

        Missile missile[] = new Missile[MAX_MISSILES];
        Alien alien[] = new Alien[MAX_ALIENS];
        ExtendLive extendLive = new ExtendLive();

        private Bitmap gp_l, gp_r, gp, mine, patrol, bMissile, bLive;
        private Bitmap heart;

        public MyView(Context context, Typeface font, Typeface fontEnd) {
            super(context);
            sh = getHolder();
            sh.addCallback(this);
            gp_l = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_gp_left), SPRITE_MAIN_SIZE);
            gp_r = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trick_gp_right),SPRITE_MAIN_SIZE);
            heart = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.heart),SPRITE_HEART_SIZE);
            mine = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pixel),SPRITE_CUBE_SIZE);
            patrol = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.starpatrol),SPRITE_CUBE_SIZE);
            bMissile = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.missile),SPRITE_MISSILE_SIZE);
            bLive = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.matlab),SPRITE_LIVE_SIZE);
            this.font = font;
            this.fontEnd = font;
            paint = new Paint();
            paintEnd = new Paint();
            paint.setTypeface(font);
            paintEnd.setTypeface(fontEnd);
            v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            startTime = System.currentTimeMillis();
        }

        public void moveSprite_x(float dx) {

            if (gameStatus == GAME_PLAY) {

                this.sprite_x += dx * 10;
                if (sprite_x > screen_x - LIMITS_BORDER)
                    sprite_x = screen_x - LIMITS_BORDER;
                if (sprite_x < LIMITS_BORDER)
                    sprite_x = LIMITS_BORDER;

                if (dx > 0.6) {
                    orientation = false;
                } else if (dx < -0.6) {
                    orientation = true;
                }
            }
        }

        public void setMissile (boolean en) {
            isMissileEnabled = en;
        }
/*
        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);


        }
*/
        public void updatePhysics() {

            int bottom = screen_y - gp.getHeight() - gp.getHeight()/3;


            // Collisions
            for (int i=0;i<MAX_MISSILES;i++) {
                int mx = missile[i].mx;
                int my = missile[i].my;

                for (int j = 0; j < MAX_ALIENS; j++) {
                    int ax = alien[j].mx;
                    int ay = alien[j].my;
                    double dist = Math.sqrt((mx - ax) * (mx - ax) + (my - ay) * (my - ay));

                    if (alien[j].visible && isThereCollision(mine, alien[j].mx, alien[j].my, gp, sprite_x, sprite_y)) { // Alien attack
                        alien[j].visible = false;
                        lives--;
                        v.vibrate(100);
                    }

                    if (missile[i].visible && alien[j].visible && dist < mine.getWidth() / 1.9) { // If collision, destroy missile and alien
                        missile[i].visible = false;
                        alien[j].lives--;
                        if (alien[j].isDead()) {
                            alien[j].visible = false;
                            alien[j].my = 0;
                            score += (alien[j].isPatrol ? 4:1);
                        }
                    }
                }
            }
            if (extendLive.visible && isThereCollision(bLive, extendLive.mx, extendLive.my, gp, sprite_x, sprite_y)) { // extra life
                extendLive.visible = false;
                extendLive.my = 0;
                lives++;
                v.vibrate(200);
            }

            // Lifes
            if (lives == -1) {
                gameStatus = GAME_LOOSE;
                v.vibrate(pattern, -1);
            }

            // Lives bonus : appears if not visible, random boolean agreed and more than 15+ delay and +15 score
            if (extendLive.visible){ // life bonus is visible : move it down
                extendLive.my += LIVES_STEP_PX;

                if (extendLive.my > screen_y) {
                    extendLive.visible = false;
                    extendLive.my = 0;
                }
            } else if (!extendLive.visible && score > 15 && (System.currentTimeMillis()-lastLive) > 8000) {
            //if (!extendLive.visible && score > 5 && (System.currentTimeMillis()-lastLive) > 15000) {
                if (rand.nextBoolean()) {
                    extendLive.visible = true;
                    lastLive = System.currentTimeMillis();
                    extendLive.mx = rand.nextInt(screen_x);
                }
                lastLive = System.currentTimeMillis();
            }



            // Missiles
            for (int i=0;i<MAX_MISSILES;i++) {
                // DO something only if missile is concerned
                if (!missile[i].visible) { // Missile is invisible

                    // If previous missile is far away
                    // If i = 0, previous is 3
                    int prev;
                    if (i==0) prev = 3;
                    else prev = i - 1;

                    boolean isFirst = (!missile[0].visible && !missile[1].visible && !missile[2].visible && !missile[3].visible);

                    if ((isFirst || (bottom - missile[prev].my) > bottom/4) && isMissileEnabled) {

                        missile[i].visible = true; // Set active
                        missile[i].mx = sprite_x; // Set at the bottom of screen, at the top of main sprite
                        missile[i].my = bottom;
                    }
                } else if (missile[i].visible || (!missile[i].visible && missile[i].my != bottom)) { // Missile is visible
                    missile[i].my -= MISSILES_STEP_PX;

                    // Missile is out of screen
                    if (missile[i].my < 0) {
                        missile[i].visible = false;
                        missile[i].my = bottom;
                    }
                }
            }


            // Aliens
            for (int i=0;i<MAX_ALIENS;i++) {
                // DO something only if alien is concerned
                if (!alien[i].visible) { // Alien is invisible

                    // If previous alien is far away
                    // If i = 0, previous is 2
                    int prev;
                    if (i==0) prev = 2;
                    else prev = i - 1;

                    boolean isFirst = (!alien[0].visible && !alien[1].visible && !alien[2].visible);

                    if ((isFirst || (alien[prev].my) > screen_y/4)) {

                        // If score > 40 et random ~ score 1000% : new kind of alien
                        if (score > 40 && rand.nextInt(1000) < score) {
                            alien[i].setPatrol();
                        } else {
                            alien[i].resetPatrol();
                        }
                        alien[i].visible = true; // Set active
                        alien[i].mx = rand.nextInt(screen_x); // Set at the bottom of screen, at the top of main sprite
                        alien[i].my = 0;
                    }
                } else { // Alien is visible
                    if (alien[i].isPatrol)
                        alien[i].my += PATROL_STEP_PX;
                    else
                        alien[i].my += ALIEN_STEP_PX;

                    // Alien is out of screen
                    if (alien[i].my > screen_y) {
                        alien[i].visible = false;
                        alien[i].my = 0;
                    }
                }
            }


        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Canvas canvas = sh.lockCanvas();

            Log.d("Surface", "Created");

            screen_x = getWidth();
            screen_y = getHeight();

            if (orientation)
                gp = gp_l;
            else
                gp = gp_r;

            sprite_y = screen_y - gp.getHeight() / 2 - gp.getHeight() / 6;

            if (!isStarted) {
                for (int i=0;i<MAX_MISSILES;i++) {
                    missile[i] = new Missile();
                    missile[i].my = screen_y - gp.getHeight();
                }
                for (int i=0;i<MAX_ALIENS;i++)
                    alien[i] = new Alien();
                isStarted = true;
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xff1b488c);
            canvas.drawPaint(paint);
            paint.setColor(Color.parseColor("#f0f0f0"));
            paint.setTextSize(96);


            // Missiles
            if (gameStatus == GAME_PLAY) updatePhysics();

            for (int i=0;i<MAX_MISSILES;i++) {

                if (missile[i].visible) {

                    canvas.drawBitmap(bMissile, missile[i].mx - bMissile.getWidth()/2, missile[i].my - bMissile.getHeight()/2, null);/*
                    canvas.drawRect(missile[i].mx - MISSILES_SIZE_X / 2, missile[i].my - MISSILES_SIZE_Y / 2,
                            missile[i].mx + MISSILES_SIZE_X / 2, missile[i].my + MISSILES_SIZE_Y / 2,
                            paint);*/
                }
            }

            // Aliens
            for (int i=0;i<MAX_ALIENS;i++) {

                if (alien[i].visible) {
                    if (alien[i].isPatrol)
                        canvas.drawBitmap(patrol, alien[i].mx - patrol.getWidth()/2, alien[i].my - patrol.getHeight()/2, null);
                    else
                        canvas.drawBitmap(mine, alien[i].mx - mine.getWidth()/2, alien[i].my - mine.getHeight()/2, null);
                    /*
                    canvas.drawRect(alien[i].mx - ALIEN_SIZE_X / 2, alien[i].my - ALIEN_SIZE_Y / 2,
                            alien[i].mx + ALIEN_SIZE_X / 2, alien[i].my + ALIEN_SIZE_Y / 2,
                            paint);*/
                }
            }

            // Life bonus
            if (extendLive.visible) {
                canvas.drawBitmap(bLive, extendLive.mx - bLive.getWidth()/2, extendLive.my - bLive.getHeight()/2, null);
            }

            // Main character
            canvas.drawBitmap(gp, this.sprite_x - gp.getWidth() / 2, this.sprite_y - gp.getHeight() / 2, null);

            // Score
            canvas.drawText(String.format("%05d", score), 25, 100, paint);

            // Vies restantes
            for (int i=1;i<=lives;i++)
                canvas.drawBitmap(heart, 25 + (SPRITE_HEART_SIZE + 15) * (i-1), 140, null);

            // Loose message and scores
            if (gameStatus == GAME_LOOSE) {
                paint.setColor(0xdf202030);
                canvas.drawPaint(paint);
                paint.setColor(Color.parseColor("#f0f0f0"));
                paint.setTextSize(48);
                paint.setTypeface(font);
                centerTextCanvas(LOOSE_MSG, canvas, paint);
                paintEnd.setColor(Color.parseColor("#f0f0f0"));
                paintEnd.setTextSize(24);


                if (!st) {
                    playtime = (System.currentTimeMillis() - startTime)/1000.0;
                    Log.d("GAME", "User is " + profile.getName());
                    SyncScores syncScores = new SyncScores(score, holder, screen_x, screen_y);
                    syncScores.execute();
                    st = true;
                }
                /*int dx = 20, dy = screen_y/2+130;
                canvas.drawText("#### read if u're a dev ####", dx, dy, paintEnd);
                canvas.drawText("Memory Alloc GL : " + playtime + " secs.", dx, dy+50, paintEnd);
                canvas.drawText("MSQL database : connect : [FAIL] (NETW_DISABL)", dx, dy+100, paintEnd);
                canvas.drawText("syncScores.php : POST method", dx, dy+150, paintEnd);
                canvas.drawText("php > SHA-1 value is : [OK] 0.0214 sec", dx, dy+200, paintEnd);
                canvas.drawText("php > SHA-1 value is : [OK] 0.0214 sec", dx, dy+250, paintEnd);
                canvas.drawText("SQL request : ORDER BY ASCENT 0,5", dx, dy+300, paintEnd);
                canvas.drawText("---", dx, dy+350, paintEnd);
                canvas.drawText("\"I HATE THIS HACKER CRAP\"", dx, dy+400, paintEnd);*/
            }

            this.postInvalidateDelayed( 1000 / 35);

            sh.unlockCanvasAndPost(canvas);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    public boolean isThereCollision(Bitmap obj, int objX, int objY, Bitmap plane, int planeX, int planeY) {
        boolean pl1 = (Math.abs(objX - planeX) < plane.getWidth()/7.3 + obj.getWidth()/2) && (Math.abs(objY - planeY) < (plane.getHeight()/2.1 + obj.getHeight()/2.1));
        boolean pl2 = (Math.abs(objX - planeX) < plane.getWidth()/2 + obj.getWidth()/2) && (Math.abs(objY - planeY) < (plane.getHeight()/3.8 + obj.getHeight()/2.1));

        return pl1 || pl2;
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newSize) {
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

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void centerTextCanvas (String text, Canvas canvas, Paint paint) {
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
        paint.setTextAlign(Paint.Align.CENTER);
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        canvas.drawText(text, xPos, yPos, paint);
    }

    /**
     * Async task to synchronize sores
     */
    private class SyncScores extends AsyncTask<String,String,String> {

        private int score;
        private SurfaceHolder holder;
        private int screenW, screenH;

        public SyncScores (int score, SurfaceHolder holder, int screenW, int screenH) {
            this.score = score;
            this.holder = holder;
            this.screenW = screenW;
            this.screenH = screenH;
        }

        @Override
        protected String doInBackground(String... params) {

            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("client", profile.getId()));
            pairs.add(new BasicNameValuePair("score", "" + score));
            pairs.add(new BasicNameValuePair("hash", EncryptUtils.sha256(TrickActivity.this.getResources().getString(R.string.SALT_SYNC_SCORES) + profile.getId() + score)));
            String gameResp = ConnexionUtils.postServerData(Constants.URL_GPGAME_POST_SCORES, pairs);

            return gameResp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject obj = new JSONObject(result);
                int rank = obj.getInt("rank");
                int bscore = obj.getInt("bscore");
                JSONArray array = obj.getJSONArray("best");
                String best = "\n";

                //Log.d("GAME", "Scores : " + rank + ", " + bscore + best);

                Canvas canvas = holder.lockCanvas();
                Paint paint = new Paint();
                paint.setColor(0xffffffff);
                paint.setTextSize(15);
                int dx = 20, dy = screenH/2+130;
                if (canvas != null) {
                    for (int i=0;i<array.length();i++) {
                        JSONObject o = array.getJSONObject(i);
                        canvas.drawText(o.getString("login") + "  " + o.getInt("score"), dx, dy, paint);
                        dy += 30;
                        //best += o.getString("login") + "  " + o.getInt("score") + "\n";
                    }
                    holder.unlockCanvasAndPost(canvas);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
