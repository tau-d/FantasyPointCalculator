package FantasyPointCalculator;

import java.util.List;

public class PlayerStatistics {
	protected int totalKills;
	protected int totalDeaths;
	protected int totalAssists;
	protected int totalCreepScore;
	protected int totalTenPlusKorA;
	
	protected int numGames;
	protected double avgPntsPerGame;
	protected double stdevPntsPerGame;
	
	protected int numWins;
	protected double avgPntsPerLoss;
	protected double stdevPntsPerLoss;
	
	protected int numLosses;
	protected double avgPntsPerWin;
	protected double stdevPntsPerWin;
	
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