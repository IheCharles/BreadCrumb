package com.example.tracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.FloatProperty;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import static com.example.tracker.PedometerActivity.orientationAngles;
import static com.example.tracker.PedometerActivity.prev_x;


public class PedometerActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isSensorPresent = false;
    private TextView mStepsSinceReboot;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    public static float[] orientationAngles = new float[3];
    public static float steps=0;
    private TextView mStepsSinceReboot2;
    public static float InitialStepNumber = -1;
    public static float prev_x = 0,prev_y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepsSinceReboot =(TextView) findViewById(R.id.textView1);
        mStepsSinceReboot2 =(TextView) findViewById(R.id.textView2);
        mSensorManager = (SensorManager)
                this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {

            isSensorPresent = true;
        } else {
            isSensorPresent = false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (mSensor != null) {
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
            }
             mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (mSensor != null) {
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        updateOrientationAngles();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (InitialStepNumber==-1){
                InitialStepNumber = event.values[0];
            }


            steps = (event.values[0]-InitialStepNumber);
            //Toast.makeText(this,String.valueOf(steps),Toast.LENGTH_SHORT).show();
            mStepsSinceReboot2.setText( String.valueOf(steps));
            //  DrawingView drawingView=new DrawingView(this,null);



            if (DrawingView.can != null){
                com.example.tracker.DrawingView drawingView1= (com.example.tracker.DrawingView) findViewById(R.id.simpleDrawingView1);
                drawingView1.invalidate();
            }
        }


    }
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
      //  mStepsSinceReboot.setText(String.format("%.2f", (float) (orientationAngles[0])));
        if (orientationAngles[0] < -2.99) {
            mStepsSinceReboot.setText(String.format("%.2f", (float) ((orientationAngles[0]*-1) / (0.01666666666))));
        }else {
            if (orientationAngles[0] >= 0) {
                mStepsSinceReboot.setText(String.format("%.2f", (float) (orientationAngles[0] / (0.01666666666))));
            } else {
                mStepsSinceReboot.setText(String.format("%.2f", (float) (180+((3 - (orientationAngles[0] * -1))  / (0.01666666666)))));
            }
        }
        // "mOrientationAngles" now has up-to-date information.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}


class DrawingView extends View {
    // setup initial color
    private final int paintColor = Color.BLACK;
    Path path;
    // defines paint and canvas
    public static Paint drawPaint;
    public static Canvas can;

    int width ;
    int height;
    float angle;
    Boolean no=false;

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();
        path = new Path();

       width = this.getResources().getDisplayMetrics().widthPixels;
        height = this.getResources().getDisplayMetrics().heightPixels;
        path.moveTo(
                ((float) width / 2),
                ( (float) height / 2));
        path.close();
    }


    // Setup paint with color and stroke styles
    private void setupPaint() {
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);


    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!no) {
            no=true;
            canvas.drawPath(path, drawPaint);
          //  Toast.makeText(this.getContext(),"wack",Toast.LENGTH_SHORT).show();
        }

      can=canvas;
        draw();
    }

    public void draw(){

        drawPaint.setColor(Color.GREEN);
       // Toast.makeText(this.getContext(),String.valueOf(width),Toast.LENGTH_SHORT).show();
        float Compass = orientationAngles[0];
       /* if (Compass < 0.1 || Compass > -0.1) {
            can.drawLine((PedometerActivity.steps + (float) width / 2), (float) (PedometerActivity.steps + (float) height / 2), 0, (PedometerActivity.steps + (float) height / 2), drawPaint);
        }else if( Compass > 2.9 || Compass < -3.1){
            can.drawLine((float) width / 2, (float) height / 2, 0, (PedometerActivity.steps + (float) height / 2), drawPaint);

        }*/


        if (orientationAngles[0] < -2.99) {
            angle = (float) ((orientationAngles[0]*-1) / (0.01666666666));
        }else {
            if (orientationAngles[0] >= 0) {
                 angle =  (float) (orientationAngles[0] / (0.01666666666));
            } else {
                 angle = (float) (180+((3 - (orientationAngles[0] * -1))  / (0.01666666666)));
            }
        }


        if (PedometerActivity.prev_y != 0 || PedometerActivity.prev_x != 0 ) {
            path.moveTo(PedometerActivity.prev_x, PedometerActivity.prev_y);
        }else{
            path.moveTo((float)width/2, (float) height/2);
        }

        if (PedometerActivity.steps != 0) {
            Toast.makeText(this.getContext(),String.valueOf(prev_x),Toast.LENGTH_SHORT).show();
            path.lineTo((float) ((width / 2) + (10*PedometerActivity.steps * Math.cos((angle*Math.PI/180)))), (float) ((height / 2) + (10*PedometerActivity.steps * Math.sin((angle*Math.PI/180)))));
            path.close();
        }else {
            path.lineTo((float) (width / 2) , (float) ((height / 2) ));
            path.close();
        }

        can.drawPath(path,drawPaint);

        PedometerActivity.prev_x = (float) ((width / 2)+(PedometerActivity.steps * Math.cos((angle*Math.PI/180))));
        PedometerActivity.prev_y = (float) ((height / 2) + (PedometerActivity.steps* Math.sin((angle*Math.PI/180))));

    }


}
