package com.android.calibrate;

import com.android.calibrate.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
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
import android.view.Display;
import android.util.DisplayMetrics;
import android.widget.Toast;
import android.os.SystemProperties;
import android.content.res.Configuration;

public class AndroidCalibrate extends Activity {
    final String TAG = "ScreenCalibration";

    final int DEFAULT_SCREEN_WIDTH = 480;

    final int DEFAULT_SCREEN_HEIGHT =800;
    final int CALIBRATE_GAP =100;

    int screen_width ;
    int screen_hight ;
    int orientation;
    int rawX;
    int rawY;
    int w;
    int h;
	
    CrossView myview;

    int direction;
    int cal_index;

    private Calibrate cal;

   int xList[] = {
           CALIBRATE_GAP, DEFAULT_SCREEN_WIDTH - CALIBRATE_GAP,
		   	DEFAULT_SCREEN_WIDTH - CALIBRATE_GAP, CALIBRATE_GAP, DEFAULT_SCREEN_WIDTH / 2
   }; 
 
   int yList[] = {
            CALIBRATE_GAP, CALIBRATE_GAP, DEFAULT_SCREEN_HEIGHT - CALIBRATE_GAP,
				DEFAULT_SCREEN_HEIGHT - CALIBRATE_GAP, DEFAULT_SCREEN_HEIGHT / 2
   };

    static void setNotTitle(Activity act) {
        act.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    static void setFullScreen(Activity act) {
        setNotTitle(act);
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //add by WOOJOY TJD
    void setDisplayStatus(Activity act) {
	WindowManager windowManager = getWindowManager();

	Display display = windowManager.getDefaultDisplay();
	screen_width = display.getWidth();
	screen_hight = display.getHeight();

	orientation = act.getResources().getConfiguration().orientation;


	if (DEFAULT_SCREEN_WIDTH != screen_width
		||DEFAULT_SCREEN_HEIGHT != screen_hight) {
		xList[0] = CALIBRATE_GAP;
		yList[0] = CALIBRATE_GAP;
		xList[1] = screen_width - CALIBRATE_GAP;
		yList[1] = CALIBRATE_GAP;
		xList[2] = screen_width - CALIBRATE_GAP;
		yList[2] = screen_hight - CALIBRATE_GAP;
		xList[3] = CALIBRATE_GAP;
		yList[3] = screen_hight - CALIBRATE_GAP;
		xList[4] = screen_width / 2;
		yList[4] = screen_hight / 2;
	}

	w = screen_width;
	h = screen_hight;
	if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
		for (int i=0; i<5; i++) {
			int tmp = xList[i];
			xList[i] = screen_hight - yList[i];
			yList[i] = tmp;
		}	
		w = screen_hight;
		h = screen_width;
	}	
  }
//end add by TJD

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(this);    
	setDisplayStatus(this);

	Log.i("CALIBRATE screen_width", " " + screen_width);
	Log.i("CALIBRATE screen_hight", " " + screen_hight);
	Log.i("CALIBRATE orientation", " " + orientation);
	
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

		 //add by WOOJOY TJD
		  rawX = (int)event.getRawX();
		  rawY = (int)event.getRawY();
		  if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
		  	switch(direction) {
				case 0: cal_index = 1; break;
				case 1: cal_index = 2; break;
				case 2: cal_index = 3; break;
				case 3: cal_index = 0; break;
				case 4: cal_index = 4; break;	
			}
			int tmp = rawX;
			rawX = screen_hight - rawY;
			rawY = tmp;
		  } else {
			cal_index = direction;
		  }
		  rawX = rawX*4096/w;
		  rawY = rawY*4096/h;
		  //end add

		 Log.i("OnTouch rawX:Y", rawX  +":"+ rawY);
                if (direction < 4) {
                    Log.i("CALIBRATE time onTouchListener", " " + direction);
                    cal.get_sample(cal_index, rawX, rawY, xList[direction], yList[direction]);
                } else  if (direction == 4) {
                   cal.get_sample(cal_index, rawX, rawY, xList[direction], yList[direction]);

                    Log.i("CALIBRATE", "calibrate_main");
                    if ( -2 == cal.calibrate_main()) {
				Toast.makeText(getBaseContext(), "Calibrate failed, do again!", Toast.LENGTH_SHORT).show();
				SystemProperties.set("ts.config.calibrate", "start");
				direction = -1;
		 	} else {
			      Log.i("CALIBRATE","p0, x= "+cal.get_cal_x((int)event.getX(),(int)event.getY())+
						 " y= "+cal.get_cal_y((int)event.getX(),(int)event.getY()));
	                    Toast.makeText(getBaseContext(), "Calibrate Done!", Toast.LENGTH_SHORT).show();
	                    SystemProperties.set("ts.config.calibrate", "done");
	                    AndroidCalibrate.this.finish();
			}
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
                canvas.drawLine(screen_width - CAL_GAP-CAL_LINE, CAL_GAP, screen_width -CAL_GAP+CAL_LINE, CAL_GAP, paint);
                canvas.drawLine(screen_width - CAL_GAP, CAL_GAP-CAL_LINE, screen_width - CAL_GAP, CAL_GAP+CAL_LINE, paint);
                paint.setColor(Color.WHITE);

            } else if (direction == 2) {
                canvas.drawLine(screen_width - CAL_GAP-CAL_LINE, screen_hight - CAL_GAP, screen_width - CAL_GAP+CAL_LINE,
                        screen_hight - CAL_GAP, paint);
                canvas.drawLine(screen_width - CAL_GAP, screen_hight - CAL_GAP-CAL_LINE, screen_width - CAL_GAP,
                        screen_hight -CAL_GAP+CAL_LINE, paint);
                paint.setColor(Color.WHITE);
            } else if (direction == 3) {
                canvas.drawLine(CAL_GAP-CAL_LINE, screen_hight - CAL_GAP, CAL_GAP+CAL_LINE, screen_hight - CAL_GAP, paint);
                canvas.drawLine(CAL_GAP, screen_hight - CAL_GAP-CAL_LINE, CAL_GAP, screen_hight -CAL_GAP+CAL_LINE, paint);
                paint.setColor(Color.WHITE);

            } else if (direction == 4) {
                canvas.drawLine(screen_width / 2 - CAL_LINE, screen_hight / 2,
                        screen_width / 2 + CAL_LINE, screen_hight / 2, paint);
                canvas.drawLine(screen_width / 2, screen_hight / 2 - CAL_LINE,
                        screen_width / 2, screen_hight / 2 + CAL_LINE, paint);
                paint.setColor(Color.WHITE);
            } else {

            }
            // canvas.drawText(getResources().getString(R.string.
            // screen_calibration_content),
            // screen_width / 2 - 50, screen_hight / 2, paint);
            super.onDraw(canvas);
        }
    }
}
