package application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {
	
	static TabPane tp;
	
	@Override
	public void start(Stage primaryStage) {
		setUserAgentStylesheet(STYLESHEET_MODENA);
		primaryStage.setMaximized(true);
		primaryStage.getIcons().add(new Image("file:../../images/icon.png"));
		
		tp = new TabPane();
		Scene scene = new Scene(tp);
		NewTab tab1 = new NewTab(primaryStage,scene);
		tp.getTabs().add(tab1);
		tp.setTabMinWidth(140);
		tp.setTabMinWidth(150);		
		tab1.getStyleClass().add("TabPaneStyle");
		
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

		//------Connect to server---------------------------
		new DbConnect();
		
		//------------------------Close connection to database-------------------------------------
		primaryStage.setOnCloseRequest(e -> {
			DbConnect.mongoClient.close();
			});
	}

	public static void main(String[] args) {
		launch(args);
	}


static class NewTab extends Tab
{
	Button addTab,logout;
	static int i=0;
	int numSites = 0,backClicks;
	
	NewTab()
	{}
	
	NewTab(Stage primaryStage,Scene scene)
	{
		i++;
		setText("New Tab " + i);
		addTab = new Button("NewTab");
		setContent(new TabContent(primaryStage,scene));
		addTab.setOnAction(e -> {
			NewTab tabs = new NewTab(primaryStage,scene);
			tp.getTabs().add(tabs);
			tabs.getStyleClass().add("TabPaneStyle");
		});
		
		setOnCloseRequest(new EventHandler<Event>() {			
			@Override
			public void handle(Event arg0) {
				if(tp.getTabs().size() <= 1)
				{
					DbConnect.mongoClient.close();
					primaryStage.close();
				}
			}
		});
	}

class TabContent extends BorderPane
{
		private WebView wv;
		private WebEngine page;
		private Stage primaryStage;
		
		private final String NOT_LOGGED_IN = "You are not logged in";
		
		public TabContent(Stage primaryStage,Scene scene) {
			
			this.primaryStage = primaryStage;
			
			Menu optionMenu = new Menu("Options");
			/*Image image = new Image("file:///C:/Users/Shikhar/workspaceFX/Browser/images/menuIcon.png");
			ImageView imageView = new ImageView(image);
			optionMenu.setGraphic(imageView);*/
			
				MenuItem openTab = new MenuItem("New Tab");
				MenuItem historyMenu = new MenuItem("History");
				MenuItem bookmarksMenu = new MenuItem("Bookmark");
				MenuItem viewBookmarksMenu = new MenuItem("View Bookmarks");
				MenuItem logoutMenu = new MenuItem("Logout");
				MenuItem closeMenu = new MenuItem("Close");
				
				
				openTab.setOnAction(e -> {
					NewTab tabs = new NewTab(primaryStage,scene);
					tp.getTabs().add(tabs);
				});
				
				historyMenu.setOnAction(e -> {
						new HistoryStage(page);
				});
				
				logoutMenu.setOnAction( e -> {
					if(LoginWindow.loggedin == true)
					{
						LoginWindow.loggedin = false;
						LoginWindow.message.setText("You have been logged out");
					}
					else
					{
						new NotificationStage(NOT_LOGGED_IN);
					}
				});
				
				bookmarksMenu.setOnAction(e -> {
					String title = page.getTitle();
					String url = page.getLocation();
					new AddBookmarks(title,url);
				});
				
				viewBookmarksMenu.setOnAction(e -> {
					new BookmarksStage(page);
				});
				
				closeMenu.setOnAction( e -> {
					primaryStage.close();
					DbConnect.mongoClient.close();					
				});
				
				
				optionMenu.getItems().add(openTab);
				optionMenu.getItems().add(new SeparatorMenuItem());
				optionMenu.getItems().add(historyMenu);
				optionMenu.getItems().add(new SeparatorMenuItem());
				optionMenu.getItems().add(bookmarksMenu);
				optionMenu.getItems().add(new SeparatorMenuItem());
				optionMenu.getItems().add(viewBookmarksMenu);
				optionMenu.getItems().add(new SeparatorMenuItem());
				optionMenu.getItems().add(logoutMenu);
				optionMenu.getItems().add(new SeparatorMenuItem());
				optionMenu.getItems().add(closeMenu);
			
			MenuBar menuBar = new MenuBar();
			menuBar.getMenus().addAll(optionMenu);
			
			Button go = new Button();
			Button reload = new Button();
			Button back = new Button();
			//Button forward = new Button();
			//Button history = new Button("History");
			//Button bookmarks = new Button("Bookmarks");
			Button login = new Button("Login");
			//logout = new Button("logout");
			ProgressBar pb = new ProgressBar(0);
			HBox hb2 = new HBox(pb);
			TextField urlText = new TextField();
				urlText.setPromptText("Enter a URL here");
			wv = new WebView();
			page = wv.getEngine();
			HBox hb = new HBox(2,back,urlText,go,reload,addTab,login,menuBar);			
			
			login.setOnAction(e -> {
				new LoginWindow();
			});
			
			/*logout.setOnAction(e -> {
				LoginWindow.loggedin = false;
				LoginWindow.message.setText("You have been logged out");
			});*/
			
			go.setDisable(true);
			pb.setVisible(false);
			pb.setDisable(true);
			back.setDisable(true);
			//forward.setDisable(true);
			reload.setDisable(true);
			
			/*---- Aligning Nodes ----*/
			setTop(hb);
			setCenter(wv);
			setBottom(hb2);
			
			loadLocalPage("C:/Users/Shikhar/workspaceFX/Browser/src/application/newtab.html");
			
			menuBar.getStyleClass().add("menuBarStyle");
			back.getStyleClass().add("backButton");
			//forward.getStyleClass().add("forwardButton");
			reload.getStyleClass().add("reloadButton");
			go.getStyleClass().add("goButton");
			hb.getStyleClass().add("HBoxStyle");
			pb.getStyleClass().add("progressStyle"); 
			addTab.getStyleClass().add("addTabButtonStyle");
			//history.getStyleClass().add("historyButtonStyle");
			//bookmarks.getStyleClass().add("bookmarksButtonStyle");
			
			//--- Changin title of current tab
			ReadOnlyStringProperty r = page.titleProperty();
			r.addListener(e -> {
								setText(r.get());
								});
			
			/*history.setOnAction(e -> {
				new HistoryStage(page);
			});*/
			
			//----- urlText -----------------------------
			urlText.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(
						ObservableValue<? extends String> observable,
						String oldValue, String newValue) {
					if(newValue.length() <= 5)
					{
						go.setDisable(true);
						urlText.setStyle("-fx-text-fill:red");
					}
					else
					{
						go.setDisable(false);
						urlText.setStyle("-fx-text-fill:teal");
					}
				}
			});
			
			/*---- Button Handlers ----*/
			ObservableList<Entry> ol;
			WebHistory wh = page.getHistory();
			ol = wh.getEntries();
			
			/*bookmarks.setOnAction(e -> {
				String title = page.getTitle();
				String url = page.getLocation();
				new AddBookmarks(title,url);
			});*/
			
			back.setOnAction(e -> {
				backClicks++;
				page.load(ol.get(ol.size()-2).getUrl());
			});
			
			/*forward.setOnAction(e -> {
				page.load(ol.get(ol.size()-2).getUrl());
			});*/
			
			go.setTooltip(new Tooltip("Search"));
			go.setOnAction(e -> {
								String str = urlText.getText();
								if(str != null)
								{
									String[] arr = str.split(":");
									if(arr.length <= 1)
										str = "http://" + str;
									if(arr[0].equalsIgnoreCase("c") || arr[0].equalsIgnoreCase("file")
									|| arr[0].equalsIgnoreCase("d") || arr[0].equalsIgnoreCase("e")
									|| arr[0].equalsIgnoreCase("f") || arr[0].equalsIgnoreCase("g"))									
									{			
										loadLocalPage(str);
									}
									else
									{
										page.load(str);
									}
								}
								
							});
			
			//----- Reload button ------------------
			reload.setOnAction(e -> page.reload());
			
			// -- Progress bar look and change progress bar level
			pb.progressProperty().bind(page.getLoadWorker().progressProperty());
			
			// -- Wait for page to load completely then change url,history
			page.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
				@Override
				public void changed(ObservableValue<? extends State> observable,
						State oldValue, State newValue) {
					try {
						if(newValue == Worker.State.FAILED)
						{
							Thread.sleep(350);
							pb.setDisable(true);
							pb.setVisible(false);
							loadLocalPage("C:/Users/Shikhar/workspaceFX/Browser/src/application/failed.html");
						}
						else if(newValue == Worker.State.SCHEDULED)
						{
							pb.setVisible(true);
						}
						else if(newValue == Worker.State.RUNNING) {
							pb.setDisable(false);
							Thread.sleep(500);
				            urlText.setText(page.getLocation());
						}
						else if(newValue == Worker.State.SUCCEEDED) {
							Thread.sleep(500);
							pb.setVisible(false);
							pb.setDisable(true);
							reload.setDisable(false);
							urlText.setText(page.getLocation());
	//-----------------History Storing-------------------------------------------
							if(!ol.isEmpty())
							{
								Entry entry = ol.get(wh.getCurrentIndex());								
								new HistoryStore(entry);
							}							
							numSites++;
							if(numSites > 1)
								back.setDisable(false);
							/*if(backClicks >= 1)
								forward.setDisable(false);*/
						}
					}catch(InterruptedException e)
					{
						System.out.println("InterruptedException in loadWorker Section\n");
						e.printStackTrace();
					}
				}
			});
		}

		public void loadLocalPage(String str)
		{
			String content = "";
			FileInputStream fis = null;
			try {
				File file = new File(str);
				fis = new FileInputStream(file);
				int read;
				while((read = fis.read()) != -1)
				{
					content = content + (char)read;
				}
				page.loadContent(content);
			} catch (Exception e1) {
				//e1.printStackTrace();
			}finally{
				try{
					fis.close();
				}catch(IOException e){}
			}
		}
	}
}
}

