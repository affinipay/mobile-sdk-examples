package com.affinipay

import android.Manifest
import android.content.Intent
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.affinipay.cardreadersdk.CardInputParentActivity
import com.affinipay.cardreadersdk.charge.CardEntryFragment
import com.affinipay.cardreadersdk.charge.OneTimeToken
import com.affinipay.dagger.TestComponentRule
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import org.hamcrest.core.IsNot.not
import org.junit.*
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.IOException
import java.io.InputStreamReader

@RunWith(AndroidJUnit4::class) class CardInputTest {
  private val component = TestComponentRule(InstrumentationRegistry.getTargetContext())
  @JvmField @Rule
  var mActivityRule = ActivityTestRule<CardInputParentActivity>(CardInputParentActivity::class.java, true, false)
  @JvmField @Rule
  var chain: TestRule = RuleChain.outerRule(component).around(mActivityRule)
  @JvmField @Rule
  var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE)

  @Before fun init() {
    val intent = Intent()
    intent.putExtra(CardEntryFragment.ARG_PUBLIC_KEY, "PUBLIC_KEY")
    intent.putExtra(CardEntryFragment.ARG_CHARGE_AMOUNT, "389")
    mActivityRule.launchActivity(intent)
  }

  @Test fun testCardNumberInput() {
    onView(withId(R.id.etCardNumber)).perform(click())
    onView(withId(R.id.etCardNumber)).perform(typeText("1723"))
    onView(withId(R.id.etCardNumber)).check(matches(withText("1723")))
  }

  @Test fun testInputValidation() {
    onView(withId(R.id.floatingActionButton)).check(matches(not((isEnabled()))))
    onView(withId(R.id.etCardNumber)).perform(click())
    onView(withId(R.id.etCardNumber)).perform(typeText("4242424242424242"))
    onView(withId(R.id.etCardNumber)).check(matches(withText("4242 4242 4242 4242")))
    onView(withId(R.id.floatingActionButton)).check(matches(not((isEnabled()))))

    //check valid month
    onView(withId(R.id.etExpMonth)).perform(click())
    onView(withId(R.id.etExpMonth)).perform(typeText("12"))
    onView(withId(R.id.etExpMonth)).check(matches(withText("12/")))
    onView(withId(R.id.floatingActionButton)).check(matches(not((isEnabled()))))

    onView(withId(R.id.etExpMonth)).perform(typeText("22"))
    onView(withId(R.id.etExpMonth)).check(matches(withText("12/22")))

    onView(withId(R.id.etCvv)).perform(click())
    onView(withId(R.id.etCvv)).perform(typeText("781"))
    onView(withId(R.id.etCvv)).check(matches(withText("781")))
    onView(withId(R.id.floatingActionButton)).check(matches((isEnabled())))
  }

  @Ignore
  @Test fun testManualEntryCharge() {

    var reader: JsonReader? = null
    try {
      reader = JsonReader(InputStreamReader(InstrumentationRegistry.getContext().assets.open("200_charge_response.json")))
    } catch (e: IOException) {
      e.printStackTrace()
    }

    val oneTimeToken = OneTimeToken()
    Mockito.`when`(component.mockChargeModel.makeCharge("000", "123", oneTimeToken.id as String, "389"))
        .thenReturn(Observable.just(Gson().fromJson<Charge>(reader!!, Charge::class.java)))

    //Enter Card Number
    onView(withId(R.id.etCardNumber)).perform(click())
    onView(withId(R.id.etCardNumber)).perform(typeText("4242424242424242"))

    onView(withId(R.id.etExpMonth)).perform(typeText("12/22"))
    onView(withId(R.id.etCvv)).perform(typeText("333"))

    onView(withId(R.id.floatingActionButton)).perform(click())
  }
}
