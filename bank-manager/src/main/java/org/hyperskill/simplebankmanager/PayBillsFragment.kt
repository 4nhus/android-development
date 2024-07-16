package org.hyperskill.simplebankmanager

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import kotlin.properties.Delegates

class PayBillsFragment : Fragment() {
    private lateinit var codeInputEditText: EditText
    private lateinit var showBillInfoButton: Button
    private var balance by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity!!.onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(
                R.id.action_payBillsFragment_to_userMenuFragment,
                bundleOf(
                    "username" to arguments!!.getString("username"),
                    "password" to arguments!!.getString("password"),
                    "balance" to balance,
                    "exchangeMap" to arguments!!.getSerializable("exchangeMap"),
                    "billInfo" to arguments!!.getSerializable("billInfo")
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pay_bills, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        balance = arguments!!.getDouble("balance")

        codeInputEditText = view.findViewById(R.id.payBillsCodeInputEditText)
        showBillInfoButton = view.findViewById<Button?>(R.id.payBillsShowBillInfoButton).apply {
            setOnClickListener {
                val billingCode = codeInputEditText.text.toString()
                val billInfo =
                    arguments!!.getSerializable("billInfo") as Map<String, Triple<String, String, Double>>

                if (billingCode !in billInfo) {
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Wrong code")
                        .setPositiveButton("Ok") { _, _ -> }
                        .create()
                        .show()
                } else {
                    val billName = billInfo[billingCode]!!.first
                    val billCode = billInfo[billingCode]!!.second
                    val billAmount = billInfo[billingCode]!!.third

                    AlertDialog.Builder(context)
                        .setTitle("Bill info")
                        .setMessage(
                            """
                            Name: $billName
                            BillCode: $billCode
                            Amount: $${String.format("%.2f", billAmount)}
                        """.trimIndent()
                        )
                        .setPositiveButton("Ok") { _, _ ->
                            if (balance >= billAmount) {
                                Toast.makeText(
                                    context,
                                    "Payment for bill $billName, was successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                balance -= billAmount
                            } else {
                                AlertDialog.Builder(context)
                                    .setTitle("Error")
                                    .setMessage("Not enough funds")
                                    .setPositiveButton("Ok") { _, _ -> }
                                    .create()
                                    .show()
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ -> }
                        .create()
                        .show()
                }
            }
        }
    }
}