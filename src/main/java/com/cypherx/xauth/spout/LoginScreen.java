package com.cypherx.xauth.spout;

import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.TextField;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class LoginScreen extends GenericPopup implements xAuthScreen {
	private final xAuth plugin;
	private Label lblHeader;
	private Label lblPassword;
	private TextField txtPassword;
	private Button btnLogin;
	private Button btnCancel;

	public LoginScreen(xAuth plugin, SpoutPlayer player) {
		this.plugin = plugin;
		int width = player.getMainScreen().getWidth();
		int height = player.getMainScreen().getHeight();

		lblHeader = new GenericLabel();
		lblHeader.setText("Please log in using the form below:").setHexColor(0xFFFFFF);
		lblHeader.setCentered(true);
		lblHeader.setX(width / 2).setY(40);
		attachWidget(lblHeader);

		txtPassword = new GenericTextField();
		txtPassword.setWidth(150).setHeight(12);
		txtPassword.setX((width / 2) - (txtPassword.getWidth() / 2));
		txtPassword.setY((height / 2) - (txtPassword.getHeight() / 2));
		txtPassword.setMaximumCharacters(64);
		attachWidget(txtPassword);

		lblPassword = new GenericLabel();
		lblPassword.setText("Password:").setHexColor(0xFFFFFF);
		lblPassword.setX(txtPassword.getX()).setY(txtPassword.getY() - 10);
		attachWidget(lblPassword);

		btnLogin = new GenericButton();
		btnLogin.setText("Login").setCentered(true);
		btnLogin.setWidth((txtPassword.getWidth() / 2)).setHeight(20).setX(txtPassword.getX()).setY(txtPassword.getY() + 15);
		attachWidget(btnLogin);

		btnCancel = new GenericButton();
		btnCancel.setText("Cancel").setCentered(true);
		btnCancel.setWidth(btnLogin.getWidth()).setHeight(20).setX(txtPassword.getX() + btnLogin.getWidth()).setY(txtPassword.getY() + 15);
		attachWidget(btnCancel);
	}

	public void handleClick(Button button, SpoutPlayer player) {
		if (button.equals(btnLogin)) {
			xAuthPlayer xPlayer = plugin.getPlayer(player.getName());

			if (plugin.checkPassword(xPlayer.getAccount(), txtPassword.getText()))
				System.out.println("Correct");
			else
				System.out.println("Incorrect");
		} else if (button.equals(btnCancel))
			this.close();
	}

	public void handleClose(SpoutPlayer player) {
		
	}
}