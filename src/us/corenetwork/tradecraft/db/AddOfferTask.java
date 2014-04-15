package us.corenetwork.tradecraft.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.craftbukkit.v1_7_R2.util.CraftMagicNumbers;

import us.corenetwork.tradecraft.IO;
import us.corenetwork.tradecraft.Logs;
import us.corenetwork.tradecraft.Util;

public class AddOfferTask implements IDbTask {

	private String UUID;
	private int tradeID;
	int firstID;
	private int firstDamage;
	private byte[] firstNBT;
	private int firstAmount;
	private int secondID;
	private int secondDamage;
	private byte[] secondNBT;
	private int secondAmount;
	private int thirdID;
	private int thirdDamage;
	private byte[] thirdNBT;
	private int thirdAmount;
	private int tier;
	private int tradesLeft;
	private int tradesPerformed;
	
	
	public AddOfferTask(String UUID, int tradeID, int firstID, int firstDamage,
			byte[] firstNBT, int firstAmount, int secondID, int secondDamage,
			byte[] secondNBT, int secondAmount, int thirdID, int thirdDamage,
			byte[] thirdNBT, int thirdAmount, int tier, int tradesLeft,
			int tradesPerformed) {
		super();
		this.UUID = UUID;
		this.tradeID = tradeID;
		this.firstID = firstID;
		this.firstDamage = firstDamage;
		this.firstNBT = firstNBT;
		this.firstAmount = firstAmount;
		this.secondID = secondID;
		this.secondDamage = secondDamage;
		this.secondNBT = secondNBT;
		this.secondAmount = secondAmount;
		this.thirdID = thirdID;
		this.thirdDamage = thirdDamage;
		this.thirdNBT = thirdNBT;
		this.thirdAmount = thirdAmount;
		this.tier = tier;
		this.tradesLeft = tradesLeft;
		this.tradesPerformed = tradesPerformed;
	}
	
	public AddOfferTask(String UUID, int tradeID, int firstID, int firstDamage,
			byte[] firstNBT, int firstAmount, int thirdID, int thirdDamage,
			byte[] thirdNBT, int thirdAmount, int tier, int tradesLeft,
			int tradesPerformed) {
		this(UUID, tradeID, firstID, firstDamage, firstNBT, firstAmount, 0,0,new byte[0],0,thirdID, thirdDamage, thirdNBT, thirdAmount, tier,tradesLeft, tradesPerformed);
	}
	
	
	@Override
	public void perform() 
	{
		try {
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO offers (Villager, ID, "
	                + "FirstItemID, FirstItemDamage, FirstItemNBT, FirstItemAmount, "
	                + "SecondItemID, SecondItemDamage, SecondItemNBT, SecondItemAmount, "
	                + "ThirdItemID, ThirdItemDamage, ThirdItemNBT, ThirdItemAmount, "
	                + "Tier, TradesLeft, TradesPerformed) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			
	        statement.setString(1, UUID);
	        statement.setInt(2, tradeID);
	
	        statement.setInt(3, firstID);
	        statement.setInt(4, firstDamage);
	        statement.setBytes(5, firstNBT);
	        statement.setInt(6, firstAmount);

            statement.setInt(7, secondID);
            statement.setInt(8, secondDamage);
            statement.setBytes(9, secondNBT);
            statement.setInt(10, secondAmount);
	       
	
	        statement.setInt(11, thirdID);
	        statement.setInt(12, thirdDamage);
	        statement.setBytes(13, thirdNBT);
	        statement.setInt(14, thirdAmount);
	
	        statement.setInt(15, tier);
	        statement.setInt(16, tradesLeft);
	        statement.setInt(17, tradesPerformed);
	        statement.execute();
	        statement.close();
			IO.getConnection().commit();
		} catch (SQLException e) {
			Logs.severe("Error while adding offer to database ! " + UUID + " " + tradeID);
		    e.printStackTrace();
		}
	}

}
