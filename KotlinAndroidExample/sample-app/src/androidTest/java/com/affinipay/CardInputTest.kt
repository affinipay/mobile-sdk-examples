package com.affinipay

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.affinipay.cardreadersdk.CardInputParentActivity
import com.affinipay.cardreadersdk.R
import com.affinipay.cardreadersdk.charge.CardEntryFragment
import com.affinipay.cardreadersdk.charge.OneTimeToken
import com.affinipay.dagger.TestComponentRule
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.IOException
import java.io.InputStreamReader

@RunWith(AndroidJUnit4::class) class CardInputTest {

  val component = TestComponentRule(InstrumentationRegistry.getTargetContext())
  var mActivityRule = ActivityTestRule<CardInputParentActivity>(CardInputParentActivity::class.java, true, false)
  @JvmField @Rule
  var chain: TestRule = RuleChain.outerRule(component).around(mActivityRule)

  @Before fun init() {
    val intent = Intent()
    intent.putExtra(CardEntryFragment.ARG_PUBLIC_KEY, "PUBLIC_KEY")
    intent.putExtra(CardEntryFragment.ARG_CHARGE_AMOUNT, "389")
    mActivityRule.launchActivity(intent)
  }

  @Test fun doesChargeInputReflectAmountPassedIn() {
    onView(withId(R.id.button_charge)).check(matches(withText("Charge $3.89")))
  }

  @Test fun testCardNumberInput() {
    onView(withId(R.id.input_cardnumber)).perform(click())
    onView(withId(R.id.keypad1)).perform(click())
    onView(withId(R.id.keypad7)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad3)).perform(click())
    onView(withId(R.id.input_cardnumber)).check(matches(withText("1723")))
    onView(withId(R.id.keypadDelete)).perform(click())
    onView(withId(R.id.keypadDelete)).perform(click())
    onView(withId(R.id.input_cardnumber)).check(matches(withText("17")))
    onView(withId(R.id.keypadClear)).perform(click())
    onView(withId(R.id.input_cardnumber)).check(matches(withText("")))
  }

  @Test fun testInputValidation() {
    onView(withId(R.id.button_charge)).check(matches(not((isEnabled()))))
    onView(withId(R.id.input_cardnumber)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.input_cardnumber)).check(matches(withText("4242424242424242")))
    onView(withId(R.id.button_charge)).check(matches(not((isEnabled()))))

    //check invalid month
    onView(withId(R.id.keypad1)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.error_message)).check(matches(withText("Invalid Expiration Date.")))
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.input_month_expiration)).check(matches(withText("12")))
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad3)).perform(click())
    onView(withId(R.id.input_year_expiration)).check(matches(withText("23")))
    onView(withId(R.id.button_charge)).check(matches(not((isEnabled()))))

    onView(withId(R.id.keypad7)).perform(click())
    onView(withId(R.id.keypad8)).perform(click())
    onView(withId(R.id.keypad1)).perform(click())
    onView(withId(R.id.input_cvv)).check(matches(withText("781")))
    onView(withId(R.id.button_charge)).check(matches((isEnabled())))
    onView(withId(R.id.keypad1)).perform(click())
    onView(withId(R.id.keypad1)).perform(click())
    onView(withId(R.id.button_charge)).check(matches(not((isEnabled()))))
    onView(withId(R.id.error_message)).check(matches(withText("Invalid CVV.")))
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
    onView(withId(R.id.input_cardnumber)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())

    onView(withId(R.id.input_month_expiration)).perform(typeText("12"))
    onView(withId(R.id.input_year_expiration)).perform(typeText("22"))
    onView(withId(R.id.input_cvv)).perform(typeText("333"))

    onView(withId(R.id.input_name)).perform(typeText("Sarvesh Rajasekaran"))

    onView(withId(R.id.button_charge)).perform(click())

    onView(withId(R.id.input_name)).perform(typeText("Sarvesh Rajasekaran"))


  }
}
