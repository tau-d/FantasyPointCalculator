import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Player {
	private static final String SEPARATOR = "\t";
	private static final NumberFormat DECIMAL_FORMATTER = new DecimalFormat("#0.00");
	
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
	int tenPlusKillsOrAssists;
	
	public Player(String team, String playerName, String position, int numGames, int kills, int deaths, int assists, int creepScore, int tenPlusKillsOrAssists) {
		this.team = team;
		this.playerName = playerName;
		this.position = position;
		this.numGames = numGames;
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
		this.creepScore = creepScore;
		this.tenPlusKillsOrAssists = tenPlusKillsOrAssists;
	}

	public double calcAvgPointsPerGame() {
		return (2 * kills - 0.5 * deaths + 1.5 * assists + 0.01 * creepScore + 2 * tenPlusKillsOrAssists) / numGames;
	}
	
	public String getPos() {
		return position;
	}
	
	public String myToString(String mySeparator) {
		return 	team + mySeparator + 
				playerName + mySeparator + 
				position + mySeparator +
				numGames + mySeparator + 
				kills + mySeparator +
				deaths + mySeparator +
				assists + mySeparator +
				creepScore + mySeparator + 
				tenPlusKillsOrAssists + mySeparator + 
				DECIMAL_FORMATTER.format(calcAvgPointsPerGame());
	}
	
	@Override
	public String toString() {
		return 	myToString(SEPARATOR);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((playerName == null) ? 0 : playerName.toLowerCase().hashCode());
		result = prime * result + ((position == null) ? 0 : position.toLowerCase().hashCode());
		result = prime * result + ((team == null) ? 0 : team.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (playerName == null) {
			if (other.playerName != null)
				return false;
		} else if (!playerName.equalsIgnoreCase(other.playerName))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equalsIgnoreCase(other.position))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equalsIgnoreCase(other.team))
			return false;
		return true;
	}
	
}
