package us.corenetwork.tradecraft.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import us.corenetwork.tradecraft.IO;
import us.corenetwork.tradecraft.Logs;

public class RemoveVillagerTask implements IDbTask {

	private String UUID;
	
	public RemoveVillagerTask(String UUID)
	{
		this.UUID = UUID;
	}

	@Override
	public void perform() 
	{
		try
    	{
	    	PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM villagers WHERE ID = ?");
	        statement.setString(1, UUID);
            statement.execute();
            statement.close();
            
            statement = IO.getConnection().prepareStatement("DELETE FROM offers WHERE Villager = ?");
            statement.setString(1, UUID);
            statement.execute();
            statement.close();
            
            IO.getConnection().commit();
	    } catch (SQLException e) {
	        Logs.severe("Error while deleting villager from database ! " + UUID);
	        e.printStackTrace();
	    }
	}

}
