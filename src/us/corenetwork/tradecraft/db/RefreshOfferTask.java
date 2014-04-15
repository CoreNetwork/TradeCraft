package us.corenetwork.tradecraft.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import us.corenetwork.tradecraft.IO;
import us.corenetwork.tradecraft.Logs;

public class RefreshOfferTask implements IDbTask {

	private String UUID;
	private int tradeID;
	private int tradesLeft;
	private int tradesPerformed;
	
	public RefreshOfferTask(String UUID, int tradeID, int tradesLeft, int tradesPerformed)
	{
		this.UUID = UUID;
		this.tradeID = tradeID;
		this.tradesLeft = tradesLeft;
		this.tradesPerformed = tradesPerformed;
	}
	
	@Override
	public void perform() 
	{
		try {
			PreparedStatement statement = IO.getConnection().prepareStatement(
							"UPDATE offers SET TradesLeft = ?, TradesPerformed = ? WHERE Villager = ? AND ID = ?");
			
			statement.setInt(1, tradesLeft);
			statement.setInt(2, tradesPerformed);
			statement.setString(3, UUID);
			statement.setInt(4, tradeID);

			statement.execute();
			statement.close();
			IO.getConnection().commit();
		} catch (SQLException e) {
			Logs.severe("Error while refreshing offer in database ! " + UUID + " " + tradeID);
		    e.printStackTrace();
		}
	}

}
