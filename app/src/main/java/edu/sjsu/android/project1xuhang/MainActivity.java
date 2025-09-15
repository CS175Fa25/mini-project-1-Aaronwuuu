package edu.sjsu.android.project1xuhang;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText etPrincipal;
    private SeekBar sbRate;
    private TextView tvRateValue, tvResult;
    private RadioGroup rgYears;
    private CheckBox cbTaxes;
    private Button btnCalc, btnUninstall;

    private final ActivityResultLauncher<Intent> uninstallLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "App was uninstalled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Uninstall cancelled", Toast.LENGTH_SHORT).show();
                }
            });
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etPrincipal = findViewById(R.id.etPrincipal);
        sbRate = findViewById(R.id.sbRate);
        tvRateValue = findViewById(R.id.tvRateValue);
        rgYears = findViewById(R.id.rgYears);
        cbTaxes = findViewById(R.id.cbTaxes);
        btnCalc = findViewById(R.id.btnCalc);
        btnUninstall = findViewById(R.id.btnUninstall);
        tvResult      = findViewById(R.id.tvResult);

        sbRate.setMax(200);
        sbRate.setProgress(100);
        tvRateValue.setText("10.0%");

        sbRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rate = progress / 10.0;
                tvRateValue.setText(String.format("%.1f%%", rate));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        btnCalc.setOnClickListener(v -> calculateMortgage());
        btnUninstall.setOnClickListener(v -> uninstallApp());
    }
    private void calculateMortgage() {
        String principalStr = etPrincipal.getText().toString().trim();
        if (principalStr.isEmpty()) {
            etPrincipal.setError("Principal is required");
            Toast.makeText(this, "Please enter principal amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!principalStr.matches("^\\d+(\\.\\d{1,2})?$")) {
            etPrincipal.setError("Up to 2 decimals, e.g. 12345.67");
            Toast.makeText(this, "Invalid principal format", Toast.LENGTH_SHORT).show();
            return;
        }

        double principal = 0;
        try {
            principal = Double.parseDouble(principalStr);
            if (principal <= 0) {
                etPrincipal.setError("Must be greater than 0");
                Toast.makeText(this, "Principal must be > 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            etPrincipal.setError("Invalid number");
            Toast.makeText(this, "Invalid principal number", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgYears.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select loan term (years)", Toast.LENGTH_SHORT).show();
            // 让 RadioGroup 获取焦点并滚动到可见（可选）
            rgYears.requestFocus();
            return;
        }

        RadioButton rb = findViewById(checkedId);
        int years = Integer.parseInt(rb.getText().toString().trim());
        int months = years * 12;

        double annualRate = sbRate.getProgress() / 10.0;     // e.g. 10.0
        if (annualRate < 0 || annualRate > 20) {             // 你设的范围 0~20
            Toast.makeText(this, "Rate out of range", Toast.LENGTH_SHORT).show();
            return;
        }
        double monthlyRate = (annualRate / 100.0) / 12.0;

        if (cbTaxes.isChecked()) principal *= 1.001;

        double monthlyPayment;
        if (monthlyRate > 0) {
            double pow = Math.pow(1 + monthlyRate, months);
            monthlyPayment = principal * (monthlyRate * pow) / (pow - 1);
        } else {
            monthlyPayment = principal / months;
        }

        String result = String.format("Monthly Payment: $%.2f", monthlyPayment);
        tvResult.setText(result);


    }
    private void uninstallApp() {
        Uri pkg = Uri.parse("package:" + getPackageName());
        Intent del = new Intent(Intent.ACTION_DELETE, pkg);
        del.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        try {
            uninstallLauncher.launch(del);
        } catch (Exception e) {
            Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkg);
            startActivity(fallback);
        }
    }
}
