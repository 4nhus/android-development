package org.hyperskill.simplebankmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ViewBalanceFragment : Fragment() {
    private lateinit var balanceAmountTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_balance, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        balanceAmountTextView = view.findViewById<TextView?>(R.id.viewBalanceAmountTextView).apply {
            text = "$${String.format("%.2f", arguments!!.getDouble("balance"))}"
        }
    }
}