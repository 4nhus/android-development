package org.hyperskill.simplebankmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class CalculateExchangeFragment : Fragment() {
    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var displayTextView: TextView
    private lateinit var amountEditText: EditText
    private lateinit var button: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calculate_exchange, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromSpinner = view.findViewById<Spinner?>(R.id.calculateExchangeFromSpinner).apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position) as String

                    if (selectedItem == toSpinner.selectedItem) {
                        Toast.makeText(
                            context,
                            "Cannot convert to same currency",
                            Toast.LENGTH_SHORT
                        ).show()
                        toSpinner.setSelection((toSpinner.selectedItemPosition + 1) % 3)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        toSpinner = view.findViewById<Spinner?>(R.id.calculateExchangeToSpinner).apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position) as String

                    if (selectedItem == fromSpinner.selectedItem) {
                        Toast.makeText(
                            context,
                            "Cannot convert to same currency",
                            Toast.LENGTH_SHORT
                        ).show()
                        setSelection((position + 1) % 3)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        ArrayAdapter.createFromResource(
            context!!,
            R.array.currencies_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            fromSpinner.adapter = adapter
            toSpinner.adapter = adapter
        }

        displayTextView = view.findViewById(R.id.calculateExchangeDisplayTextView)
        amountEditText = view.findViewById(R.id.calculateExchangeAmountEditText)
        button = view.findViewById<Button?>(R.id.calculateExchangeButton).apply {
            setOnClickListener {
                if (amountEditText.text.isBlank()) {
                    Toast.makeText(context, "Enter amount", Toast.LENGTH_SHORT).show()
                } else {
                    val fromCurrency = fromSpinner.selectedItem as String
                    val toCurrency = toSpinner.selectedItem as String
                    val amount = amountEditText.text.toString().toDouble()
                    val exchangeMap =
                        arguments!!.getSerializable("exchangeMap") as Map<String, Map<String, Double>>
                    val exchangedAmount =
                        String.format("%.2f", amount * exchangeMap[fromCurrency]!![toCurrency]!!)
                    val fromCurrencySymbol = when (fromCurrency) {
                        "USD" -> "$"
                        "EUR" -> "€"
                        else -> "£" // GBP
                    }
                    val toCurrencySymbol = when (toCurrency) {
                        "USD" -> "$"
                        "EUR" -> "€"
                        else -> "£" // GBP
                    }
                    displayTextView.text =
                        "$fromCurrencySymbol${
                            String.format(
                                "%.2f",
                                amount
                            )
                        } = $toCurrencySymbol$exchangedAmount"
                }
            }
        }
    }
}