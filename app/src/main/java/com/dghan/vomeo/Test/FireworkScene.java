package com.dghan.vomeo.Test;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.ColorUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

enum FireworkSceneAnimateState {
    //Firework state
    asReady, asRunning, asPause
}

enum AnimationState{
    Idling, Moving, Exploding, Done
}
//IDEA:
    /*
    We represent the firework as a circle
        Each firework will be fired from a starting point until it reaches the given destination
            (which is usually randomly generated)
        The firework will move and gradually slow down using the easing equation
        Once it reaches the destination, the firework explodes into the given number of fragments
        Each fragment will be shot out randomly from the core of the explosion and gradually fade out
            using the easing equation
        For easing: http://www.gizma.com/easing/
     */

class Misc {
    static boolean isDebugging = false;
    private static int backgroundAlpha = 255;            //For fun and effect :D
    static int backgroundColor = Color.WHITE ;
    static float screenHeight;
    static float screenWidth;

    //Firework parameters
    //Timing
    static long minDuration = 2000;
    static long maxDuration = 3000;
    static int minFragment = 50;
    static int maxFragment = 100;
    static float minFireworkRadius = 20;
    static float maxFireworkRadius = 30;                //Radius of the firework in pixel
    static int fireworkPerBatch = 3;                    //Number of firework shot at once
    //Coordination
    //Min and colorMax possible spawn and target positions
    static Vector2D minPos;
    static Vector2D maxPos;

    //Fragment stuff
    static int minRotationSpeed = 4;
    static int maxRotationSpeed = 9;

    //Color stuff
    private static int colorMin = 100;
    private static int colorMax = 205;
    private static int colorRange = colorMax - colorMin;

    static void CalculateBoundary(int width, int height){
        screenHeight = height;
        screenWidth = width;

        minPos = new Vector2D(screenWidth * 0.3f, screenHeight * 0.1f);
        maxPos = new Vector2D(screenWidth * 0.7f, screenHeight * 0.5f);
    }
    static void ChangeBackgroundAlpha(int alpha){
        if (alpha < 0 || alpha > 255){
            ColorUtils.setAlphaComponent(backgroundColor, backgroundAlpha);
        } else {
            ColorUtils.setAlphaComponent(backgroundColor, alpha);
        }
    }
    static void ResetBackgroundAlpha(){
        backgroundColor = ColorUtils.setAlphaComponent(backgroundColor, backgroundAlpha);
    }
    static void MatchBackgroundColor(){

    }

    //Scale number from one col orRange to another
    static float MapRange(float unscaledNum, float minAllowed, float maxAllowed, float min, float max) {
        return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
    }
    //Random color
    static int RandomColor(Random random){
        int red = random.nextInt() % colorRange + colorMin;
        int blue = random.nextInt() % colorRange + colorMin;
        int green = random.nextInt() % colorRange + colorMin;
        return Color.rgb(red, green, blue);
    }

    //Check whether 2 coords are near each other within acceptable colorRange
    static boolean Near(float parm1, float parm2, float epsilon){
        return Math.abs(parm1 - parm2) <= epsilon;
    }
    /**
     * The basic function for easing.
     * @param t is the current time (or position) of the tween.
     *          This can be seconds or frames, steps, seconds, ms, whatever –
     *          as long as the unit is the same as is used for the total time [3].
     * @param b is the beginning value
     * @param c is the change between the beginning and destination value of the property.
     * @param d is the total time of the tween.
     * @return the eased value
     */
    static double EaseOutCirc(float t, float b, float c, float d) {
        if (t >= d){
            return c + b;
        }
        t /= d;
        t--;
        return c * Math.sqrt(1 - t*t) + b;
    }
    static double EaseOutQuart(float t, float b, float c, float d){
        if (t >= d){
            return c + b;
        }
        t /= d;
        t--;
        return -c * (t*t*t*t - 1) + b;
    }
    static double EaseOutCubic(float t, float b, float c, float d){
        if (t >= d){
            return c + b;
        }
        t /= d;
        t--;
        return c*(t*t*t + 1) + b;
    }
    static double LinearTween(float t, float b, float c, float d){
        if (t >= d){
            return c + b;
        }
        return c*t/d + b;
    }
    static double EaseInQuint(float t, float b, float c, float d){
        if (t >= d){
            return c + b;
        }
        t /= d;
        return c*Math.pow(t, 4) + b;
    }
    static double EaseOutQuint(float t, float b, float c, float d){
        if (t >= d){
            return c + b;
        }
        t /= d;
        t--;
        return c*(t*t*t*t*t + 1) + b;
    }
}

