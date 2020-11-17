package com.affinipay

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class LoginAccountView : Fragment() {

  lateinit var navigator: Navigator
  lateinit var publicKeyRequester: PublicKeyRequester
  val compositeDisposable: CompositeDisposable = CompositeDisposable()

  @Inject
  lateinit var model: LoginAccountModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.activity_main, container, false)
  }

  override fun onAttach(activity: Activity) {
    super.onAttach(activity)
    App[activity].component.inject(this)
    navigator = activity as Navigator
    publicKeyRequester = activity as PublicKeyRequester
  }

    override fun onResume() {
        super.onResume()

        val disposable = model.getAccounts().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ accounts: Accounts? ->

                    val allAccounts: ArrayList<Account> = ArrayList()
                    allAccounts.addAll(accounts?.testAccounts as Collection<Account>)
                    allAccounts.addAll(accounts.liveAccounts as Collection<Account>)
                    val dataAdapter = activity?.let { ArrayAdapter<Account>(it, android.R.layout.simple_spinner_item, allAccounts) }
                    dataAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    account_spinner.adapter = dataAdapter
                    account_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            val publicKey = allAccounts[position].publicKey as String
                            publicKeyRequester.onAccountChosen(publicKey, allAccounts[position].id, allAccounts[position].name,
                                    allAccounts[position].trustAccount)
                            get_paid.isEnabled = true
                            err_message.visibility = View.GONE
                        }
                    }
                }, { e: Throwable? ->
                    Log.d("uhoh", e.toString())
                    err_message.text = resources.getString(R.string.generic_error_message)
                    get_paid.isEnabled = false
                    err_message.visibility = View.VISIBLE
                })

        compositeDisposable.add(disposable)

        get_paid.setOnClickListener {
            navigator.goToCardEntryScreen()
        }
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }
}
