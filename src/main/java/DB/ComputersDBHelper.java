package DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

import Models.ComputerModel;
import Constants.Constants;

public class ComputersDBHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 3;
    public ComputersDBHelper(Context context)
    {
        super(context, Constants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createDBQuery = "create table if not exists " + Constants.COMPUTERS_TABLE +
                " (" + Constants.ID_COL + " integer primary key autoincrement not null, " +
                       Constants.MOTHERBOARD_MAN_COL + " text not null, " +
                       Constants.VIDEOCARD_MAN_COL + " text not null, " +
                       Constants.RAM_COL + " integer not null check(" + Constants.RAM_COL + " > 0), " +
                       Constants.MAN_YEAR_COL + " text not null, " +
                       Constants.MONITOR_MAN_COL + " text not null" + " )";
        db.execSQL(createDBQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + Constants.COMPUTERS_TABLE);
        onCreate(db);
    }

    public static ArrayList<ComputerModel> fetchAllData(Cursor cursor, boolean closeCursor)
    {
        ArrayList<ComputerModel> data = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            int idColIndex = cursor.getColumnIndex(Constants.ID_COL);
            int motherboardManColIndex = cursor.getColumnIndex(Constants.MOTHERBOARD_MAN_COL);
            int videocardManColIndex = cursor.getColumnIndex(Constants.VIDEOCARD_MAN_COL);
            int ramColIndex = cursor.getColumnIndex(Constants.RAM_COL);
            int manufacturerDateColIndex = cursor.getColumnIndex(Constants.MAN_YEAR_COL);
            int monitorManColIndex = cursor.getColumnIndex(Constants.MONITOR_MAN_COL);
            do
            {
                if(idColIndex!=-1 && motherboardManColIndex!=-1 && videocardManColIndex!=-1 && ramColIndex!=-1&&manufacturerDateColIndex!=-1&&monitorManColIndex!=-1)
                {
                    data.add(new ComputerModel(
                            cursor.getInt(idColIndex),
                            cursor.getString(motherboardManColIndex),
                            cursor.getString(videocardManColIndex),
                            cursor.getInt(ramColIndex),
                            cursor.getString(manufacturerDateColIndex),
                            cursor.getString(monitorManColIndex),
                            false
                    ));
                }
            } while (cursor.moveToNext());
        }
        if(closeCursor) cursor.close();
        return data;
    }
}