package com.affinipay.dagger

import android.content.Context
import com.affinipay.App
import com.affinipay.ChargeModel
import com.affinipay.LoginAccountModel

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestComponentRule(val context: Context) : TestRule {

    private var mTestComponent: AppTestComponent? = null

    val mockChargeModel: ChargeModel
        get() = mTestComponent!!.chargeModel()

    val mockLoginAccountModel: LoginAccountModel
        get() = mTestComponent!!.loginAccountModel()

    private fun setupDaggerTestComponentInApplication() {
        val application = App[context]
        mTestComponent = DaggerAppTestComponent.builder().networkTestModule(NetworkTestModule(application)).build()
        application.component = mTestComponent as AppComponent
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    setupDaggerTestComponentInApplication()
                    base.evaluate()
                } finally {
                    mTestComponent = null
                }
            }
        }
    }
}
