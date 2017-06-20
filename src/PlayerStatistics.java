import java.util.List;

public class PlayerStatistics {
	int totalKills;
	int totalDeaths;
	int totalAssists;
	int totalCreepScore;
	int totalTenPlusKorA;
	
	int numGames;
	double avgPntsPerGame;
	double stdevPntsPerGame;
	
	int numWins;
	double avgPntsPerLoss;
	double stdevPntsPerLoss;
	
	int numLosses;
	double avgPntsPerWin;
	double stdevPntsPerWin;
	
	public PlayerStatistics(List<List<MatchStats>> weekList) {
		totalKills = 0;
		totalDeaths = 0;
		totalAssists = 0;
		totalCreepScore = 0;
		totalTenPlusKorA = 0;
		
		numGames = 0;
		numWins = 0;
		numLosses = 0;
		
		calcTotalsAndAvgs(weekList);
		calcStdevs(weekList);
	}
	
	private void calcTotalsAndAvgs(List<List<MatchStats>> weekList) {
		double winPoints = 0, lossPoints = 0;
		
		for (List<MatchStats> week : weekList) {
			for (MatchStats match : week) {
				totalKills += match.kills;
				totalDeaths += match.deaths;
				totalAssists += match.assists;
				totalCreepScore += match.creepScore;
				if (match.tenPlusKOrA()) ++totalTenPlusKorA;
				++numGames;
				
				if (match.win) { // win
					winPoints += match.fantasyPoints;
					++numWins;
				} else { // loss
					lossPoints += match.fantasyPoints;
					++numLosses;
				}
			}
		}
		
		avgPntsPerGame = (winPoints + lossPoints) / numGames;
		avgPntsPerLoss = lossPoints / numLosses;
		avgPntsPerWin = winPoints / numWins;
	}
	
	private void calcStdevs(List<List<MatchStats>> weekList) {
		double allDiffs = 0, winDiffs = 0, lossDiffs = 0;
		
		for (List<MatchStats> week : weekList) {
			for (MatchStats match : week) {
				allDiffs += Math.pow(match.fantasyPoints - avgPntsPerGame, 2);
				
				if (match.win) { // win
					winDiffs += Math.pow(match.fantasyPoints - avgPntsPerWin, 2);
				} else { // loss
					lossDiffs += Math.pow(match.fantasyPoints - avgPntsPerLoss, 2);
				}
			}
		}
		
		stdevPntsPerGame = Math.sqrt(allDiffs / numGames);
		stdevPntsPerLoss = Math.sqrt(lossDiffs / numLosses);;
		stdevPntsPerWin = Math.sqrt(winDiffs / numWins);;
	}
}