class Vector2D{
    float x;
    float y;
    Vector2D(float x, float y){
        this.x = x;
        this.y = y;
    }

    //Some special matrix
    static Vector2D[] GetStandardRotationMatrix(float angle){
        angle = (float) Math.toRadians(angle);
        return new Vector2D[] {new Vector2D((float) Math.cos(angle), (float) Math.sin(angle)),
                new Vector2D((float)-Math.sin(angle), (float) Math.cos(angle))};
    }

    void Add(Vector2D force){
        x += force.x;
        y += force.y;
    }
    void Mult(float parm){
        x *= parm;
        y *= parm;
    }
    //Right side matrix multiplication -> used for transformation
    void Mult(Vector2D[] matrix){
        if (matrix.length != 2){
            return;
        }

        x = matrix[0].x * x + matrix[1].x * y;
        y = matrix[0].y * x + matrix[1].y * y;
    }
    void Mult(float parm1, float parm2){
        x *= parm1;
        y *= parm2;
    }

    Vector2D Clone(){
        return new Vector2D(x, y);
    }
}

class FireworkGun{
    private int maxNumberOfFirework = 20;
    private LinkedList<Firework> freeFireworkList;  //List of free fireworks
    private LinkedList<Firework> shotFireworkList;  //List of shot fireworks
    private long timer;                             //We shoot firework on periodically
    private long fireworkInterval = 1500;
    private Random random;

    private Fragment[] fragmentList;      //Explosion from gun
    private LinkedList<Fragment> workingFragment;

    FireworkGun(){
        //Initialize
        random = new Random();
        freeFireworkList = new LinkedList<>();
        shotFireworkList = new LinkedList<>();
        for(int indx = 0; indx < maxNumberOfFirework; indx++){
            //Add a bunch of firework
            freeFireworkList.add(new Firework(random));
        }

//        fragmentList = new Fragment[Misc.maxFragment];
//        workingFragment = new LinkedList<>();
//        for(int indx = 0; indx < Misc.maxFragment; indx++){
//            //Explode into a bunch of fragment
//            fragmentList[indx] = new Fragment(random);
//        }
        timer = System.currentTimeMillis();
    }

    private void StartFirework(Firework firework){
        Vector2D startPos = new Vector2D(random.nextInt((int) (Misc.maxPos.x - (Misc.maxPos.x == 0 ? -1 : Misc.minPos.x))) + Misc.minPos.x,
                Misc.screenHeight);
//        //And explode at gun point
//        int numberOfFragment = random.nextInt(Misc.maxFragment == Misc.minFragment ? 1 : Misc.maxFragment - Misc.minFragment) + Misc.minFragment / 9;
//        for(int indx = 0; indx < numberOfFragment; indx++){
//            float fragRadius = (random.nextFloat() * (Misc.maxFireworkRadius - Misc.minFireworkRadius) + Misc.minFireworkRadius)
//                    * (Misc.MapRange(random.nextFloat(), 0.2f, 0.5f, 0, 1));
//            fragmentList[indx].StartFragment(startPos, fragRadius, Color.argb(255, 210, 0, 0));
//            workingFragment.add(fragmentList[indx]);
//        }

        firework.StartFirework(startPos);
    }
    void Update(Canvas canvas, Paint paint){
        //Each fireworkInterval millisecond, shoot another firework
        if (System.currentTimeMillis() - timer >= fireworkInterval && freeFireworkList.size() > 0){
            for(int indx = 0; indx < Misc.fireworkPerBatch; indx++){
                //Shoot the next free firework if available
                StartFirework(freeFireworkList.getFirst());
                shotFireworkList.add(freeFireworkList.removeFirst());
            }

            timer = System.currentTimeMillis();
        }

        //Redraw all the firework
//        for(Firework firework : shotFireworkList){
//            firework.Update(canvas, paint);
//        }

//        for(int indx = workingFragment.size() - 1; indx >= 0; indx--){
//            Fragment fragment = workingFragment.get(indx);
//            if (fragment.state != DucAnimationState.Done){
//                fragment.Update(canvas, paint);
//            } else {
//                 workingFragment.remove(indx);
//            }
//        }

        //Redraw and Check for free firework
        for(int indx = shotFireworkList.size() - 1; indx >= 0; indx--){
            Firework firework = shotFireworkList.get(indx);
            if (firework.state == AnimationState.Done){
                firework.state = AnimationState.Idling;
                freeFireworkList.add(shotFireworkList.remove(indx));
            } else {
                firework.Update(canvas, paint);
            }
        }
    }
}
class Firework{
    //Basic info
    private long fireworkDuration = 1500;    //How long does it take for the firework to get to the destination? In millisecond
    private float radius = 5;
    private int color;
    public AnimationState state = AnimationState.Idling;
    private Random random;

