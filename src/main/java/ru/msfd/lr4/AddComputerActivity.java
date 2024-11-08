package ru.msfd.lr4;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.stream.Stream;

import Constants.Constants;
import Models.ComputerModel;

public class AddComputerActivity extends AppCompatActivity {

    Button addComputerButton, cancelButton;
    Spinner motherboardManSpinner, videocardManSpinner, monitorManSpinner;
    EditText ramEditText;
    CalendarView manufactureDateCalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_computer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Setup();
    }

    private void Setup()
    {
        SetupViewReferences();
        SetupButtons();
        SetupSpinners();
        SetupTextView();
    }

    private void SetupViewReferences()
    {
        addComputerButton = findViewById(R.id.add_computer_btn);
        cancelButton = findViewById(R.id.cancel_button);
        motherboardManSpinner = findViewById(R.id.motherboard_man_spinner);
        videocardManSpinner = findViewById(R.id.videocard_man_spinner);
        monitorManSpinner = findViewById(R.id.monitor_man_spinner);
        manufactureDateCalendar = findViewById(R.id.manufacture_date_calendar);
        ramEditText = findViewById(R.id.ram_edittext);
    }

    private String[] MapEnumToArray(Object[] source)
    {
        return Stream.of(source).map(Object::toString).toArray(String[]::new);
    }

    private void SetupSpinners()
    {
        if(motherboardManSpinner != null)
        {
            motherboardManSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                    MapEnumToArray(ComputerModel.MotherboardManufacturer.values())));

        }

        if(videocardManSpinner != null)
        {
            videocardManSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                    MapEnumToArray(ComputerModel.VideocardManufacturer.values())));

        }

        if(monitorManSpinner != null)
        {
            monitorManSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                    MapEnumToArray(ComputerModel.MonitordManufacturer.values())));
        }
    }

    private void SetupTextView()
    {
        if(ramEditText != null)
        {
            ramEditText.setText("2");
            ramEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) { }

                private boolean isEditing = false;

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                    if (isEditing) return;

                    String input = charSequence.toString();
                    if (!input.isEmpty()) {

                        boolean nonInteger = input.contains(".");
                        boolean negative = true;
                        try {
                            negative = Integer.parseInt(input) <= 0;
                        } catch (Exception e) {
                            Log.d(Constants.CUSTOM_TAG, e.toString());
                        }

                        if (nonInteger || negative) {
                            isEditing = true;

                            ramEditText.setText("0");
                            ramEditText.setSelection(ramEditText.getText().toString().length());
                            if (nonInteger)  Toast.makeText(AddComputerActivity.this, "ОП должна быть целым числом Гб!", Toast.LENGTH_SHORT).show();
                            if (negative)  Toast.makeText(AddComputerActivity.this, "ОП должна быть больше '0' Гб!", Toast.LENGTH_SHORT).show();
                            isEditing = false;
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }

    private void SetupButtons()
    {
        if(addComputerButton != null)
        {
            addComputerButton.setOnClickListener(view -> {
                if(motherboardManSpinner == null ||
                videocardManSpinner == null ||
                        ramEditText == null ||
                manufactureDateCalendar == null ||
                monitorManSpinner == null)
                {
                    Log.d(Constants.CUSTOM_TAG, "Ошибка инициализации!");
                    return;
                }
                if(motherboardManSpinner.getSelectedItem() == null || videocardManSpinner.getSelectedItem() == null ||
                    manufactureDateCalendar.getDate() < 1000 || ramEditText.getText().toString().isEmpty())
                {
                    final String messsage = "Заполните все поля!";
                    Log.d(Constants.CUSTOM_TAG, messsage);
                    Toast.makeText(this, messsage, Toast.LENGTH_SHORT).show();

                }
                else if(Integer.parseInt(ramEditText.getText().toString()) <= 0)
                {
                    final String messsage1 = "ОП должна быть больше 0!";
                    Log.d(Constants.CUSTOM_TAG, messsage1);
                    Toast.makeText(this, messsage1, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.d(Constants.CUSTOM_TAG, "Заполняем поля");

                    Intent intent = getIntent();
                    intent.putExtra(MainActivity.COMPUTER_EXTRAS, new ComputerModel
                            (
                                    ComputerModel.INVALID_ID,
                                    motherboardManSpinner.getSelectedItem().toString(),
                                    videocardManSpinner.getSelectedItem().toString(),
                                    Integer.parseInt(ramEditText.getText().toString()),
                                    String.valueOf(manufactureDateCalendar.getDate()),
                                    monitorManSpinner.getSelectedItem().toString(),
                                    true
                            ));
                    Log.d(Constants.CUSTOM_TAG, "Устанавливаем результат");

                    setResult(RESULT_OK, intent);
                    Log.d(Constants.CUSTOM_TAG, "Завершаем работу активности");

                    finish();
                }
            });
        }
    }
}