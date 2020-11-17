package com.affinipay;

import com.google.gson.annotations.SerializedName;

public class Account {

  @SerializedName("id") private String id;
  @SerializedName("name") private String name;
  @SerializedName("public_key") private String publicKey;
  @SerializedName("secret_key") private String secretKey;
  @SerializedName("trust_account") private Boolean trustAccount;

  public String getPublicKey() {
    return publicKey;
  }

  public String getId() { return id; }

  public String getName() { return name; }

  public Boolean getTrustAccount() { return trustAccount; }

  @Override
  public String toString() {
    return name;
  }
}
