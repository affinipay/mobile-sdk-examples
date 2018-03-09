package com.affinipay

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.affinipay.cardreadersdk.CurrencyFormatter
import kotlinx.android.synthetic.main.demo_receipt_layout.*

class ReceiptActivity : AppCompatActivity() {

  companion object {
    val ARG_AMOUNT: String = "ARG_AMOUNT"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.demo_receipt_layout)
  }

  override fun onResume() {
    super.onResume()

    val amount = intent.getStringExtra(ARG_AMOUNT)
    chargeAmount.text = CurrencyFormatter.toLocalCurrency(amount)

    doneButton.setOnClickListener {
      val newChargeIntent = Intent(this, ContainerActivity::class.java)
      startActivity(newChargeIntent)
    }
  }

}