class HistoryStore implements Runnable
{
	private Entry entry;
	private Thread t;
	static DbConnect dbconnect = null;
	
	public HistoryStore(Entry entry) {
		this.entry = entry;	
		t = new Thread(this,"HistoryStore");
		t.start();
}

	@Override
	public void run() {
		sendToDb();
	}
	
	private void sendToDb()
	{
		Date date = entry.getLastVisitedDate();
		String title = entry.getTitle();
		String url = entry.getUrl();
		if(LoginWindow.loggedin == true)
		{
			String findQ = "{name:'"+LoginWindow.nameStr+"', pass:'"+LoginWindow.passStr+"'}";
			title = title.replace("'", "");				
			String updateQ = "{$push:{title:'"+title+"',url:'"+url+"',date:'"+date+"'}}";
			DbConnect.coll.updateOne((BasicDBObject)JSON.parse(findQ), (BasicDBObject)JSON.parse(updateQ));
		}
		else
		{
			dbconnect = new DbConnect("history");
			DbConnect.coll.insertOne(new Document("title", title).append("url", url).append("date", date));
		}
	}
}

class AddBookmarks implements Runnable
{
	private String title = "";
	private String url = "";
	static DbConnect dbconnect = null;
	
	AddBookmarks(String title, String url)
	{
		this.title = title;
		this.url = url;
		sendToDb();
	}

