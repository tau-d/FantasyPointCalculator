package FantasyPointCalculator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//TODO: feature suggestions: output stats by week?, team/player matchups?, more points than opponents?

public class Player {
	private static final String SEPARATOR = "\t";
	private static final NumberFormat DECIMAL_FORMATTER = new DecimalFormat("#0.00");
		
	public static final String[] COL_HEADERS = {
			"Team", "Player", "Position",
			"# Games","Avg Pnts/Game","stdev",
			"# Wins","Avg Pnts/Win","stdev",
			"# Losses","Avg Pnts/Loss","stdev",
			"Kills","Deaths","Assists","CS","10+ K/A" 
	};
	
	public static final String MID = "MID";
	public static final String ADC = "ADC";
	public static final String SUP = "SUP";
	public static final String TOP = "TOP";
	public static final String JGL = "JGL";
	
	private String team;
	private String playerName;
	private String position;
	private List<List<MatchStats>> weekList;
	private PlayerStatistics stats;
		
	public Player(String team, String playerName, String position) {
		this.team = team;
		this.playerName = playerName;
		this.position = position;
		
		weekList = new ArrayList<>();
		newWeek();
		
		stats = null;
	}
	
	public String getTeam() {
		return team;
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getPosition() {
		return position;
	}

	public List<List<MatchStats>> getWeekList() {
		return weekList;
	}

	private PlayerStatistics getStats() {
		if (stats == null) {
			stats = new PlayerStatistics(weekList);
		}
		return stats;
	}
	
	public int getTotalKills() {
		return getStats().totalKills;
	}

	public int getTotalDeaths() {
		return getStats().totalDeaths;
	}

	public int getTotalAssists() {
		return getStats().totalAssists;
	}

	public int getTotalCreepScore() {
		return getStats().totalCreepScore;
	}

	public int getTotalTenPlusKorA() {
		return getStats().totalTenPlusKorA;
	}

	public int getNumGames() {
		return getStats().numGames;
	}

	public double getAvgPntsPerGame() {
		return getStats().avgPntsPerGame;
	}

	public double getStdevPntsPerGame() {
		return getStats().stdevPntsPerGame;
	}

	public int getNumWins() {
		return getStats().numWins;
	}

	public double getAvgPntsPerLoss() {
		return getStats().avgPntsPerLoss;
	}

	public double getStdevPntsPerLoss() {
		return getStats().stdevPntsPerLoss;
	}

	public int getNumLosses() {
		return getStats().numLosses;
	}

	public double getAvgPntsPerWin() {
		return getStats().avgPntsPerWin;
	}

	public double getStdevPntsPerWin() {
		return getStats().stdevPntsPerWin;
	}

	public void addMatch(boolean win, String enemyTeam, int kills, int deaths, int assists, int creepScore) {
		stats = null; // new match makes previously calculated stats invalid
		if (weekList.isEmpty()) {
			System.err.println("Cannot add match before a week is added");
			return;
		}
		getLatestWeek().add(new MatchStats(win, enemyTeam, kills, deaths, assists, creepScore));
	}
	
	private List<MatchStats> getLatestWeek() {
		if (weekList.isEmpty()) {
			System.err.println("No week exists");
			return null;
		}
		return weekList.get(weekList.size() - 1);
	}
	
	public void newWeek() {
		List<MatchStats> newWeek = new ArrayList<>();
		weekList.add(newWeek);
	}
	
	public String myToString(String mySeparator) {
		if (stats == null) {
			stats = new PlayerStatistics(weekList);
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(team + mySeparator);
		sb.append(playerName + mySeparator);
		sb.append(position + mySeparator);
				
		sb.append(getStats().numGames + mySeparator);
		sb.append(format(getStats().avgPntsPerGame) + mySeparator);
		sb.append(format(getStats().stdevPntsPerGame) + mySeparator);
		sb.append(getStats().numWins + mySeparator);
		sb.append(format(getStats().avgPntsPerWin) + mySeparator);
		sb.append(format(getStats().stdevPntsPerWin) + mySeparator);
		sb.append(getStats().numLosses + mySeparator);
		sb.append(format(getStats().avgPntsPerLoss) + mySeparator);
		sb.append(format(getStats().stdevPntsPerLoss) + mySeparator);
				
		sb.append(getStats().totalKills + mySeparator);
		sb.append(getStats().totalDeaths + mySeparator);
		sb.append(getStats().totalAssists + mySeparator);
		sb.append(getStats().totalCreepScore + mySeparator);
		sb.append(getStats().totalTenPlusKorA);
		
		return sb.toString();
	}
	
	private String format(double d) {
		return DECIMAL_FORMATTER.format(d);
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
