package com.company.smssender

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.smssender.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Fixed number of digits required for the phone number (Iranian mobile format)
    private val REQUIRED_LENGTH = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOk.setOnClickListener {
            handleOkClick()
        }
    }

    private fun handleOkClick() {
        val typedInput = binding.etPhone.text?.toString()?.trim() ?: ""
        val rawInput = normalizePhoneNumber(typedInput)

        // Reflect the normalized value back into the field so the user sees the corrected number
        if (rawInput != typedInput) {
            binding.etPhone.setText(rawInput)
            binding.etPhone.setSelection(rawInput.length)
        }

        // 1) Empty check
        if (rawInput.isEmpty()) {
            binding.tilPhone.error = getString(R.string.error_empty)
            return
        }

        // 2) Digits-only check
        if (!rawInput.all { it.isDigit() }) {
            binding.tilPhone.error = getString(R.string.error_digits_only)
            return
        }

        // 3) Exact length check (not less, not more than 11 digits)
        if (rawInput.length != REQUIRED_LENGTH) {
            binding.tilPhone.error = getString(R.string.error_length)
            return
        }

        // All good -> clear error and open SMS app with prefilled message
        binding.tilPhone.error = null
        openSmsAppWithMessage(rawInput)
    }

    /**
     * Cleans up a pasted/typed phone number:
     * - Converts Persian/Arabic-Indic digits to normal Latin digits
     * - Removes spaces, dashes and parentheses
     * - Replaces a leading +98 or 0098 with a single leading 0
     *   (numbers that already start with 0 are left untouched)
     */
    private fun normalizePhoneNumber(input: String): String {
        var result = StringBuilder()
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> result.append('0' + (ch - '۰'))
                in '٠'..'٩' -> result.append('0' + (ch - '٠'))
                else -> result.append(ch)
            }
        }
        var s = result.toString().replace(Regex("[\\s\\-()]"), "")

        s = when {
            s.startsWith("+98") -> "0" + s.removePrefix("+98")
            s.startsWith("0098") -> "0" + s.removePrefix("0098")
            else -> s
        }

        return s
    }

    private fun openSmsAppWithMessage(phoneNumber: String) {
        val message = getString(R.string.sms_body_template)

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }

        try {
            startActivity(smsIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "اپلیکیشن پیامک روی این گوشی پیدا نشد",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

