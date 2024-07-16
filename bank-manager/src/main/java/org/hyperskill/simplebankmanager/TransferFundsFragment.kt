package org.hyperskill.simplebankmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.navigation.fragment.findNavController

class TransferFundsFragment : Fragment() {
    private lateinit var accountEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfer_funds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountEditText = view.findViewById(R.id.transferFundsAccountEditText)
        amountEditText = view.findViewById(R.id.transferFundsAmountEditText)
        button = view.findViewById<Button?>(R.id.transferFundsButton).apply {
            setOnClickListener {
                val account = accountEditText.text.toString()
                val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0

                if (validateTransfer(account, amount)) {
                    var balance = arguments!!.getDouble("balance")
                    if (amount > balance) {
                        Toast.makeText(
                            context,
                            "Not enough funds to transfer $${String.format("%.2f", amount)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Transferred $${String.format("%.2f", amount)} to account $account",
                            Toast.LENGTH_SHORT
                        ).show()
                        balance -= amount
                        findNavController().navigate(
                            R.id.action_transferFundsFragment_to_userMenuFragment,
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
            }
        }

    }

    private fun validateTransfer(account: String, amount: Double): Boolean {
        var validTransfer = true;
        if (!validateAccount(account)) {
            accountEditText.error = "Invalid account number"
            validTransfer = false
        }
        if (amount <= 0.0) {
            amountEditText.error = "Invalid amount"
            validTransfer = false
        }
        return validTransfer
    }

    private fun validateAccount(account: String): Boolean {
        return account.length == 6 && (account.startsWith("ca") || account.startsWith("sa")) && account.substring(
            2
        ).isDigitsOnly()
    }
}