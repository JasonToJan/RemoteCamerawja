package com.jason.remotecamera_wja.partb;

import android.view.MotionEvent;
import android.view.View;

public class FocusOnTouchListener implements View.OnTouchListener  {
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch(event.getAction()){

            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
        }
        return true;
    }
}