    private Vector2D startPos;
    private Vector2D endPos;
    private long startTime;

    //Fragment info
    private Fragment[] fragmentList;
    private int numberOfFragment;

    //Some trailing
    private LinkedList<Vector2D> trailHistory;
    private int maxTrail = 20;

    Firework(Random random){
        this.random = random;
        fragmentList = new Fragment[Misc.maxFragment];
        trailHistory = new LinkedList<>();
        for(int indx = 0; indx < fragmentList.length; indx++){
            fragmentList[indx] = new Fragment(random);
        }
    }

    void StartFirework(Vector2D startPos){
        //Randomize stuff
        this.fireworkDuration = Math.abs(random.nextLong() % (Misc.maxDuration - Misc.minDuration)) + Misc.minDuration;
        this.radius = random.nextFloat() * (Misc.maxFireworkRadius - Misc.minFireworkRadius) + Misc.minFireworkRadius;
        //Since at first, when the view has not been fully initialized, all the colorMax and colorMin width and height are 0
        //  therefore, we need to + 1 to make the colorRange [0, bound) works ( [0, 0) will break the program!)
        float targetX = random.nextInt((int) (Misc.maxPos.x - (Misc.maxPos.x == 0 ? -1 : Misc.minPos.x))) + Misc.minPos.x;
        float targetY = random.nextInt((int) (Misc.maxPos.y - (Misc.maxPos.y == 0 ? -1 : Misc.minPos.y))) + Misc.minPos.y;

        //startPos.x is randomize between 2 x boundary of the screen
        //startPos.y is at the bottom of the screen
        this.startPos = startPos;
        this.endPos = new Vector2D(targetX, targetY);
        this.startTime = System.currentTimeMillis();

        //Alpha is always at colorMax
        color = Misc.RandomColor(random);

        //FOR DEBUGGING PURPOSE
        if (Misc.isDebugging){
            startPos = new Vector2D(500, 0);
            color = Color.WHITE;
            this.radius = 5;
        }

        trailHistory.clear();
        //Start moving
        state = AnimationState.Moving;
    }
    private void Explode(float centerX, float centerY){
        numberOfFragment = random.nextInt(Misc.maxFragment == Misc.minFragment ? 1 : Misc.maxFragment - Misc.minFragment) + Misc.minFragment;
        state = AnimationState.Exploding;

        for(int indx = 0; indx < numberOfFragment; indx++){
            float radius = this.radius * (Misc.MapRange(random.nextFloat(), 0.2f, 0.5f, 0, 1));
            fragmentList[indx].StartFragment(new Vector2D(centerX, centerY), radius, null);
        }
    }

    //Fragment thingy
    private boolean CheckFreeFragments(){
        for(Fragment fragment : fragmentList){
            if (fragment.state != AnimationState.Done){
                return false;
            }
        }
        return true;
    }
    private void DrawShape(Canvas canvas, Paint paint, Vector2D pos, int alpha, float radius){
        paint.setAlpha(alpha);
        canvas.drawCircle(pos.x, pos.y, radius, paint);
    }

    void Update(Canvas canvas, Paint paint){
        switch(state){
            case Moving:
                //Simple, draw a line until we reach destination :D
                paint.setColor(color);

                long curTime = System.currentTimeMillis() - startTime;
                float nextX = (float) Misc.LinearTween(curTime, startPos.x, endPos.x - startPos.x, fireworkDuration);
                float nextY = (float) Misc.EaseOutQuint(curTime, startPos.y, endPos.y - startPos.y, fireworkDuration);

                int curAlpha = 255;
                float curRadius = radius;
                for(Vector2D trail: trailHistory){
                    DrawShape(canvas, paint, trail, curAlpha *= 0.6f, curRadius *= 0.8f);
                }
                trailHistory.addFirst(new Vector2D(nextX, nextY));
                if (trailHistory.size() > maxTrail){
                    trailHistory.removeLast();
                }

                if ((Misc.Near(nextX, endPos.x, 100) && Misc.Near(nextY, endPos.y, 20)) || curTime > fireworkDuration){
                    paint.setColor(Misc.backgroundColor);
                    Explode(nextX, nextY);
                }

                DrawShape(canvas, paint, new Vector2D(nextX, nextY), 255, radius);
                //canvas.drawCircle(nextX, nextY, radius, paint);
                break;
            case Exploding:
                for (int indx = 0; indx < numberOfFragment; indx++) {
                    fragmentList[indx].Update(canvas, paint);
                }

                if (CheckFreeFragments()){
                    //All fragment finish moving
                    state = AnimationState.Done;
                    numberOfFragment = 0;
                }
                break;
        }
    }
}
class Fragment{
    //Credit to: https://codepen.io/rajatkantinandi/pen/bQNedV
    //Basic info
    public AnimationState state = AnimationState.Done;

