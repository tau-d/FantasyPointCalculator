
public class MatchStats {

	boolean win;
	String enemyTeam;
	int kills;
	int deaths;
	int assists;
	int creepScore;
	double fantasyPoints;
		
	public MatchStats(boolean win, String enemyTeam, int kills, int deaths, int assists, int creepScore) {
		this.win = win;
		this.enemyTeam = enemyTeam;
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
		this.creepScore = creepScore;
		this.fantasyPoints = calcFantasyPoints();
	}
	
	private double calcFantasyPoints() {
		return (2 * kills) - (0.5 * deaths) + (1.5 * assists) + (0.01 * creepScore) + (tenPlusKOrA() ? 2 : 0);
	}
	
	public boolean tenPlusKOrA() {
		return kills >= 10 || assists >= 10;
	}
}
