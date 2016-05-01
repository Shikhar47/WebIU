package application;

import javafx.scene.paint.Color;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class DbConnect
{
	static MongoCollection<Document> coll;
	FindIterable<Document> findDoc;
	static MongoClient mongoClient;
	static MongoDatabase mongodb;
	long numDoc = 0;
	
	DbConnect()
	{
		mongoClient = new MongoClient("localhost", 27017);
			System.out.println("Connected to server");
		mongodb = mongoClient.getDatabase("project");
			System.out.println("connected to database");
	}
	
	DbConnect(String collName)
	{
		try{
		mongodb.createCollection(collName);
		}catch(MongoCommandException e)
		{
			System.out.println("Collection already exists");
		}finally{
			// ---- get collection
			coll = mongodb.getCollection(collName);
		}
	}
	
	public void insert(String name,String pass)
	{
		if(coll.count(new Document("name", name).append("pass", pass)) < 1)
		{
			coll.insertOne(new Document("name", name).append("pass", pass));
			LoginWindow.loggedin = true;
			LoginWindow.message.setFill(Color.MEDIUMSEAGREEN);
			LoginWindow.message.setText("You have signed up");
		}
		else
		{
			LoginWindow.message.setFill(Color.RED);
			LoginWindow.message.setText("Username : " + name +" already exists");
		}
	}
	
	public Document find(String name, String pass)
	{
		numDoc = coll.count(new Document("name", name).append("pass", pass));
		if(numDoc == 0)
		{
			LoginWindow.message.setFill(Color.RED);
			LoginWindow.message.setText("Username : " + name + " not found. Please Sign up first.");
		}
		else
		{
			if(LoginWindow.loggedin == true)
			{
				LoginWindow.message.setFill(Color.RED);
				LoginWindow.message.setText(name + ", you are already logged in!!");
			}
			else
			{
				LoginWindow.message.setText(name + ", you are now logged in");
				LoginWindow.loggedin = true;
				LoginWindow.message.setFill(Color.MEDIUMSEAGREEN);
			}
		}
		
		//-- find document with 'name' = name
		FindIterable<Document> iter =  coll.find(new Document("name", name).append("pass", pass));
		
		
		
		return iter.first();
	}
	
	public void closeConnecton()
	{
		mongoClient.close();
	}
}