package com.dbmsproject;

import com.dbmsproject.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	private MainController mainController;


	@Override
	public void start(Stage primaryStage) throws Exception {

		//  FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main_window.fxml"));
		//  AnchorPane anchorPane = fxmlLoader.load();
		//  controller =fxmlLoader.getController();


		Parent root = FXMLLoader.load(getClass().getResource("fxml/main_window.fxml"));
		primaryStage.setTitle("Household Grocery Manager");
		primaryStage.setScene(new Scene(root, 1200, 800));
		primaryStage.setResizable(false);
		primaryStage.show();

       /* Scene scene = new Scene(anchorPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Household Manager");
        primaryStage.setResizable(false);
        primaryStage.show();
*/

	}


	public static void main(String[] args) {
		launch(args);
	}
}
