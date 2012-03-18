package com.cypherx.xauth.database;

public enum Table {
	ACCOUNT ("accounts"),
	LOCATION ("locations"),
	LOCKOUT ("lockouts"),
	PLAYERDATA ("playerdata"),
	SESSION ("sessions");

	private String name;

	Table(String name) {
		this.name = name;
	}

	public String getName() { return name; }
}