	@Override
	public void run() {
		sendToDb();
	}
	
	private void sendToDb()
	{
		if(LoginWindow.loggedin == true)
		{
			String findQ = "{name:'"+LoginWindow.nameStr+"', pass:'"+LoginWindow.passStr+"'}";
			title = title.replace("'", "");
			String updateQ = "{$push:{bookmark:'"+title+"',bookmarkUrl:'"+url+"'}}";
			DbConnect.coll.updateOne((BasicDBObject)JSON.parse(findQ), (BasicDBObject)JSON.parse(updateQ));
		}
		else
		{
			dbconnect = new DbConnect("bookmarks");
			DbConnect.coll.insertOne(new Document("_id", url).append("title", title));
		}
	}
}

// ------------------------------------------------History Stage-------------------

@SuppressWarnings("serial")
class HistoryStage extends JFrame
{
	JTable table;
	JButton jbutton;
	JPanel panel;
	DefaultTableModel model;
	
	public HistoryStage(WebEngine page) {
		
		model = new DefaultTableModel();
		model.addColumn("Title");
		model.addColumn("Url");
		model.addColumn("Visiting Date");
		table = new JTable(model);
		JLabel jlb = new JLabel("");
		jbutton = new JButton("Clear History");
		if(LoginWindow.loggedin == true)
		{
			String[] titleStr = {},urlStr = {},dateStr = {};
			Document doc = DbConnect.coll.find(new Document("name",LoginWindow.nameStr).append("pass", LoginWindow.passStr)).first();
			if(doc.get("title") != null)
			{
			titleStr = doc.get("title").toString().trim().replace("[", "").replace("]", "").split(",");
			urlStr = doc.get("url").toString().trim().replace("[", "").replace("]", "").split(",");
			dateStr = doc.get("date").toString().trim().replace("[", "").replace("]", "").split(",");
			}
			for(int i=0; i<titleStr.length; i++)
				model.addRow(new Object[]{titleStr[i], urlStr[i], dateStr[i]});
		}
		else
		{
			new DbConnect("history");
			FindIterable<Document> iter = DbConnect.coll.find();
			iter.forEach(new Block<Document>() {
				@Override
				public void apply(Document arg0) {
					if(arg0.get("title") != null)
						model.addRow(new Object[]{arg0.getString("title"), arg0.getString("url"), formatted(arg0.getDate("date"))});
				}
			});
		}
        
		
		jbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(LoginWindow.loggedin == true)
					DbConnect.coll.updateOne(new Document("name", LoginWindow.nameStr).append("pass", LoginWindow.passStr), new Document("$unset", new Document("title", "").append("url", "").append("date", "")));
				else
					DbConnect.coll.drop();
				jlb.setText("History has been cleared"); // ----- Jlabel to tell history cleared
			}
		});
		
		panel = new JPanel();
		
		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(jlb);
		panel.add(jbutton);
		panel.add(scrollpane);
        add(panel);
        setSize(550,350);
        setVisible(true);
	}

	@Deprecated
	public String formatted(Date date) {
		String day = "";
		String time = "";
		day = "";
		time = "";
		Calendar calendar = Calendar.getInstance();
		if(calendar.get(Calendar.DATE) == date.getDate())
		{	
			day = "Today ";
			time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
		}
		else if((calendar.get(Calendar.DATE) - date.getDate()) == 1)
		{
			day = "Yesterday ";
			time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
		}
		else if((calendar.get(Calendar.DATE) - date.getDate()) < 7)
		{
			day = "This Week ";
			time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
		}
		else
		{
			day = date.getYear()+ "/" + (date.getMonth() + 1) + "/" + date.getDate() + "  ";
			time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
		}
		return (day+time);
	}
}

