package com.affinipay

import android.app.AlertDialog
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.affinipay.cardreadersdk.R
import com.affinipay.cardreadersdk.ViewProvider
import com.affinipay.cardreadersdk.signature.SignatureFragment
import com.affinipay.cardreadersdk.signature.SignatureParams
import com.affinipay.cardreadersdk.signature.SignatureRequester
import com.affinipay.cardreadersdk.signature.SignatureResult
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SignatureActivity : AppCompatActivity(), SignatureRequester, Navigator {

  companion object {
    val ARG_CHARGE_ID: String = "ARG_CHARGE_ID"
    val ARG_ACCOUNT_ID: String = "ARG_ACCOUNT_ID"
    val ARG_PUBLIC_KEY: String = "ARG_PUBLIC_KEY"
  }

  private val compositeDisposable = CompositeDisposable()

  @Inject
  lateinit var chargeModel: ChargeModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    App[this].component.inject(this)
    setContentView(R.layout.activity_container_layout)
  }

  override fun onStart() {
    super.onStart()

    goToScreen(ViewProvider.getSignatureView(SignatureParams("Display name for the merchant who is receiving this payment",
        intent.getStringExtra(SignatureFragment.ARG_AMOUNT))))
  }

  override fun onPause() {
    compositeDisposable.dispose()
    super.onPause()
  }

  private fun goToScreen(newView: Fragment) {
    val fragmentTransaction = fragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.fragment_container, newView).addToBackStack(newView.tag)
    fragmentTransaction.commit()
  }

  override fun onBackPressed() {
    //do nothing
  }

  override fun goToAmountSelectionScreen() {
    goToScreen(ViewProvider.getAmountSelectionView())
  }

  override fun onSignatureReceived(signatureResult: SignatureResult) {

    val chargeId = intent.extras.getString(ARG_CHARGE_ID)
    val accountId = intent.extras.getString(ARG_ACCOUNT_ID)
    val publicKey = intent.extras.getString(ARG_PUBLIC_KEY)

    compositeDisposable.add(chargeModel.addSignature(chargeId, accountId, signatureResult.signature, publicKey).subscribe({

      signatureReceipt ->
      val dialog = AlertDialog.Builder(this)
      dialog.setTitle("Charge Completed")
          .setMessage(signatureReceipt.toString())
          .setCancelable(false)
          .setPositiveButton("See Receipt", { _: DialogInterface, _: Int ->
            val receiptIntent = Intent(this, ReceiptActivity::class.java)
            receiptIntent.putExtra(ReceiptActivity.ARG_AMOUNT, intent.getStringExtra(SignatureFragment.ARG_AMOUNT))
            startActivity(receiptIntent)
          }).show()
    }, {
      error ->
      print(error.message)
    }))
  }
}