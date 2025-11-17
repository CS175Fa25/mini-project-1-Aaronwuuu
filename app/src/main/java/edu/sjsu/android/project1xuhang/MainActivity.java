package edu.sjsu.android.project1xuhang;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText etPrincipal;
    private SeekBar sbRate;
    private TextView tvRateValue, tvResult;
    private RadioGroup rgYears;
    private CheckBox cbTaxes;
    private Button btnCalc, btnUninstall;

    @Override
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
        tvResult = findViewById(R.id.tvResult);

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        String zeroPayment = currencyFormat.format(0.0);
        String initialText = getString(R.string.text_monthly_payment, zeroPayment);
        tvResult.setText(initialText);

        sbRate.setMax(200);
        sbRate.setProgress(100);
        tvRateValue.setText(getString(R.string.default_rate));

        sbRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rate = progress / 10.0;
                tvRateValue.setText(String.format(Locale.getDefault(), "%.1f%%", rate));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnCalc.setOnClickListener(v -> calculateMortgage());
        btnUninstall.setOnClickListener(v -> uninstallApp());
    }

    private void calculateMortgage() {
        String principalStr = etPrincipal.getText().toString().trim();
        if (principalStr.isEmpty()) {
            etPrincipal.setError(getString(R.string.error_principal_required));
            Toast.makeText(this,
                    getString(R.string.toast_enter_principal_amount),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!principalStr.matches("^\\d+(\\.\\d{1,2})?$")) {
            etPrincipal.setError(getString(R.string.error_principal_format));
            Toast.makeText(this,
                    getString(R.string.toast_invalid_principal_format),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double principal;
        try {
            principal = Double.parseDouble(principalStr);
            if (principal <= 0) {
                etPrincipal.setError(getString(R.string.error_principal_positive));
                Toast.makeText(this,
                        getString(R.string.toast_principal_positive),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            etPrincipal.setError(getString(R.string.error_invalid_number));
            Toast.makeText(this,
                    getString(R.string.toast_invalid_number),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgYears.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this,
                    getString(R.string.toast_select_loan_term),
                    Toast.LENGTH_SHORT).show();
            rgYears.requestFocus();
            return;
        }

        RadioButton rb = findViewById(checkedId);
        int years = Integer.parseInt(rb.getText().toString().trim());
        int months = years * 12;

        double annualRate = sbRate.getProgress() / 10.0;
        if (annualRate < 0 || annualRate > 20) {
            Toast.makeText(this,
                    getString(R.string.toast_rate_out_of_range),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        double monthlyRate = (annualRate / 100.0) / 12.0;

        if (cbTaxes.isChecked()) {
            principal *= 1.001;
        }

        double monthlyPayment;
        if (monthlyRate > 0) {
            double pow = Math.pow(1 + monthlyRate, months);
            monthlyPayment = principal * (monthlyRate * pow) / (pow - 1);
        } else {
            monthlyPayment = principal / months;
        }

        // ✅ 用 Locale & NumberFormat 格式化货币
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        String formattedPayment = currencyFormat.format(monthlyPayment);

        String resultText = getString(R.string.text_monthly_payment, formattedPayment);
        tvResult.setText(resultText);
    }

    // ✅ 卸载逻辑改为最简单版本：不再用 ActivityResultLauncher
    private void uninstallApp() {
        Uri pkg = Uri.parse("package:" + getPackageName());
        Intent del = new Intent(Intent.ACTION_DELETE, pkg);
        try {
            startActivity(del);
        } catch (Exception e) {
            Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkg);
            startActivity(fallback);
        }
    }

    // ✅ 菜单：语言设置入口
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_language) {
            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
