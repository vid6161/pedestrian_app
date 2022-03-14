package com.example.sensorapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "UserData.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "user";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "user_name";
    private static final String COLUMN_AGE = "user_age";
    private static final String COLUMN_GENDER = "user_gender";
    private static final String COLUMN_HEIGHT = "user_height";
    private static final String COLUMN_WEIGHT = "user_weight";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COLUMN_NAME+" TEXT, "+
                COLUMN_AGE+" INTEGER, "+
                COLUMN_GENDER+" TEXT, "+
                COLUMN_HEIGHT+ " INTEGER, "+
                COLUMN_WEIGHT+ " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    void addData (String name, int age, String gender, int height, int weight){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME,name);
        cv.put(COLUMN_AGE,age);
        cv.put(COLUMN_GENDER,gender);
        cv.put(COLUMN_HEIGHT,height);
        cv.put(COLUMN_WEIGHT,weight);

        long result= db.insert(TABLE_NAME,null,cv);
    }

    Cursor readAllData() {
        String query = "SELECT * FROM "+TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if(db!= null){
            cursor = db.rawQuery(query,null);
        }
        return cursor;
    }

    void UpdateData(String row_id,String name, int age, String gender, int height, int weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME,name);
        cv.put(COLUMN_AGE,age);
        cv.put(COLUMN_GENDER,gender);
        cv.put(COLUMN_HEIGHT,height);
        cv.put(COLUMN_WEIGHT,weight);

        db.update(TABLE_NAME,cv,"_id=?",new String[]{row_id});
    }

}
