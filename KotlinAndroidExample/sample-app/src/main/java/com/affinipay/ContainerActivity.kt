package com.affinipay

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.affinipay.cardreadersdk.R
import com.affinipay.cardreadersdk.ViewProvider
import com.affinipay.cardreadersdk.amount.AmountRequester
import com.affinipay.cardreadersdk.amount.AmountResult
import com.affinipay.cardreadersdk.charge.*
import com.affinipay.cardreadersdk.customerinfo.CustomerInfoParams
import com.affinipay.cardreadersdk.customerinfo.CustomerInfoRequester
import com.affinipay.cardreadersdk.customerinfo.CustomerInfoResult
import com.affinipay.cardreadersdk.customerinfo.CustomerInfoType
import com.affinipay.cardreadersdk.signature.SignatureFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject



class ContainerActivity : AppCompatActivity(), Navigator, AmountRequester, ChargeRequester, PublicKeyRequester, CustomerInfoRequester {

  private val compositeDisposable = CompositeDisposable()
  private var publicKey: String? = null
  private var accountId: String? = null
  private var amount: String? = null
  lateinit private var fragment:Fragment

  @Inject
  lateinit var chargeModel: ChargeModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    App[this].component.inject(this)
    setContentView(R.layout.activity_container_layout)

  }


  override fun onStart() {
    super.onStart()
    if (fragmentManager.backStackEntryCount == 0) {
      goToScreen(LoginAccountView())
    }
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)

    if(!publicKey.isNullOrEmpty()) {
      outState!!.putString("public_key", publicKey)
    }

    if (!accountId.isNullOrEmpty()) {
      outState!!.putString("accountId", accountId)
    }

    if (!amount.isNullOrEmpty()) {
      outState!!.putString("amount", amount)
    }

  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)

    publicKey = savedInstanceState.getString("public_key")
    amount = savedInstanceState.getString("amount")
    accountId = savedInstanceState.getString("accountId")

    fragment = fragmentManager.findFragmentByTag("currentFragment")
  }

  override fun onResume() {
    super.onResume()

  }

  override fun onPause() {
    compositeDisposable.dispose()
    super.onPause()
  }

  override fun onBackPressed() {
    if (fragmentManager.backStackEntryCount > 1) {
      fragmentManager.popBackStackImmediate()
    } else {
      finish()
    }
  }

  private fun goToCustomerInfoScreen() {
    val requiredFieldTypes = arrayListOf(CustomerInfoType.EMAIL, CustomerInfoType.POSTAL_CODE, CustomerInfoType.ADDRESS1)
    val params = CustomerInfoParams(requiredFieldTypes)
    goToScreen(ViewProvider.getCustomerInfoView(params))
  }

  override fun goToAmountSelectionScreen() {
    goToScreen(ViewProvider.getAmountSelectionView())
  }

  private fun goToScreen(newView: Fragment) {
    val fragmentTransaction = fragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.fragment_container, newView, "currentFragment").addToBackStack(newView.tag)
    fragmentTransaction.commit()
  }

  override fun onAccountChosen(publicKey: String, accountId: String) {
    this.publicKey = publicKey
    this.accountId = accountId
  }

  override fun onAmountReceived(amountResult: AmountResult) {
    amount = amountResult.amount
    goToCustomerInfoScreen()
  }

  override fun onCustomerInfoReceived(customerInfoResult: CustomerInfoResult) {
    fragment = ViewProvider.getCardEntryView(ChargeParams(customerInfoResult.customerInfo, publicKey as String, amount as String))
    goToScreen(fragment)
  }

  override fun onManualChargeDataReceived(manualChargeResult: ChargeResult) {
    compositeDisposable.add(chargeModel.makeCharge(publicKey as String, accountId as String, manualChargeResult.oneTimeToken, manualChargeResult.amount).subscribeOn
    (Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe({
      charge ->
      (fragment as CardEntryFragment).completionCallback(true)
      handleChargeCompletedReceipt(charge)
    }, {
      error ->
      (fragment as CardEntryFragment).completionCallback(false)
      print(error.message)
    }))
  }

  override fun onSwipeChargeDataReceived(swipeChargeResult: ChargeResult) {
    compositeDisposable.add(chargeModel.makeCharge(publicKey as String, accountId as String, swipeChargeResult.oneTimeToken, swipeChargeResult.amount).subscribeOn
    (Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe({
      charge ->
      (fragment as CardEntryFragment).completionCallback(true)
      handleChargeCompletedReceipt(charge)
    }, {
      error ->
      (fragment as CardEntryFragment).completionCallback(false)
      print(error.message)
    }))
  }

  override fun onEMVChargeDataReceived(emvChargeResult: ChargeResult) {
    compositeDisposable.add(chargeModel.makeEMVCharge(publicKey as String, accountId as String, emvChargeResult.oneTimeToken, emvChargeResult.amount, emvChargeResult
        .paymentDataSource,
        emvChargeResult.pointOfSale as PointOfSale).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
      charge ->
      (fragment as CardEntryFragment).completionCallback(true)
      handleChargeCompletedReceipt(charge)
    }, {
      error ->
      (fragment as CardEntryFragment).completionCallback(false)
      print(error.message)
    }))
  }

  fun handleChargeCompletedReceipt(charge: Charge) {
    val chargeId = charge.chargeDetails?.id
    val signIntent = Intent(this, SignatureActivity::class.java)
    signIntent.putExtra(SignatureActivity.ARG_PUBLIC_KEY, publicKey)
    signIntent.putExtra(SignatureActivity.ARG_ACCOUNT_ID, accountId)
    signIntent.putExtra(SignatureActivity.ARG_CHARGE_ID, chargeId)
    signIntent.putExtra(SignatureFragment.ARG_AMOUNT, amount)
    startActivity(signIntent)
  }

}