package com.affinipay;

import com.google.gson.annotations.SerializedName;

public class Account {

  @SerializedName("id") private String id;
  @SerializedName("name") private String name;
  @SerializedName("public_key") private String publicKey;
  @SerializedName("secret_key") private String secretKey;

  public String getPublicKey() {
    return publicKey;
  }

  public String getId() { return id; }

  @Override
  public String toString() {
    return name;
  }
}
