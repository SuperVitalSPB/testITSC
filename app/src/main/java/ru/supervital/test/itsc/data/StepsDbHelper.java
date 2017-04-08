package ru.supervital.test.itsc.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import static ru.supervital.test.itsc.data.StepsHistoryContract.StepsEntry.TABLE_NAME;

/**
 * Created by Vitaly Oantsa on 07.04.2017.
 */

public class StepsDbHelper extends SQLiteOpenHelper {
    public static final String TAG = StepsDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "steps.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Конструктор {@link StepsDbHelper}.
     *
     * @param context Контекст приложения
     */
    public StepsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + StepsHistoryContract.StepsEntry.COLUMN_DATE_STEP + " TEXT PRIMARY KEY , "
                + StepsHistoryContract.StepsEntry.COLUMN_COUNT + " INTEGER NOT NULL);";
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getTrustDate(Date aDate){
        Date wDate = (aDate==null? new Date() : aDate);
        String res = android.text.format.DateFormat.format(
                                 "yyyy-MM-dd", wDate).toString();
        return res;
    }

    public Integer getCountStepsInDay(Date aDate){
        Integer res = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String fDate = getTrustDate(aDate);

        try {
            String[] projection = {StepsHistoryContract.StepsEntry.COLUMN_COUNT};

            String selection = StepsHistoryContract.StepsEntry.COLUMN_DATE_STEP + "= ? ";
            String[] selectionArgs = {fDate};

            cursor = db.query(
                    TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            int columnCountIndex = cursor.getColumnIndex(StepsHistoryContract.StepsEntry.COLUMN_COUNT);

            while (cursor.moveToNext())
                res = cursor.getInt(columnCountIndex);
        }
        finally {
            if (cursor!=null)
                    cursor.close();
        }

        return res;
    }

    public void setCountStepsInDay(Date aDate, Integer aCount){
        SQLiteDatabase db = getWritableDatabase();

        Boolean newDay = getCountStepsInDay(aDate) == 0;
        ContentValues values = new ContentValues();
        values.put(StepsHistoryContract.StepsEntry.COLUMN_COUNT, aCount);

        if (newDay){
            values.put(StepsHistoryContract.StepsEntry.COLUMN_DATE_STEP, getTrustDate(aDate));
            long newRowId = db.insert(StepsHistoryContract.StepsEntry.TABLE_NAME, null, values);
        }
        else {
            db.update(TABLE_NAME,
                    values,
                    StepsHistoryContract.StepsEntry.COLUMN_DATE_STEP + "= ? ",
                    new String[] {getTrustDate(aDate)});
        }
    };

}
