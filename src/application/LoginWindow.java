package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginWindow {

	Text nameText,passText;
	static Text message;
	TextField name;
	PasswordField pass;
	Button signUp,logIn;
	Stage loginStage;
	GridPane loginLayout;
	Scene loginScene;
	static boolean loggedin = false;	
	static String nameStr,passStr;
	
	LoginWindow()
	{
		loginStage = new Stage();
		
		nameText = new Text("Name : ");
		passText = new Text("Password:");
		message = new Text();
		message.setStyle("-fx-text-fill: red");
		name = new TextField();
			name.setPromptText("Enter name");
		pass = new PasswordField();
			pass.setPromptText("Enter password");
			
		signUp = new Button("Sign Up");
		logIn = new Button("Log In");	

		
		signUp.setOnAction(e -> {
			nameStr = name.getText();
			passStr = pass.getText();
			if(nameStr.isEmpty() || passStr.isEmpty())
			{
				message.setFill(Color.RED);
				message.setText("Please enter name and password");
			}
			else
			{
				DbConnect dbClass = new DbConnect("users");
					dbClass.insert(nameStr, passStr);
			}
		});
		
		logIn.setOnAction(e -> {
			nameStr = name.getText();
			passStr = pass.getText();
			if(nameStr.isEmpty() || passStr.isEmpty())
			{
				message.setFill(Color.RED);
				message.setText("Please enter name and password");
			}
			else
			{
				DbConnect dbClass = new DbConnect("users");
					dbClass.find(nameStr,passStr);
			}
		});
		
		loginLayout = new GridPane();
		loginLayout.setAlignment(Pos.CENTER);
		nameText.getStyleClass().add("nameTextStyle");
		name.getStyleClass().add("nameStyle");
		passText.getStyleClass().add("passTextStyle");
		pass.getStyleClass().add("passStyle");
		message.getStyleClass().add("messageStyle");
		signUp.getStyleClass().add("signUpStyle");
		logIn.getStyleClass().add("logInStyle");
		
		
			loginLayout.setPadding(new Insets(0, 0, 10, 25));
			loginLayout.setVgap(10);
			loginLayout.setHgap(15);
			loginLayout.add(nameText, 0, 0);
			loginLayout.add(name, 1, 0);
			loginLayout.add(passText, 0, 1);
			loginLayout.add(pass, 1, 1);
			//add colspan to message for expansion.rowspan=1 is default & must be 1 or greater 
			loginLayout.add(message, 0, 2, 3, 1);
			loginLayout.add(signUp, 0, 3);
			loginLayout.add(logIn, 1, 3);

		loginScene = new Scene(loginLayout);
		
		loginStage = new Stage();
			loginStage.setTitle("Login");
			loginStage.setWidth(420);
			loginStage.setHeight(250);
			loginStage.setScene(loginScene);
		loginStage.show();
	}
}