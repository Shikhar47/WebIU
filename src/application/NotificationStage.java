package application;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class NotificationStage{

	public NotificationStage(String notification) {
		Text text = new Text(notification);
		
		StackPane root = new StackPane();
		Scene scene = new Scene(root, 300, 150);
		Stage notificationStage = new Stage();
		
		root.getChildren().add(text);
		
		text.setFill(Color.RED);
		
		notificationStage.setScene(scene);
		notificationStage.show();
		
		notificationStage.setOnCloseRequest(e -> {
			new LoginWindow();
		});
	}
}