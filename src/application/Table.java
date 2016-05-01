package application;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
 
public class Table extends JFrame
{
	MongoCollection<Document> coll;
    public Table()
    {
    	MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase mongodb =  mongoClient.getDatabase("project");
		try{
		mongodb.createCollection("users");
		}catch(MongoCommandException e)
		{
			System.out.println("Collection already exists");
		}finally{
			coll = mongodb.getCollection("users");
		}
    	String[] colNames = {"Title", "URL", "Date Visited"};
        
    	Document doc = coll.find(new Document("name",LoginWindow.nameStr).append("pass", LoginWindow.passStr)).first();			

		String[] titleStr = doc.get("title").toString().trim().replace("[", "").replace("]", "").split(",");
		String[] urlStr = doc.get("url").toString().trim().replace("[", "").replace("]", "").split(",");
		String[] dateStr = doc.get("date").toString().trim().replace("[", "").replace("]", "").split(",");
		
		String rowValues[][] = {titleStr, urlStr, dateStr};
    	
        //actual data for the table in a 2d array
        Object[][] data = new Object[][] {
            {1, "John", 40.0, false },
            {2, "Rambo", 70.0, false },
            {3, "Zorro", 60.0, true },
        };
 
        //create table with data
        JTable table = new JTable(rowValues, colNames);
         
        //add the table to the frame
        this.add(new JScrollPane(table));
         
        this.setTitle("Table Example");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
        this.pack();
        this.setVisible(true);
    }
     
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Table();
            }
        });
    }   
}