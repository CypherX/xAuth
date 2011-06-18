package com.cypherx.xauth;

import java.sql.Timestamp;

public class StrikeBan {
	private String host;
	private Timestamp banTime;

	public StrikeBan() {}

	public StrikeBan(String host) {
		this.host = host;
		banTime = Util.getNow();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setBanTime(Timestamp banTime) {
		this.banTime = banTime;
	}

	public Timestamp getBanTime() {
		return banTime;
	}
}