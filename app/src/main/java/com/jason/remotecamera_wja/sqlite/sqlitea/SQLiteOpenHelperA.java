package com.jason.remotecamera_wja.sqlite.sqlitea;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteOpenHelperA extends SQLiteOpenHelper {

    private static  SQLiteOpenHelperA helper;

    //构造器,传入四个参数Context对象，数据库名字name，操作数据库的Cursor对象，版本号version。
    private SQLiteOpenHelperA(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //为了简化构造器的使用，我们自定义一个构造器
    private SQLiteOpenHelperA(Context context, String name) {
        this(context, name, null, 1);//传入Context和数据库的名称，调用上面那个构造器
    }
    //将自定义的数据库创建类单例。
    public static  synchronized  SQLiteOpenHelperA getInstance(Context context) {
        if(helper==null){
            helper = new SQLiteOpenHelperA(context, TableConfigA.TABLE_PICTUREA);
        }
        return  helper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table if not exists "+TableConfigA.TABLE_PICTUREA+"("
                +TableConfigA.Column.ID+" integer not null primary key autoincrement,"
                +TableConfigA.Column.URI+ " verchar(100),"
                +TableConfigA.Column.CREATETIME+ " verchar(100),"
                +TableConfigA.Column.PREF_PIC_SIZE+ " verchar(100) ,"
                +TableConfigA.Column.PREF_FLASH_MODE+ " verchar(100),"
                +TableConfigA.Column.PREF_FOCUS_MODE+ " verchar(100), "
                +TableConfigA.Column.PREF_WHITE_BALANCE+ " verchar(100),"
                +TableConfigA.Column.PREF_SCENE_MODE+ " verchar(100),"
                +TableConfigA.Column.PREF_EXPOS_COMP+ " verchar(100),"
                +TableConfigA.Column.PREF_JPEG_QUALITY+ " verchar(100))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //用于升级数据库，只需要在创建本类对象时传入一个比之前创建传入的version大的数即可。
    }

}