    private Vector2D vel;           //Velocity of the fragment
    private Vector2D acc;           //Acceleration of the fragment
    private Vector2D pos;           //Current position

    private float radius = 2;
    private long duration = 0;
    private long startTime = 0;

    private int color;
    private Random random;

    //For random shape
    enum Shape{
        Circle, Triangle, Square;

        public static Shape GetRandomShape(Random random){
            return values()[random.nextInt(values().length)];
        }
    }
    private Shape curShape;
    private float curRotation;              //Current rotation of the shape
    private float rotationSpeed = 7;        //Angle / second
    private Vector2D[] baseVertexList;

    Fragment(Random random){
        this.random = random;
        acc = new Vector2D(0, 1);  //Gravity!
        baseVertexList = new Vector2D[4];//{new Vector2D(0, 0), new Vector2D(0, 0), new Vector2D(0, 0), new Vector2D(0, 0)};
    }

    void StartFragment(Vector2D startPos, float radius, Integer color){
        pos = startPos;
        vel = new Vector2D(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);     //Normalized vector for direction
        vel.Mult(random.nextInt(81) + 30);                                       //Multiply by the length
        rotationSpeed = random.nextInt(Misc.maxRotationSpeed - Misc.minRotationSpeed) + Misc.minRotationSpeed;

        this.radius = radius;
        this.duration = (long) ((random.nextLong() % (Misc.maxFragment - Misc.minDuration) + Misc.minDuration) * 0.5f);

        this.startTime = System.currentTimeMillis();

        //Alpha is always at colorMax
        if (color == null){
            this.color = Misc.RandomColor(random);
        } else {
            this.color = color;
        }

        //Randomize shape that will be drawn
        curShape = Shape.GetRandomShape(random);
        curRotation = 0;

        switch (curShape){
            case Circle:
                this.radius *= 1.3f;
                break;
            case Triangle:
                this.radius *= 2.5f; //Scale up a little bit

                //This will first consider (0, 0) as the centroid's coordination and then translate using the current position
                baseVertexList[0] = new Vector2D(0, this.radius);
                baseVertexList[1] = new Vector2D((float)-Math.sqrt(3) / 2.0f * this.radius, -this.radius / 2.0f);
                baseVertexList[2] = new Vector2D((float) Math.sqrt(3) / 2.0f * this.radius, -this.radius / 2.0f);
                break;
            case Square:
                this.radius *= 3f; //Scale up a little bit

                //The center of the square is (0,0)
                baseVertexList[0] = new Vector2D( this.radius * (float) Math.sqrt(2) / 4.0f, this.radius * (float) Math.sqrt(2) / 4.0f);
                baseVertexList[1] = new Vector2D( this.radius * (float) Math.sqrt(2) / 4.0f, -this.radius * (float) Math.sqrt(2) / 4.0f);
                baseVertexList[2] = new Vector2D( -this.radius * (float) Math.sqrt(2) / 4.0f, -this.radius * (float) Math.sqrt(2) / 4.0f);
                baseVertexList[3] = new Vector2D( -this.radius * (float) Math.sqrt(2) / 4.0f, this.radius * (float) Math.sqrt(2) / 4.0f);
                break;
        }

        state = AnimationState.Moving;
    }

    private void DrawCircle(Canvas canvas, Paint paint){
        canvas.drawCircle(pos.x, pos.y, radius, paint);
    }
    private void DrawShape(Canvas canvas, Paint paint, int point){
        //Set up the canvas and paint
//        paint.setStrokeWidth(2);
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        Vector2D standardRotationMatrix[] = Vector2D.GetStandardRotationMatrix(curRotation);
        Vector2D curPos[] = new Vector2D[point];

        //Do the special case first
        //Copy the original vertex
        curPos[0] = baseVertexList[0].Clone();
        //Rotate the vertex
        curPos[0].Mult(standardRotationMatrix);
        //Then translate it
        curPos[0].Add(pos);
        //And draw it :D
        path.moveTo(curPos[0].x, curPos[0].y);

        for(int indx = 1; indx < point; indx++){
            //Copy the original vertex
            curPos[indx] = baseVertexList[indx].Clone();
            //Rotate the vertex
            curPos[indx].Mult(standardRotationMatrix);
            //Then translate it
            curPos[indx].Add(pos);
            //And draw it :D
            path.lineTo(curPos[indx].x, curPos[indx].y);
        }
        path.lineTo(curPos[0].x, curPos[0].y);
        canvas.drawPath(path, paint);
    }

