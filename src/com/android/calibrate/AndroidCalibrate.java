package com.android.calibrate;

import com.android.calibrate.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import android.os.SystemProperties;

public class AndroidCalibrate extends Activity {
    final String TAG = "ScreenCalibration";

    final int UI_SCREEN_WIDTH = 480;

    final int UI_SCREEN_HEIGHT =800;
    final int CALIBRATE_GAP =100;

    CrossView myview;

    int direction;

    private Calibrate cal;

    int xList[] = {
            CALIBRATE_GAP, UI_SCREEN_WIDTH - CALIBRATE_GAP, 
		UI_SCREEN_WIDTH - CALIBRATE_GAP, CALIBRATE_GAP, UI_SCREEN_WIDTH / 2
    };

    int yList[] = {
            CALIBRATE_GAP, CALIBRATE_GAP, UI_SCREEN_HEIGHT - CALIBRATE_GAP, 
		UI_SCREEN_HEIGHT - CALIBRATE_GAP, UI_SCREEN_HEIGHT / 2
    };

    static void setNotTitle(Activity act) {
        act.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    static void setFullScreen(Activity act) {
        setNotTitle(act);
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(this);    
        myview = new CrossView(this);
        setContentView(myview);
        
        SystemProperties.set("ts.config.calibrate", "start");
        
        cal = new Calibrate();
        direction = 0;
        
        myview.setOnTouchListener(new OnTouchListener() {
            //@Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("OnTouch", event.getX() + "," + event.getY());
                Log.i("OnTouch getRaw", event.getRawX() + "," + event.getRawY());
                v.invalidate();
                if (direction < 4) {
                    Log.i("CALIBRATE time onTouchListener", " " + direction);
                    cal.get_sample(direction, (int)event.getRawX()*4096/479, (int)event.getRawY()*4096/799,
                            xList[direction], yList[direction]);
                    /*
		    cal.get_sample(direction, (int)event.getX()*4096/479, (int)event.getY()*4096/799,
                            xList[direction], yList[direction]);
		    */
                }
                if (direction == 4) {
                    cal.get_sample(direction, (int)event.getRawX()*4096/479, (int)event.getRawY()*4096/799,
                            xList[direction], yList[direction]);
                    /*
		    cal.get_sample(direction, (int)event.getX(), (int)event.getY(),
                            xList[direction], yList[direction]);
		    */
                    Log.i("CALIBRATE", "calibrate_main");
                    cal.calibrate_main();
		    Log.i("CALIBRATE","p0, x= "+cal.get_cal_x((int)event.getX(),(int)event.getY())+
					 " y= "+cal.get_cal_y((int)event.getX(),(int)event.getY()));
			/*
		    Log.i("CALIBRATE","p0, x= "+cal.get_cal_x(100,100)+" y= "+cal.get_cal_y(100,100));
		    Log.i("CALIBRATE","p1, x= "+cal.get_cal_x(380,100)+" y= "+cal.get_cal_y(380,100));
		    Log.i("CALIBRATE","p2, x= "+cal.get_cal_x(380,700)+" y= "+cal.get_cal_y(380,700));
		    Log.i("CALIBRATE","p3, x= "+cal.get_cal_x(100,700)+" y= "+cal.get_cal_y(100,700));
		    Log.i("CALIBRATE","p4, x= "+cal.get_cal_x(240,400)+" y= "+cal.get_cal_y(240,400));
			*/
                    Toast.makeText(getBaseContext(), "Calibrate Done!", Toast.LENGTH_SHORT).show();
                    SystemProperties.set("ts.config.calibrate", "done");
                    AndroidCalibrate.this.finish();
                }
                direction++;
                return false;
            }
        });
    }

    public class CrossView extends View {
    	final int CAL_GAP =100;
    	final int CAL_LINE =20;
        public CrossView(Context context) {
            super(context);
        }

        public void onDraw(Canvas canvas) {

            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            if (direction == 0) {
                canvas.drawLine((CAL_GAP-CAL_LINE), CAL_GAP, (CAL_GAP+CAL_LINE),CAL_GAP, paint);
                canvas.drawLine(CAL_GAP, (CAL_GAP-CAL_LINE), CAL_GAP, (CAL_GAP+CAL_LINE), paint);
                paint.setColor(Color.WHITE);
            } else if (direction == 1) {
                canvas.drawLine(UI_SCREEN_WIDTH - CAL_GAP-CAL_LINE, CAL_GAP, UI_SCREEN_WIDTH -CAL_GAP+CAL_LINE, CAL_GAP, paint);
                canvas.drawLine(UI_SCREEN_WIDTH - CAL_GAP, CAL_GAP-CAL_LINE, UI_SCREEN_WIDTH - CAL_GAP, CAL_GAP+CAL_LINE, paint);
                paint.setColor(Color.WHITE);

            } else if (direction == 2) {
                canvas.drawLine(UI_SCREEN_WIDTH - CAL_GAP-CAL_LINE, UI_SCREEN_HEIGHT - CAL_GAP, UI_SCREEN_WIDTH - CAL_GAP+CAL_LINE,
                        UI_SCREEN_HEIGHT - CAL_GAP, paint);
                canvas.drawLine(UI_SCREEN_WIDTH - CAL_GAP, UI_SCREEN_HEIGHT - CAL_GAP-CAL_LINE, UI_SCREEN_WIDTH - CAL_GAP,
                        UI_SCREEN_HEIGHT -CAL_GAP+CAL_LINE, paint);
                paint.setColor(Color.WHITE);
            } else if (direction == 3) {
                canvas.drawLine(CAL_GAP-CAL_LINE, UI_SCREEN_HEIGHT - CAL_GAP, CAL_GAP+CAL_LINE, UI_SCREEN_HEIGHT - CAL_GAP, paint);
                canvas.drawLine(CAL_GAP, UI_SCREEN_HEIGHT - CAL_GAP-CAL_LINE, CAL_GAP, UI_SCREEN_HEIGHT -CAL_GAP+CAL_LINE, paint);
                paint.setColor(Color.WHITE);

            } else if (direction == 4) {
                canvas.drawLine(UI_SCREEN_WIDTH / 2 - CAL_LINE, UI_SCREEN_HEIGHT / 2,
                        UI_SCREEN_WIDTH / 2 + CAL_LINE, UI_SCREEN_HEIGHT / 2, paint);
                canvas.drawLine(UI_SCREEN_WIDTH / 2, UI_SCREEN_HEIGHT / 2 - CAL_LINE,
                        UI_SCREEN_WIDTH / 2, UI_SCREEN_HEIGHT / 2 + CAL_LINE, paint);
                paint.setColor(Color.WHITE);
            } else {

            }
            // canvas.drawText(getResources().getString(R.string.
            // screen_calibration_content),
            // UI_SCREEN_WIDTH / 2 - 50, UI_SCREEN_HEIGHT / 2, paint);
            super.onDraw(canvas);
        }
    }
}
