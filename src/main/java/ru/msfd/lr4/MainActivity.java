package ru.msfd.lr4;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Constants.Constants;
import DB.ComputersDBHelper;
import Models.ComputerModel;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_MANAGE_STORAGE = 1001;
    public static final String COMPUTER_EXTRAS = "computer_extras";
    ComputersDBHelper dbHelper;
    Button addComputerButton;
    Button removeComputerButton;
    ListView computersList;

    ArrayList<ComputerModel> models;
    long selectedModelId;

    SQLiteDatabase db;

    SimpleCursorAdapter computersAdapter;

    Map<Integer, Runnable> itemsFunctions;

    Cursor currentCursor;
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted ->
            {
                if (!isGranted)  Toast.makeText(this, "Разрешение на чтение/запись файлов отклонено!", Toast.LENGTH_SHORT).show();
            }
    );
    ActivityResultLauncher<Intent> addComputerActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if(o.getResultCode() == MainActivity.RESULT_OK)
                    {
                        Intent intent = o.getData();
                        if(intent != null)
                        {
                            ComputerModel model = intent.getParcelableExtra(COMPUTER_EXTRAS);
                            if(model != null)
                            {
                                if(db.isOpen())
                                {
                                    ContentValues cv = new ContentValues();
                                    cv.put(Constants.MOTHERBOARD_MAN_COL, model.motherboardManufacturer.toString());
                                    cv.put(Constants.VIDEOCARD_MAN_COL, model.videocardManufacturer.toString());
                                    cv.put(Constants.RAM_COL, String.valueOf(model.getRam()));
                                    cv.put(Constants.MAN_YEAR_COL, model.manufactureDate.toString());
                                    cv.put(Constants.MONITOR_MAN_COL, model.monitordManufacturer.toString());
                                    model.setId((int)db.insert(Constants.COMPUTERS_TABLE, null, cv));
                                    Toast.makeText(MainActivity.this, "Добавлен автобус " + model, Toast.LENGTH_SHORT).show();
                                    computersAdapter.swapCursor(FetchAllData(db));
                                }
                            }
                        }
                    }
                    else if (o.getResultCode() == RESULT_CANCELED)
                    {
                        Toast.makeText(MainActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Setup();
    }

    private void CheckAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            else
            {
                Toast.makeText(this, "Разрешения уже предоставлены!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Нет необходимости в запросе разрешений", Toast.LENGTH_SHORT).show();
        }
    }


    private Cursor UpdateCursor(Cursor cursor)
    {
        if (currentCursor != null && !currentCursor.isClosed()) currentCursor.close();
        currentCursor = cursor;
        return currentCursor;
    }

    private void UpdateAdapter(int resId, Cursor cursor, String[] columns, int[] ids)
    {
        computersAdapter = new SimpleCursorAdapter(MainActivity.this, resId, UpdateCursor(cursor), columns, ids,0);
        computersList.setAdapter(computersAdapter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dbHelper.close();
    }

    private void WriteStringInFile (String data)
    {
        BufferedWriter bw = null;
        try
        {
            bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(Constants.FILENAME, MODE_PRIVATE)));
            Log.d(Constants.CUSTOM_TAG, "Начало записи\n");
            bw.write(data + "\n");
            Log.d(Constants.CUSTOM_TAG, "Файл записан");
        }
        catch(IOException e) { Log.d(Constants.ERRORS_TAG, e.toString()); }
        finally
        {
            try
            {
                if(bw!=null) bw.close();
            }
            catch(IOException e){ Log.d(Constants.ERRORS_TAG, e.toString()); }
        }
    }

    private void SetupMenu()
    {
        itemsFunctions = new HashMap<>();

        itemsFunctions.put(R.id.menu_show_all, () -> {
            Cursor cursor = FetchAllData(db);
            ArrayList<ComputerModel> computers = ComputersDBHelper.fetchAllData(cursor, false);
            StringBuilder sb = new StringBuilder();
            final String label = "=====Все данные без модификаций выборки=====";
            sb.append(label);
            for(ComputerModel computer : computers) sb.append(computer.toString()).append("\n");

            Log.d(Constants.CUSTOM_TAG, label + "\n" + sb);
            Toast.makeText(this, label, Toast.LENGTH_SHORT).show();

            UpdateAdapter(R.layout.computers_list_item, cursor,
                    new String[]{Constants.ID_COL, Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, Constants.RAM_COL, Constants.MAN_YEAR_COL, Constants.MONITOR_MAN_COL},
                    new int[]{R.id.computer_id_tv, R.id.motherboard_man_tv, R.id.videocard_man_tv, R.id.ram_tv, R.id.manufacture_date_tv, R.id.monitor_man_tv});
        });

        itemsFunctions.put(R.id.menu_sort_computers, () ->
        {
            String query = "select * from " + Constants.COMPUTERS_TABLE +
                    " order by " + Constants.RAM_COL + " asc";
            String label = "=====Сортировка ОП=====";
            ArrayList<ComputerModel> data = ComputersDBHelper.fetchAllData(db.rawQuery(query, null), true);
            Log.d(Constants.CUSTOM_TAG, label);
            for (ComputerModel info : data) Log.d(Constants.CUSTOM_TAG, info.toString());
            String result = label + "\n";
            StringBuilder sb = new StringBuilder();
            for (ComputerModel info : data) sb.append(info.toString()).append("\n");
            WriteStringInFile(result + sb);
            Toast.makeText(MainActivity.this, label, Toast.LENGTH_LONG).show();

        });

        itemsFunctions.put(R.id.menu_group_computers, () -> {
            final String totalRamCol = "total_ram";
            String query = "select " + Constants.MOTHERBOARD_MAN_COL + ", " + Constants.VIDEOCARD_MAN_COL +
                    ", sum(" + Constants.RAM_COL + ") as " + totalRamCol +
                    " from " + Constants.COMPUTERS_TABLE + " group by " + Constants.MOTHERBOARD_MAN_COL + ", " + Constants.VIDEOCARD_MAN_COL;
            String message = "=====Группировка по производителям мат. плат и видеокарт=====";
            Log.d(Constants.CUSTOM_TAG, message);
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null)
            {
                StringBuilder sb = new StringBuilder();
                MatrixCursor matrixCursor = new MatrixCursor(new String[] {"_id", Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, totalRamCol});
                int id = 1;
                int motherboardManColIndex = cursor.getColumnIndex(Constants.MOTHERBOARD_MAN_COL);
                int videocardManColIndex = cursor.getColumnIndex(Constants.VIDEOCARD_MAN_COL);
                int totalRAMColIndex = cursor.getColumnIndex(totalRamCol);
                if(motherboardManColIndex > -1 && videocardManColIndex > -1 && totalRAMColIndex > -1)
                {
                    while (cursor.moveToNext())
                    {
                        String motherboardMan = cursor.getString(motherboardManColIndex);
                        String videocardMan = cursor.getString(videocardManColIndex);
                        int totalRam = cursor.getInt(totalRAMColIndex);
                        matrixCursor.addRow(new Object[] { id++, motherboardMan, videocardMan, totalRam });
                        sb.append(getString(R.string.manufacturer_man_group_name) + " ").append(cursor.getString(motherboardManColIndex) + " ").append(getString(R.string.videocard_man_group_name) + " ").append(cursor.getString(videocardManColIndex) + " ").append(getString(R.string.total_ram) + " ").append(cursor.getInt(totalRAMColIndex)).append("\n");

                    }
                }
                cursor.close();
                Log.d(Constants.CUSTOM_TAG, message + "\n" + sb);
                UpdateAdapter(R.layout.motherboard_videocard_man_group_layout, matrixCursor,
                        new String[] { Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, totalRamCol },
                        new int[] {R.id.motherboard_man_tv, R.id.videocard_man_tv, R.id.total_group_ram_tv} );
            }

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        });

        itemsFunctions.put(R.id.menu_total_ram_capacity, () -> {
            String totalRAMCol = "total_ram";
            String query = "select sum("+ Constants.RAM_COL +") as " + totalRAMCol + " from " + Constants.COMPUTERS_TABLE;
            Cursor cursor = db.rawQuery(query, null);
            int totalRAM = 0;
            int totalRAMColIndex = cursor.getColumnIndex(totalRAMCol);
            if (cursor.moveToFirst() && totalRAMColIndex != -1) totalRAM = cursor.getInt(totalRAMColIndex);
            cursor.close();
            Log.d(Constants.CUSTOM_TAG, "=====Всего ОП=====");
            String result = "Всего ОП в каждом устройстве: " + totalRAM;
            WriteStringInFile(result);
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            Log.d(Constants.CUSTOM_TAG, result);
        });

        itemsFunctions.put(R.id.menu_avg_ram_capacity, () -> {
            final String avgGroupRam = "avg_ram";
            String query = "select " + Constants.MOTHERBOARD_MAN_COL +
                    ", avg(" + Constants.RAM_COL + ") as " + avgGroupRam +
                    " from " + Constants.COMPUTERS_TABLE + " group by " + Constants.MOTHERBOARD_MAN_COL;
            String label = "=====Средний размер ОП в группе=====";
            Log.d(Constants.CUSTOM_TAG, label);
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null)
            {
                StringBuilder sb = new StringBuilder();
                MatrixCursor matrixCursor = new MatrixCursor(new String[] {"_id", Constants.MOTHERBOARD_MAN_COL, avgGroupRam });
                int id = 1;
                int motherboardManColIndex = cursor.getColumnIndex(Constants.MOTHERBOARD_MAN_COL);
                int avgGroupRAMColIndex = cursor.getColumnIndex(avgGroupRam);
                if(motherboardManColIndex > -1 && avgGroupRAMColIndex > -1)
                {
                    while (cursor.moveToNext())
                    {
                        String motherboardMan = cursor.getString(motherboardManColIndex);
                        float avgGroupRAM = cursor.getFloat(avgGroupRAMColIndex);
                        matrixCursor.addRow(new Object[] { id++, motherboardMan, avgGroupRAM });
                        sb.append(getString(R.string.motherboard_man_spinner_label) + " ").append(cursor.getString(motherboardManColIndex) + " ").append(getString(R.string.avg_ram) + " ").append(cursor.getFloat(avgGroupRAMColIndex)).append("\n");
                    }
                }
                cursor.close();
                String result = label + "\n" + sb;
                Log.d(Constants.CUSTOM_TAG, result);
                WriteStringInFile(result);
                UpdateAdapter(R.layout.avg_group_ram_layout, matrixCursor,
                        new String[] { Constants.MOTHERBOARD_MAN_COL, avgGroupRam },
                        new int[] {R.id.motherboard_man_tv, R.id.avg_group_ram_tv });
            }
        });

        itemsFunctions.put(R.id.menu_max_ram, () -> {

            String query = "select * from " + Constants.COMPUTERS_TABLE + " order by " + Constants.RAM_COL + " desc limit 1";

            Log.d(Constants.CUSTOM_TAG, "=====Компьютер с максимальным значением ОП=====");
            ArrayList<ComputerModel> data = ComputersDBHelper.fetchAllData(db.rawQuery(query, null), true);
            if(!data.isEmpty())
            {
                String busInfo = data.get(0).toString();
                Toast.makeText(MainActivity.this, busInfo, Toast.LENGTH_LONG).show();
                Log.d(Constants.CUSTOM_TAG, busInfo);
            }
        });

        itemsFunctions.put(R.id.menu_ram_greater_than, () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Введите минимальную ОП: ");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) ->
            {
                int minValue = Integer.parseInt(input.getText().toString());
                String query = "select * from " + Constants.COMPUTERS_TABLE +
                        " where " + Constants.RAM_COL + " > " + minValue;
                Cursor cursor = db.rawQuery(query, null);
                ArrayList<ComputerModel> data = ComputersDBHelper.fetchAllData(cursor, false);
                Log.d(Constants.CUSTOM_TAG, "=====ОП больше чем " + minValue + "=====");
                for (ComputerModel computerModel : data) Log.d(Constants.CUSTOM_TAG, computerModel.toString());
                UpdateAdapter(R.layout.computers_list_item, cursor,
                        new String[]{Constants.ID_COL, Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, Constants.RAM_COL, Constants.MAN_YEAR_COL, Constants.MONITOR_MAN_COL},
                        new int[]{R.id.computer_id_tv, R.id.motherboard_man_tv, R.id.videocard_man_tv, R.id.ram_tv, R.id.manufacture_date_tv, R.id.monitor_man_tv});
            });
            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        itemsFunctions.put(R.id.menu_ram_less_than_avg, () ->
        {
            String query = "select * from " + Constants.COMPUTERS_TABLE +
                    " where " + Constants.RAM_COL + " < " + "(select avg(" + Constants.RAM_COL + ") from " + Constants.COMPUTERS_TABLE + ")";
            Cursor cursor = db.rawQuery(query, null);
            ArrayList<ComputerModel> data = ComputersDBHelper.fetchAllData(cursor, false);
            Log.d(Constants.CUSTOM_TAG, "=====ОП больше, чем средняя ОП всех компьютеров=====");
            for (ComputerModel info : data) Log.d(Constants.CUSTOM_TAG, info.toString());
            UpdateAdapter(R.layout.computers_list_item, cursor,
                    new String[] {Constants.ID_COL, Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, Constants.RAM_COL, Constants.MAN_YEAR_COL, Constants.MONITOR_MAN_COL},
                    new int[] {R.id.computer_id_tv, R.id.motherboard_man_tv, R.id.videocard_man_tv, R.id.ram_tv, R.id.manufacture_date_tv, R.id.monitor_man_tv});
        });

        itemsFunctions.put(R.id.menu_ram_greater_than_limit, () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Введите минимальное число ОП: ");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                int minValue = Integer.parseInt(input.getText().toString());
                String query = "select * from " + Constants.COMPUTERS_TABLE +
                        " where " + Constants.RAM_COL + " > " + minValue + " limit 1";
                ArrayList<ComputerModel> data = ComputersDBHelper.fetchAllData(db.rawQuery(query, null), true);
                String header = "=====Компьютер, ОП которого больше заданного====";
                Log.d(Constants.CUSTOM_TAG, header);
                Log.d(Constants.CUSTOM_TAG, data.get(0).toString());
                Toast.makeText(MainActivity.this, header + "\n" + data.get(0).toString(), Toast.LENGTH_LONG).show();
            });
            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private void Setup()
    {
        CheckAndRequestPermissions();
        models = new ArrayList<>();
        SetupMenu();
        SetupDB();
        SetupViewReferences();
        SetupButtons();
        SetupList();
    }

    private void SetupViewReferences()
    {
        addComputerButton = findViewById(R.id.add_computer_btn);
        removeComputerButton = findViewById(R.id.remove_computer_btn);
        computersList = findViewById(R.id.computers_list);
    }

    private void SetupList()
    {
        UpdateCursor(FetchAllData(db));
        computersAdapter = new SimpleCursorAdapter(this, R.layout.computers_list_item, currentCursor,
                new String[] { Constants.ID_COL, Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, Constants.RAM_COL, Constants.MAN_YEAR_COL, Constants.MONITOR_MAN_COL},
                new int[] { R.id.computer_id_tv, R.id.motherboard_man_tv, R.id.videocard_man_tv, R.id.ram_tv, R.id.manufacture_date_tv, R.id.monitor_man_tv}, 0);

        computersList.setAdapter(computersAdapter);

        computersList.setOnItemClickListener((adapterView, view, i, l) ->
        {
            Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
            int idColIndex = cursor.getColumnIndex(Constants.ID_COL);
            long itemId = cursor.getLong(idColIndex);
            selectedModelId = itemId;
        });
    }

    private void SetupButtons()
    {
        addComputerButton.setOnClickListener(view -> addComputerActivityLauncher.launch(new Intent("tkachenko.intent.action.addcomputer")));
        removeComputerButton.setOnClickListener(view -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() ->
            {
                if(selectedModelId > 0) db.delete(Constants.COMPUTERS_TABLE, Constants.ID_COL + " = ?", new String[] { String.valueOf(selectedModelId) });
                runOnUiThread(() -> {
                    computersAdapter.swapCursor(FetchAllData(db));
                    Toast.makeText(MainActivity.this, "Данные компьютера удален", Toast.LENGTH_LONG).show();
                });
                selectedModelId = -1;
            });
            executorService.shutdown();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.computers_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Runnable fun = itemsFunctions.getOrDefault(item.getItemId(), null);
        if(fun != null) fun.run();
        return super.onOptionsItemSelected(item);
    }

    private Cursor FetchAllData(SQLiteDatabase db)
    {
        if(db == null) return null;
        return db.query(Constants.COMPUTERS_TABLE, new String[] { Constants.ID_COL, Constants.MOTHERBOARD_MAN_COL, Constants.VIDEOCARD_MAN_COL, Constants.RAM_COL, Constants.MAN_YEAR_COL, Constants.MONITOR_MAN_COL}, "", null, "", "", "");
    }

    private void SetupDB()
    {
        dbHelper = new ComputersDBHelper(this);
        db = dbHelper.getWritableDatabase();
        UpdateCursor(FetchAllData(db));
    }
}