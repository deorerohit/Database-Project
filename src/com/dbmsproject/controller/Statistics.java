package com.dbmsproject.controller;

import com.dbmsproject.dataholders.Categories;
import com.dbmsproject.dataholders.DataForPieChart;
import com.dbmsproject.dataholders.Members;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class Statistics implements Initializable {

	@FXML
	private  TextField total_amt_spent;
	@FXML
	private ComboBox mem_combobox;
	@FXML
	private TextField tf_items_by_members;
	@FXML
	private ComboBox category_combobox;
	@FXML
	private TextField tf_no_in_category;
	@FXML
	private PieChart member_wise_pie_chart;

	@FXML
	private PieChart category_wise_pie_chart;



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		populateDataInMemberwisePiechart();
		populateDataInCategoryWisePiechart();
		prepareCategoryData();
		prepareMemberData();
		calcTotalAmountSpent();

	}

	private void prepareCategoryData() {
		ObservableList<String> categoriesComBox = FXCollections.observableArrayList();
		String query = "SELECT cat_name FROM categories";
		ResultSet rs = executeMyQuery(query);

		try{
			while (rs.next()) {
				categoriesComBox.add(rs.getString(1));
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

		category_combobox.setItems(categoriesComBox);
	}

	public void calcValueForCategory(javafx.event.ActionEvent actionEvent) {
		String query = "SELECT count(id) from grocery where cat_id=(select cat_id from categories where cat_name = '"+category_combobox.getValue().toString()+"')";
		ResultSet rs = executeMyQuery(query);

		try{
			while (rs.next()) {
				tf_no_in_category.setText(rs.getInt(1)+"");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}




	private void prepareMemberData() {
		EditMembers editMembers = new EditMembers();

		ObservableList<Members> membersObservableList = editMembers.getAllMembers();
		ObservableList<String> comboBoxValues = FXCollections.observableArrayList();
		//	ComboBox<Members> comboBox = new ComboBox<>(comboBoxValues);
		for (Members members : membersObservableList) {
			comboBoxValues.add(members.getMemName());
		}
		mem_combobox.setItems(comboBoxValues);

	}

	public void calcValueForMember(javafx.event.ActionEvent actionEvent) {
		String query = "SELECT count(id) from grocery where mem_id=(select mem_id from members where mem_name = '"+mem_combobox.getValue().toString()+"')";
		ResultSet rs = executeMyQuery(query);

		try{
			while (rs.next()) {
				tf_items_by_members.setText(rs.getInt(1)+"");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}



	private void calcTotalAmountSpent() {
		String query = "SELECT round(sum(price), 2) from grocery";
		ResultSet rs = executeMyQuery(query);

		try {
			while (rs.next()) {
				total_amt_spent.setText(rs.getFloat(1)+"");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}



	private void populateDataInCategoryWisePiechart() {

		ObservableList<DataForPieChart> categoryWiseData = getData("categories", "cat_name", "cat_id");
		ObservableList<PieChart.Data> categoryWisePieChartData = FXCollections.observableArrayList();

		for(DataForPieChart dataForPieChart : categoryWiseData) {
			PieChart.Data pieData = new PieChart.Data(dataForPieChart.getName(), dataForPieChart.getMoney());
			categoryWisePieChartData.add(pieData);
		}

		category_wise_pie_chart.setData(categoryWisePieChartData);

		category_wise_pie_chart.setTitle("Category wise total money spent");
		category_wise_pie_chart.setClockwise(true);
		category_wise_pie_chart.setLabelLineLength(10);
		category_wise_pie_chart.setLabelsVisible(false);
		category_wise_pie_chart.setStartAngle(180);
		DecimalFormat df = new DecimalFormat("##.00");
		categoryWisePieChartData.forEach(data ->
				data.nameProperty().bind(
						Bindings.concat(
								data.getName(), " ₹", df.format(data.getPieValue())
						)
				)
		);
	}




	public void populateDataInMemberwisePiechart() {
		ObservableList<DataForPieChart> memberWiseData = getData("members", "mem_name", "mem_id");
		ObservableList<PieChart.Data> memberwisePieChartData = FXCollections.observableArrayList();



		for (DataForPieChart dataForPieChart : memberWiseData) {
			PieChart.Data pieData = new PieChart.Data(dataForPieChart.getName(), dataForPieChart.getMoney());
			memberwisePieChartData.add(pieData);
		}

		member_wise_pie_chart.setData(memberwisePieChartData);


		member_wise_pie_chart.setTitle("Member wise total money spent");
		member_wise_pie_chart.setClockwise(true);
		member_wise_pie_chart.setLabelLineLength(10);
		member_wise_pie_chart.setLabelsVisible(false);
		member_wise_pie_chart.setStartAngle(180);
		DecimalFormat df = new DecimalFormat("##.00");
		memberwisePieChartData.forEach(data ->
				data.nameProperty().bind(
						Bindings.concat(
								data.getName(), " ₹", df.format(data.getPieValue())
						)
				)
		);

	}


	public ObservableList<DataForPieChart> getData(String tableName, String varName, String varId) {

		ObservableList<DataForPieChart> memberWiseData = FXCollections.observableArrayList();
		ResultSet rs;

		String query = "SELECT "+ tableName+"."+varName+", ROUND(SUM(grocery.price), 2) FROM grocery JOIN "+tableName+" ON grocery."+varId+" = "+tableName+"."+varId+" GROUP By "+tableName+"."+varName;
		rs = executeMyQuery(query);

		try {
			while (rs.next()) {
				DataForPieChart dataForPieChart = new DataForPieChart(rs.getString(1), rs.getFloat(2));
				memberWiseData.add(dataForPieChart);
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		return memberWiseData;
	}



	public Connection getConnection() {
		Connection conn;
		try {

			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmsproject?autoReconnect=true&useSSL=false", "root", "7887");
		
			return conn;
		} catch (Exception e) {
			System.out.println("Error :" + e.getMessage());
			return null;
		}
	}


	private ResultSet executeMyQuery(String query) {
		Connection conn = getConnection();
		Statement st;
		ResultSet rs=null;

		try {
			st = conn.createStatement();
			rs  = st.executeQuery(query);

		} catch (SQLException throwable) {
			throwable.printStackTrace();
		}
		return rs;
	}


}
