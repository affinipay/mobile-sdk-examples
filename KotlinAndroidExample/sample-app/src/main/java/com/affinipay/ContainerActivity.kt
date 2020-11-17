package com.affinipay

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.affinipay.cardreadersdk.ViewProvider
import com.affinipay.cardreadersdk.charge.*
import com.affinipay.cardreadersdk.customerinfo.CustomerInfoRequester
import com.affinipay.cardreadersdk.customerinfo.TokenizationCompleteParams
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_container_layout.*
import javax.inject.Inject

class ContainerActivity : AppCompatActivity(), Navigator, PublicKeyRequester, ChargeRequester, CustomerInfoRequester, CardEntryCallbacks {

  private val compositeDisposable = CompositeDisposable()
  private var publicKey: String? = null
  private var accountId: String? = null
  private var accountName: String? = null
  private var disableCardReader: Boolean? = false
  private var trustAccount: Boolean? = false
  private var requireCvv: Boolean? = false
  private var amount: String? = "53"
  private var fragment: Fragment? = null

  @Inject
  lateinit var chargeModel: ChargeModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    App[this].component.inject(this)
    setContentView(R.layout.activity_container_layout)
  }

  @SuppressLint("SourceLockedOrientationActivity")
  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)

    var newView: Fragment? = null
    newView = supportFragmentManager.findFragmentByTag("currentFragment")
      val fragmentTransaction = supportFragmentManager.beginTransaction()
      fragmentTransaction.detach(newView!!);
      fragmentTransaction.attach(newView);
      fragmentTransaction.commit();
  }

  override fun onStart() {
    super.onStart()

    if (supportFragmentManager.backStackEntryCount == 0) {
      goToScreen(LoginAccountView())
    }

    ivBack.setOnClickListener {
      onBackPressed()
    }

    tvCancel.setOnClickListener {
      showDialog()
    }
  }

  override fun onStop() {
    compositeDisposable.dispose()
    super.onStop()
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 1) {
      if(supportFragmentManager.backStackEntryCount == 3) {
        topContainer.visibility = View.VISIBLE
      } else {
        topContainer.visibility = View.GONE
      }
      supportFragmentManager.popBackStackImmediate()
    } else {
      finish()
    }
  }

  @SuppressLint("ResourceAsColor")
  private fun showDialog(){
    val builder = this?.let {
      AlertDialog.Builder(it)
    }
    builder.setMessage(getString(R.string.card_entry_cancel_dialog_title))
    builder.setCancelable(false)

    builder.setPositiveButton(getString(R.string.card_entry_cancel_dialog_pos_button)) { di: DialogInterface, _: Int ->
      di.dismiss()
      onCancelButtonPress()
    }

    builder.setNegativeButton(getString(R.string.card_entry_cancel_dialog_neg_button)) { di: DialogInterface, _: Int ->
      di.dismiss()
    }
    val alert = builder.create()
    alert.show()

    alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE)
    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(R.style.PositiveAlertTextStyle)
    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE)
    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(R.style.NegativeAlertTextStyle)
  }
  private fun goToScreen(newView: Fragment) {
    val fragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.fragment_container, newView, "currentFragment").addToBackStack(newView.tag)
    fragmentTransaction.commit()
  }

  override fun onAccountChosen(publicKey: String, accountId:String, accountName: String, trustAccount:Boolean) {
    this.publicKey = publicKey
    this.accountId = accountId
    this.accountName = accountName
    this.trustAccount = trustAccount
  }

  override fun goToCardEntryScreen() {
    topContainer.visibility = View.VISIBLE
    fragment = ViewProvider.getCardEntryView(this,TokenizationInitParams(publicKey as String,
            amount as String, accountName as String, disableCardReader as Boolean, trustAccount as Boolean, requireCvv as Boolean))
    goToScreen(fragment as CardEntryFragment)
  }

  override fun onReturnCardData(typeOfCharge: CardResult) {
    goToCustomerInfoScreen()
  }

  override fun onCustomerInfoReceived(tokenizationCompleteParams: TokenizationCompleteParams) {
    val cardEntryHelper = CardEntryHelper(this)
    cardEntryHelper.startTokenization(TokenizationInitParams(publicKey as String, amount as String,
            accountName as String, disableCardReader as Boolean, trustAccount as Boolean, requireCvv as Boolean), tokenizationCompleteParams)
  }

  private fun goToCustomerInfoScreen() {
    topContainer.visibility = View.GONE
    goToScreen(ViewProvider.getCustomerInfoView())
  }

  override fun onManualChargeDataReceived(manualChargeResult: ChargeResult) {
    Log.d("ContainerActivity", "onManualChargeDataReceived${manualChargeResult.oneTimeToken}")
    compositeDisposable.add(chargeModel.makeCharge(publicKey as String, accountId as String, manualChargeResult.oneTimeToken,
            manualChargeResult.amount).subscribeOn
    (Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ charge ->
              (fragment as CardEntryFragment).completionCallback(true)
            }, { error ->
              (fragment as CardEntryFragment).completionCallback(false)
              Toast.makeText(this,error.message,Toast.LENGTH_LONG).show()
              print(error.message)
            }))
  }

  override fun onSwipeChargeDataReceived(swipeChargeResult: ChargeResult) {
    Log.d("ContainerActivity", "onSwipeChargeDataReceived${swipeChargeResult.oneTimeToken}")
    compositeDisposable.add(chargeModel.makeCharge(publicKey as String, accountId as String, swipeChargeResult.oneTimeToken, swipeChargeResult.amount).subscribeOn
    (Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ charge ->
              (fragment as CardEntryFragment).completionCallback(true)
            }, { error ->
              (fragment as CardEntryFragment).completionCallback(false)
              Toast.makeText(this,error.message,Toast.LENGTH_LONG).show()
              print(error.message)
            }))
  }

  override fun onEMVChargeDataReceived(emvChargeResult: ChargeResult) {
    Log.d("ContainerActivity", "onEMVChargeDataReceived${emvChargeResult.oneTimeToken}")
    compositeDisposable.add(chargeModel.makeEMVCharge(publicKey as String, accountId as String, emvChargeResult.oneTimeToken, emvChargeResult.amount, emvChargeResult
            .paymentDataSource,
            emvChargeResult.pointOfSale as PointOfSale).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ charge ->
      (fragment as CardEntryFragment).completionCallback(true)
    }, { error ->
      (fragment as CardEntryFragment).completionCallback(false)
      Toast.makeText(this,error.message,Toast.LENGTH_LONG).show()
      print(error.message)
    }))
  }

  override fun onReset() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onDismiss() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onComplete() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onBackButtonPress() {
    if(supportFragmentManager.backStackEntryCount>1){
      supportFragmentManager.popBackStack()
    }
  }

  override fun onCancelButtonPress() {
    if(supportFragmentManager.backStackEntryCount>1){
      topContainer.visibility = View.GONE
      supportFragmentManager.popBackStack()
    }
  }

}