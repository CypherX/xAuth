package com.cypherx.xauth;

import java.sql.Timestamp;


public class Session {
	private int accountId;
	private String host;
	private Timestamp loginTime;

	public Session() {}

	public Session(int accountId, String host) {
		this.accountId = accountId;
		this.host = host;
		loginTime = Util.getNow();
	}

	public boolean isExpired() {
		Timestamp expireTime = new Timestamp(loginTime.getTime() + (xAuthSettings.sessionLength * 1000));
		if (expireTime.compareTo(Util.getNow()) < 0)
			return true;

		return false;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setLoginTime(Timestamp loginTime) {
		this.loginTime = loginTime;
	}

	public Timestamp getLoginTime() {
		return loginTime;
	}
}