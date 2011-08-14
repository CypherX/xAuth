package com.cypherx.xauth.spout;

import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.player.SpoutPlayer;

public interface xAuthScreen extends PopupScreen {
	public void handleClick(Button button, SpoutPlayer player);
	public void handleClose(SpoutPlayer player);
}