@SuppressWarnings("serial")
class BookmarksStage extends JFrame
{
	JTable table;
	JButton jbutton;
	JPanel panel;
	DefaultTableModel model;
	
	public BookmarksStage(WebEngine page) {
		
		model = new DefaultTableModel();
		model.addColumn("Title");
		model.addColumn("Url");
		table = new JTable(model);
		JLabel jlb = new JLabel("");
		jbutton = new JButton("Clear Bookmarks");
		if(LoginWindow.loggedin == true)
		{
			String[] titleStr = {},urlStr = {};
			Document doc = DbConnect.coll.find(new Document("name",LoginWindow.nameStr).append("pass", LoginWindow.passStr)).first();
			if(doc.get("title") != null)
			{
			titleStr = doc.get("bookmark").toString().trim().replace("[", "").replace("]", "").split(",");
			urlStr = doc.get("bookmarkUrl").toString().trim().replace("[", "").replace("]", "").split(",");
			}
			for(int i=0; i<titleStr.length; i++)
				model.addRow(new Object[]{titleStr[i], urlStr[i]});
		}
		else
		{
			new DbConnect("bookmarks");
			FindIterable<Document> iter = DbConnect.coll.find();
			iter.forEach(new Block<Document>() {
				@Override
				public void apply(Document arg0) {
					if(arg0.getString("_id") != null)
						model.addRow(new Object[]{arg0.getString("title"), arg0.getString("_id")});
				}
			});
		}
        
		
		jbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(LoginWindow.loggedin == true)
					DbConnect.coll.updateOne(new Document("name", LoginWindow.nameStr).append("pass", LoginWindow.passStr), new Document("$unset", new Document("title", "").append("url", "").append("date", "")));
				else
					DbConnect.coll.drop();
				jlb.setText("Bookmarks have been cleared"); // ----- Jlabel to tell bookmarks cleared
			}
		});
		
		panel = new JPanel();
		
		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(jlb);
		panel.add(jbutton);
		panel.add(scrollpane);
        add(panel);
        setSize(550,350);
        setVisible(true);
	}
}