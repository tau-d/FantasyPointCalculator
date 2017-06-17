public class Main {
		
	public static void main(String[] args) {
		System.out.println("MAIN START");
		//calcAndSavePointsFromPlayerStats();
		
		// Current incomplete split
		/*List<String> currSplitUrls = new ArrayList<>();
		currSplitUrls.add("http://lol.gamepedia.com/2017_NA_LCS/Summer_Split/Scoreboards");
		currSplitUrls.add("http://lol.gamepedia.com/2017_EU_LCS/Summer_Split/Scoreboards");
		parseUrls(currSplitUrls);*/
		
		// Last NA/EU splits
		/*List<String> prevSplitUrls = new ArrayList<>();
		prevSplitUrls.add("http://lol.gamepedia.com/2017_NA_LCS/Spring_Split/Scoreboards");
		prevSplitUrls.add("http://lol.gamepedia.com/2017_EU_LCS/Spring_Split/Scoreboards");
		parseUrls(prevSplitUrls);*/
		
		// Older splits
		//parseAllSplitScoreboards("http://lol.gamepedia.com/Riot_League_Championship_Series/North_America/Season_3/Scoreboards/Summer_Season"); // oldest season on site
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2016_NA_LCS/Spring_Split/Scoreboards"); // Week 2 | NRG vs Echo Fox: game table with no stats, I assume the game was forfeited so they didnt play?
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2015_NA_LCS_Spring/Scoreboards/Round_Robin"); // Week 2 | Team Impulse vs Team 8: Impact, Rush, and XiaoWeiXiao have empty creepscores, skip only those players
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2015_EU_LCS_Spring/Scoreboards/Round_Robin");
		//parseAllSplitScoreboards("http://lol.gamepedia.com/CBLoL_2016/Stage_1/Scoreboards"); // BR League only 6 weeks
		//parseAllSplitScoreboards("http://lol.gamepedia.com/Riot_League_Championship_Series/North_America/2014_Season/Summer_Round_Robin/Scoreboards/Summer_Season"); // 11 weeks
		
		UrlInputDialog.makeDialog();
		
		System.out.println("MAIN END");
	}

}