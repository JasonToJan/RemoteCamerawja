package com.jason.remotecamera_wja.test;

/**
 * Created by jasonjan on 2018/5/9.
 */

public abstract class MyTimerCheck {
    private int mCount = 0;
    private int mTimeOutCount = 1;
    private int mSleepTime = 1000; // 1s
    private boolean mExitFlag = false;
    private Thread mThread = null;

    public abstract void doTimerCheckWork();

    public abstract void doTimeOutWork();

    public MyTimerCheck() {
        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!mExitFlag) {
                    mCount++;
                    if (mCount < mTimeOutCount) {
                        doTimerCheckWork();
                        try {
                            mThread.sleep(mSleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            exit();
                        }
                    } else {
                        doTimeOutWork();
                    }
                }
            }
        });
    }

    public void start(int timeOutCount, int sleepTime) {
        mTimeOutCount = timeOutCount;
        mSleepTime = sleepTime;

        mThread.start();
    }

    public void exit() {
        mExitFlag = true;
    }

}
