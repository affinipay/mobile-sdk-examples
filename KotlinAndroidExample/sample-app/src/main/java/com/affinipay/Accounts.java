package com.affinipay;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Accounts {

  @SerializedName("test_accounts") private ArrayList<Account> testAccounts;
  @SerializedName("live_accounts") private ArrayList<Account> liveAccounts;

  public ArrayList<Account> getTestAccounts() {
    return testAccounts;
  }

  public ArrayList<Account> getLiveAccounts() {
    return liveAccounts;
  }
}
