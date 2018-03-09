package com.affinipay

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.affinipay.cardreadersdk.R
import com.affinipay.cardreadersdk.ViewProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class) class AmountSelectionViewTest {

  @Rule @JvmField
  var mActivityRule = ActivityTestRule<ContainerActivity>(ContainerActivity::class.java, true, false)

  @Before fun init() {
    mActivityRule.launchActivity(Intent())
    val fragmentTransaction = mActivityRule.activity.fragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.fragment_container, ViewProvider.getAmountSelectionView())
    fragmentTransaction.commit()

  }

  @Test fun doesChargeInputStartAtZero() {
    onView(withId(R.id.amount)).check(matches(withText("$0.00")))
  }

  @Test fun testDeleteButton() {
    onView(withId(R.id.keypad1)).perform(click())
    onView(withId(R.id.keypad7)).perform(click())
    onView(withId(R.id.keypad2)).perform(click())
    onView(withId(R.id.amount)).check(matches(withText("$1.72")))
    onView(withId(R.id.keypadDelete)).perform(click())
    onView(withId(R.id.keypadDelete)).perform(click())
    onView(withId(R.id.amount)).check(matches(withText("$0.01")))
  }

  @Test fun testKeyPad() {
    onView(withId(R.id.keypad3)).perform(click())
    onView(withId(R.id.keypad4)).perform(click())
    onView(withId(R.id.keypad0)).perform(click())
    onView(withId(R.id.keypad9)).perform(click())
    onView(withId(R.id.amount)).check(matches(withText("$34.09")))
  }

  @Test fun testClearButton() {
    onView(withId(R.id.keypad8)).perform(click())
    onView(withId(R.id.keypad5)).perform(click())
    onView(withId(R.id.keypad6)).perform(click())
    onView(withId(R.id.amount)).check(matches(withText("$8.56")))
    onView(withId(R.id.keypadClear)).perform(click())
    onView(withId(R.id.amount)).check(matches(withText("$0.00")))
  }
}
