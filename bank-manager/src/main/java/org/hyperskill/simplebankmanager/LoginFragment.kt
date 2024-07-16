package org.hyperskill.simplebankmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {
    private lateinit var loginUsernameEditText: EditText
    private lateinit var loginPasswordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginUsernameEditText = view.findViewById(R.id.loginUsername)
        loginPasswordEditText = view.findViewById(R.id.loginPassword)
        loginButton = view.findViewById<Button?>(R.id.loginButton).apply {
            setOnClickListener {
                val intent = (context as AppCompatActivity).intent

                val username = intent.extras?.getString("username") ?: "Lara"
                val password = intent.extras?.getString("password") ?: "1234"
                val balance = intent.extras?.getDouble("balance") ?: 100.0
                val exchangeMap =
                    intent.extras?.getSerializable("exchangeMap") as? Map<String, Map<String, Double>>
                        ?: mapOf(
                            "EUR" to mapOf(
                                "GBP" to 0.5,
                                "USD" to 2.0
                            ),
                            "GBP" to mapOf(
                                "EUR" to 2.0,
                                "USD" to 4.0
                            ),
                            "USD" to mapOf(
                                "EUR" to 0.5,
                                "GBP" to 0.25
                            )
                        )
                val billsInfoMap =
                    intent.extras?.getSerializable("billInfo") as? Map<String, Triple<String, String, Double>>
                        ?: mapOf(
                            "ELEC" to Triple("Electricity", "ELEC", 45.0),
                            "GAS" to Triple("Gas", "GAS", 20.0),
                            "WTR" to Triple("Water", "WTR", 25.5)
                        )

                if (loginUsernameEditText.text.toString() == username && loginPasswordEditText.text.toString() == password) {
                    Toast.makeText(context, "logged in", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                        R.id.action_loginFragment_to_userMenuFragment,
                        bundleOf(
                            "username" to username,
                            "password" to password,
                            "balance" to balance,
                            "exchangeMap" to exchangeMap,
                            "billInfo" to billsInfoMap
                        )
                    )
                } else {
                    Toast.makeText(context, "invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}