    void Update(Canvas canvas, Paint paint){
        switch(state) {
            case Moving:
                vel.Mult(0.85f);    //Slow down
                vel.Add(acc);   //Add acceleration vector to velocity vector
                pos.Add(vel);   //Add velocity vector to position vector

                //Simple, draw a circle until we reach destination :D
                long curTime = System.currentTimeMillis() - startTime;
                //Life span of the fragment <=> fragment's alpha
                int lifeSpan = (int) Misc.LinearTween(curTime, 255, -255, duration);
                paint.setColor(color);
                paint.setAlpha(lifeSpan);

                curRotation += rotationSpeed;
                switch(curShape){
                    case Circle:
                        DrawCircle(canvas, paint);
                        break;
                    case Triangle:
                        DrawShape(canvas, paint, 3);
                        break;
                    case Square:
                        DrawShape(canvas, paint, 4);
                        break;
                }

                if (curTime >= duration || lifeSpan <= 0) {
                    state = AnimationState.Done;
                }
                break;
        }
    }
}

public class FireworkScene extends SurfaceView implements  SurfaceHolder.Callback {
    class GameThread extends Thread
    {
        private boolean mRun = false;

        private SurfaceHolder surfaceHolder;
        private FireworkSceneAnimateState state;
        private Context context;
        private Handler handler;
        private Paint paint;

        private FireworkGun mGun;

        GameThread( SurfaceHolder surfaceHolder, Context context, Handler handler )
        {
            this.surfaceHolder = surfaceHolder;
            this.context = context;
            this.handler = handler;

            Misc.CalculateBoundary(getWidth(), getHeight());
            Misc.ResetBackgroundAlpha();

            paint = new Paint();
            paint.setStrokeWidth( 2 / getResources().getDisplayMetrics().density );
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias( true );

            mGun = new FireworkGun();
        }

        //Overwrite stuff
        void doStart()
        {
            synchronized ( surfaceHolder )
            {
                setState( FireworkSceneAnimateState.asRunning );
            }
        }

        public void pause()
        {
            synchronized ( surfaceHolder )
            {
                if ( state == FireworkSceneAnimateState.asRunning )
                    setState( FireworkSceneAnimateState.asPause );
            }
        }
        public void unpause()
        {
            setState( FireworkSceneAnimateState.asRunning );
        }
        @Override
        public void run()
        {
            while ( mRun )
            {
                Canvas c = null;
                try
                {
                    c = surfaceHolder.lockCanvas( null );

                    synchronized ( surfaceHolder )
                    {
                        if ( state == FireworkSceneAnimateState.asRunning )
                            doDraw( c );
                    }
                }
                finally
                {
                    if ( c != null )
                    {
                        surfaceHolder.unlockCanvasAndPost( c );
                    }
                }
            }
        }

        public void setRunning( boolean b )
        {
            mRun = b;
        }
        public void setState( FireworkSceneAnimateState state )
        {
            synchronized ( surfaceHolder )
            {
                this.state = state;
            }
        }

        //Treat this as Update each frame
        void doDraw( Canvas canvas )
        {
            canvas.drawColor(Misc.backgroundColor);

            mGun.Update(canvas, paint);
        }

        void setSurfaceSize( int width, int height )
        {
            synchronized ( surfaceHolder )
            {
//                for(int indx = 0; indx < fireworkList.size(); indx++){
//                    fireworkList.get(indx).Reshape(width, height);
//                }
            }
        }
    }

    private GameThread thread;

    @SuppressLint( "HandlerLeak" )
    public FireworkScene( Context context )
    {
        super( context );

        SurfaceHolder holder = getHolder();
        holder.addCallback( this );

        getHolder().addCallback( this );

        thread = new GameThread( holder, context, new Handler() {
            @Override
            public void handleMessage( Message m ) {
            }} );

        setFocusable( true );
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
    {
        thread.setSurfaceSize( width, height );
        Misc.CalculateBoundary(width, height);
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder )
    {
        thread.setRunning( true );
        thread.doStart();
        thread.start();
    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder )
    {
        boolean retry = true;
        thread.setRunning( false );

        while ( retry )
        {
            try
            {
                thread.join();
                retry = false;
            }
            catch ( InterruptedException e )
            {
                System.out.println("Can't destroy surface!");
            }
        }
    }

    public void Stop(){
        thread.setRunning(false);
    }
}