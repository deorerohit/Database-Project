package com.dbmsproject.controller;

import com.dbmsproject.connection.ManageConnection;
import com.dbmsproject.dataholders.Categories;
import com.dbmsproject.dataholders.Grocery;
import com.dbmsproject.dataholders.Members;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {


	@FXML
	private ComboBox cb_category;
	@FXML
	private TextField tf_item_name;
	@FXML
	private TextField tf_quantity;
	@FXML
	private TextField tf_price;
	@FXML
	private DatePicker dp_date;
	@FXML
	private ComboBox cb_family_member;
	@FXML
	private TableView<Grocery> tableView_grocery;
	@FXML
	private TableColumn<Grocery, Integer> col_id;
	@FXML
	private TableColumn<Grocery, String> col_itemname;
	@FXML
	private TableColumn<Grocery, Integer> col_quantity;
	@FXML
	private TableColumn<Grocery, Float> col_price;
	@FXML
	private TableColumn<Grocery, String> col_date;
	@FXML
	private TableColumn<Grocery, String> col_orderedby;
	@FXML
	public TableColumn<Grocery, String> col_category;
	@FXML
	private Button btn_add;
	@FXML
	private Button btn_update;
	@FXML
	private Button btn_delete;

	private Grocery currentlySelectedGrocery = null;

	public ManageConnection manageConnection;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		manageConnection = ManageConnection.createInstance();

		showGroceryInTable();
		setMembersInComboBox();
		setCategoriesInComboBox();

	}

	private void setCategoriesInComboBox() {

		ObservableList<Categories> allCategoriesList = FXCollections.observableArrayList();
		String query = "SELECT * FROM categories";
		ResultSet rs = manageConnection.executeQueryForResult(query);

		try {
			while (rs.next()) {

				Categories categories = new Categories(rs.getInt("cat_id"), rs.getString("cat_name"));
				allCategoriesList.add(categories);
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		ObservableList<String> categoriesComBox = FXCollections.observableArrayList();

		for (Categories categories : allCategoriesList) {
			categoriesComBox.add(categories.getCat_name());
		}
		cb_category.setItems(categoriesComBox);
	}

	public void showGroceryInTable() {

		ObservableList<Grocery> groceryList = getAllGroceries();

		col_id.setCellValueFactory(new PropertyValueFactory<Grocery, Integer>("id"));
		col_itemname.setCellValueFactory(new PropertyValueFactory<Grocery, String>("item_name"));
		col_quantity.setCellValueFactory(new PropertyValueFactory<Grocery, Integer>("quantity"));
		col_price.setCellValueFactory(new PropertyValueFactory<Grocery, Float>("price"));
		col_date.setCellValueFactory(new PropertyValueFactory<Grocery, String>("date"));
		col_orderedby.setCellValueFactory(new PropertyValueFactory<Grocery, String>("orderBy"));
		col_category.setCellValueFactory(new PropertyValueFactory<Grocery, String>("category"));

		tableView_grocery.setItems(groceryList);
		col_id.setSortType(TableColumn.SortType.ASCENDING);
		tableView_grocery.getSortOrder().add(col_id);
		tableView_grocery.sort();
	}


	public ObservableList<Grocery> getAllGroceries() {
		ObservableList<Grocery> groceryObservableList = FXCollections.observableArrayList();
		String query = "SELECT grocery.id, grocery.item_name, grocery.quantity, grocery.price, grocery.ordered_date, members.mem_name, categories.cat_name FROM grocery JOIN members JOIN categories ON grocery.mem_id = members.mem_id AND grocery.cat_id = categories.cat_id";
		ResultSet resultSet = manageConnection.executeQueryForResult(query);


		try {
			while (resultSet.next()) {
				Grocery grocery = new Grocery(resultSet.getInt("id"),
						resultSet.getString("item_name"),
						resultSet.getInt("quantity"),
						resultSet.getFloat("price"),
						resultSet.getString("ordered_date"),
						resultSet.getString(6),
						resultSet.getString(7));

				groceryObservableList.add(grocery);
			}

		} catch (SQLException throwable) {
			throwable.printStackTrace();
		}

		return groceryObservableList;
	}


	public void insertGrocery() {

		if (areFieldsEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Empty fields");
			alert.setHeaderText(null);
			alert.setContentText("Please fill the data in all the fields");
			alert.showAndWait();
			return;
		}

		if (!tf_quantity.getText().matches("\\d*")) {
			tf_quantity.setText("");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Illegal Data");
			alert.setHeaderText(null);
			alert.setContentText("Data you entered in the quantity field is not appropriate\n Should be an integer.");
			alert.showAndWait();
			return;
		}

		if (!tf_price.getText().matches("[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?")) {
			tf_price.setText("");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Illegal Data");
			alert.setHeaderText(null);
			alert.setContentText("Data you entered in the price field is not appropriate\n Should be an integer or decimal.");
			alert.showAndWait();
			return;
		}



		String date = dp_date.getValue() + "";
		String query = "INSERT INTO grocery(item_name, quantity, price, ordered_date, mem_id, cat_id) values('" + tf_item_name.getText() + "',"
				+ tf_quantity.getText() + ","
				+ tf_price.getText() + ",'"
				+ date + "',"
				+ "(SELECT mem_id FROM members WHERE mem_name = '" + cb_family_member.getValue() + "'),"
				+ "(SELECT cat_id FROM categories WHERE cat_name = '" + cb_category.getValue() + "'))";

		manageConnection.executeUpdateQuery(query);
		showGroceryInTable();
	}


	public void updateGrocery() {
		if (areFieldsEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Empty fields");
			alert.setHeaderText(null);
			alert.setContentText("Please fill the data in all the fields");
			alert.showAndWait();
			return;
		}

		String query = "UPDATE grocery set item_name = '" + tf_item_name.getText()
				+ "', quantity= " + tf_quantity.getText()
				+ ", price= " + tf_price.getText()
				+ ", ordered_date= '" + dp_date.getValue()
				+ "', mem_id=(SELECT mem_id from members where mem_name='" + cb_family_member.getValue() + "')"
				+ ", cat_id=(SELECT cat_id from categories where cat_name='" + cb_category.getValue() + "')"
				+ " where id = " + currentlySelectedGrocery.getId();


		manageConnection.executeUpdateQuery(query);
		showGroceryInTable();
	}


	public void deleteGrocery() {

		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Delete?");
		alert.setHeaderText("Your data is being deleted!!");
		alert.setContentText("Are you sure you want to delete??");

		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent()) {
			String query = "DELETE FROM grocery where id = " + currentlySelectedGrocery.getId();

			manageConnection.executeUpdateQuery(query);
			showGroceryInTable();
		}


	}


	private boolean areFieldsEmpty() {

		if (tf_item_name.getText().trim().isEmpty() || tf_price.getText().trim().isEmpty() || tf_quantity.getText().trim().isEmpty() || dp_date.getValue() == null || cb_family_member.getValue() == null || cb_category.getValue() == null)
			return true;
		else
			return false;
	}


	@FXML
	public void handleMouseAction(MouseEvent mouseEvent) {
		currentlySelectedGrocery = tableView_grocery.getSelectionModel().getSelectedItem();

		if (currentlySelectedGrocery != null) {
			tf_item_name.setText(currentlySelectedGrocery.getItem_name());
			tf_quantity.setText(currentlySelectedGrocery.getQuantity() + "");
			tf_price.setText(currentlySelectedGrocery.getPrice() + "");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(currentlySelectedGrocery.getDate(), formatter);
			dp_date.setValue(date);
			cb_family_member.setValue(currentlySelectedGrocery.getOrderBy());
			cb_category.setValue(currentlySelectedGrocery.getCategory());
		}


	}


	public void editMembers() {
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getClassLoader().getResource("com/dbmsproject/fxml/edit_members.fxml"));
			Stage stage = new Stage();
			stage.setTitle("Edit Members");
			stage.setScene(new Scene(root, 600, 400));
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void setMembersInComboBox() {
		ObservableList<String> comboBoxValues = getMembersList();
		cb_family_member.setItems(comboBoxValues);
	}


	public void showStatistics(ActionEvent actionEvent) {
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getClassLoader().getResource("com/dbmsproject/fxml/statistics.fxml"));
			Stage stage = new Stage();
			stage.setTitle("Statistics");
			stage.setScene(new Scene(root, 1000, 800));
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public ObservableList<String> getMembersList() {
		ObservableList<String> membersList = FXCollections.observableArrayList();
		String query = "SELECT mem_name FROM members";
		ResultSet rs = manageConnection.executeQueryForResult(query);

		try {
			while (rs.next()) {
				membersList.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return membersList;
	}
}
