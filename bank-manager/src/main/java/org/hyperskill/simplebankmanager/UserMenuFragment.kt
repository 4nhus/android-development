package org.hyperskill.simplebankmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

class UserMenuFragment : Fragment() {
    private lateinit var welcomeTextView: TextView
    private lateinit var viewBalanceButton: Button
    private lateinit var transferFundsButton: Button
    private lateinit var calculateExchangeButton: Button
    private lateinit var payBillsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_menu, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        welcomeTextView = view.findViewById<TextView?>(R.id.userMenuWelcomeTextView).apply {
            text = "Welcome ${arguments!!.getString("username")}"
        }

        viewBalanceButton = view.findViewById<Button?>(R.id.userMenuViewBalanceButton).apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_userMenuFragment_to_viewBalanceFragment,
                    bundleOf(
                        "balance" to arguments!!.getDouble("balance")
                    )
                )
            }
        }

        transferFundsButton = view.findViewById<Button?>(R.id.userMenuTransferFundsButton).apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_userMenuFragment_to_transferFundsFragment,
                    bundleOf(
                        "username" to arguments!!.getString("username"),
                        "password" to arguments!!.getString("password"),
                        "balance" to arguments!!.getDouble("balance"),
                        "exchangeMap" to arguments!!.getSerializable("exchangeMap"),
                        "billInfo" to arguments!!.getSerializable("billInfo")
                    )
                )
            }
        }

        calculateExchangeButton =
            view.findViewById<Button?>(R.id.userMenuExchangeCalculatorButton).apply {
                setOnClickListener {
                    findNavController().navigate(
                        R.id.action_userMenuFragment_to_calculateExchangeFragment,
                        bundleOf(
                            "balance" to arguments!!.getDouble("balance"),
                            "exchangeMap" to arguments!!.getSerializable("exchangeMap")
                        )
                    )
                }
            }

        payBillsButton = view.findViewById<Button?>(R.id.userMenuPayBillsButton).apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_userMenuFragment_to_payBillsFragment,
                    bundleOf(
                        "username" to arguments!!.getString("username"),
                        "password" to arguments!!.getString("password"),
                        "balance" to arguments!!.getDouble("balance"),
                        "exchangeMap" to arguments!!.getSerializable("exchangeMap"),
                        "billInfo" to arguments!!.getSerializable("billInfo")
                    )
                )
            }
        }
    }
}