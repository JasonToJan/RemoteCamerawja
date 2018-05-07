package com.jason.remotecamera_wja.sqlite.sqlitea;

import android.database.sqlite.SQLiteDatabase;

import com.jason.remotecamera_wja.InitApp;

public class DbManagerA {
    private static DbManagerA manager;
    private SQLiteOpenHelperA SQLiteOpenHelperA;
    private SQLiteDatabase db;

    /**
     * 私有化构造器
     */
    private DbManagerA() {
        //创建数据库
        SQLiteOpenHelperA = SQLiteOpenHelperA.getInstance(InitApp.AppContext);
        if (db == null) {
            db = SQLiteOpenHelperA.getWritableDatabase();
        }
    }

    /**
     * 单例DbManager类
     *
     * @return 返回DbManager对象
     */
    public static DbManagerA newInstances() {
        if (manager == null) {
            manager = new DbManagerA();
        }
        return manager;
    }

    /**
     * 获取数据库的对象
     *
     * @return 返回SQLiteDatabase数据库的对象
     */
    public SQLiteDatabase getDataBase() {
        return db;
    }
}
