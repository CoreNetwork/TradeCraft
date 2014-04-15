package us.corenetwork.tradecraft.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import us.corenetwork.tradecraft.IO;
import us.corenetwork.tradecraft.Logs;

public class AddVillagerTask implements IDbTask {

	private String UUID;
	private String career;
	
	public AddVillagerTask(String UUID, String career)
	{
		this.UUID = UUID;
		this.career = career;
	}
	
	@Override
	public void perform() 
	{
		try
    	{
	    	PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO villagers (ID, Career) VALUES (?,?)");
	        statement.setString(1, UUID);
	        statement.setString(2, career);
            statement.execute();
            statement.close();
            IO.getConnection().commit();
	    } catch (SQLException e) {
	        Logs.severe("Error while saving village to database ! " + career + "  " + UUID);
	        e.printStackTrace();
	    }
	}

}
