package com.cypherx.xauth.updater;

public enum UpdatePriority {
	LOW(1),
	MEDIUM(2),
	HIGH(3);

	int level;

	UpdatePriority(int level) {
		this.level = level;
	}

	public static UpdatePriority getPriority(int level) {
		for (UpdatePriority p : values())
			if (p.level == level)
				return p;

		return null;
	}
}