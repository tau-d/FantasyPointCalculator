import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Player {
	private static final String SEPARATOR = "\t";
	private static final NumberFormat FORMATTER = new DecimalFormat("#0.00");
	
	public static final String MID = "MID";
	public static final String ADC = "ADC";
	public static final String SUP = "SUP";
	public static final String TOP = "TOP";
	public static final String JGL = "JGL";
	
	String team;
	String playerName;
	String position;
	int numGames;
	int kills;
	int deaths;
	int assists;
	int creepScore;
	double avgPointsPerGame;
	
	public Player(String team, String playerName, String position, int numGames, int kills, int deaths, int assists, int creepScore) {
		this.team = team;
		this.playerName = playerName;
		this.position = position;
		this.numGames = numGames;
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
		this.creepScore = creepScore;
		avgPointsPerGame = (2 * kills - 0.5 * deaths + 1.5 * assists + 0.01 * creepScore) / numGames;
	}

	public double getAPPG() {
		return avgPointsPerGame;
	}
	
	public String getPos() {
		return position;
	}
	
	@Override
	public String toString() {
		return 	team + SEPARATOR + 
				playerName + SEPARATOR + 
				position + SEPARATOR +
				numGames + SEPARATOR + 
				kills + SEPARATOR +
				deaths + SEPARATOR +
				assists + SEPARATOR +
				creepScore + SEPARATOR +  
				FORMATTER.format(avgPointsPerGame);
	}
	
	public String toString(String mySeparator) {
		return 	team + mySeparator + 
				playerName + mySeparator + 
				position + mySeparator +
				numGames + mySeparator + 
				kills + mySeparator +
				deaths + mySeparator +
				assists + mySeparator +
				creepScore + mySeparator +  
				FORMATTER.format(avgPointsPerGame);
	}
}
