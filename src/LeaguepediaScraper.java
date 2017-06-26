import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class LeaguepediaScraper {
	private static final String[] ORDERED_POSITIONS = {Player.TOP, Player.JGL, Player.MID, Player.ADC, Player.SUP};
	private static final String[] SCOREBOARD_COLUMN_HEADERS = {"Player", "K", "D", "A", "CS"};
	// private static final String[] PLAYER_STATS_COLUMN_HEADERS = {"T", "Player", "P", "G", "K", "D", "A", "CS"};
	// private static final String EMPTY_TEAM = "Unknown";
	private static final NumberFormat NUM_FORMATTER = NumberFormat.getInstance(Locale.US);


	// METHODS
	
	public static Collection<Player> getPlayerStatsFromBaseScoreboardUrls(List<String> baseScoreboardUrls) {
		List<Document> docs = parseBaseScoreboardUrls(baseScoreboardUrls);
		Map<Player, Player> players = new HashMap<>();
		for (Document doc : docs) {
			parseOneWeekScoreboard(doc, players);
		}
		return players.values();
	}
	
	private static class docGetter implements Runnable {
		// TODO: progress bar UI
		
		private List<Document> docs;
		private String url;

		public docGetter(List<Document> docs, String url) {
			this.docs = docs;
			this.url = url;
		}

		@Override
		public void run() {
			Document doc = getDoc(url);
			if (doc != null) docs.add(doc);
		}
	}
	
	private static Document getDoc(String url) {
		Document doc = null;
		try {
			System.out.println(url + " STARTING");
			doc = Jsoup.connect(url).timeout(60 * 1000).get();
			System.out.println(url + " COMPLETE");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/*
	 * Get Documents for all weeks and return them
	 */
	private static List<Document> parseBaseScoreboardUrls(List<String> baseScoreboardUrls) {
		System.out.println("PARSING ALL WEEKS");
		
		final List<Document> weekScoreboardDocs = new ArrayList<>();
		
		ExecutorService es = Executors.newFixedThreadPool(baseScoreboardUrls.size());
		for (final String base : baseScoreboardUrls) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					final Document week1 = getDoc(base + "/Week_1");
					weekScoreboardDocs.add(week1);
					
					final int numWeeks = countWeeks(week1);
					ExecutorService execServ = Executors.newFixedThreadPool(numWeeks);
					for (int i = 2; i <= numWeeks; ++i) {
						execServ.execute(new docGetter(weekScoreboardDocs, base + "/Week_" + i));
					}
					execServ.shutdown();
					
					try {
						execServ.awaitTermination(60, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		es.shutdown();
		
		try {
			es.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("ALL WEEKS PARSED");
		return weekScoreboardDocs;
	}
	
	private static int countWeeks(Document doc) {
		int maxWeek = 1;
		for (Element e : doc.getElementsByTag("a")) {
			String link = e.attr("href");
			maxWeek = Math.max(maxWeek, getWeekNumFromHref(link));
		}
		
		return maxWeek;
	}
	
	private static int getWeekNumFromHref(String href) {
		final String prefix = "/Week_";
		int week_start = href.lastIndexOf(prefix);
		
		if (week_start == -1) return -1; // prefix does not exist
		
		final int numStart = week_start + prefix.length();
		int numEnd = numStart;
		while (numEnd < href.length() && Character.isDigit(href.charAt(numEnd))) {
			++numEnd;
		}
		
		try {
			int numWeek = Integer.parseInt(href.substring(numStart, numEnd));
			return numWeek;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/*
	 * Parse all the match tables in document and save the match stats of each player in storedPlayers
	 */
	private static void parseOneWeekScoreboard(Document doc, Map<Player, Player> storedPlayers) {
		if (doc == null) {
			System.err.println("Document is null");
			return;
		}
		
		for (Player p : storedPlayers.values()) {
			p.newWeek();
		}

		Elements gameTables = doc.getElementsByClass("match-recap");
		if (gameTables.isEmpty()) {
			System.out.println(doc.location() + " NO GAME TABLES");
			return;
		}
		
		Map<String, Integer> teamToWins = new HashMap<>();
		
		for (Element game : gameTables) {
			Element tbody = game.child(game.children().size() - 1);

			Element teams = tbody.child(1);
			String team1 = teams.child(0).ownText();
			String team2 = teams.child(3).ownText();
			
			//System.out.println(teams.text());
			
			Elements tables = tbody.getElementsByTag("table");
			if (tables.isEmpty() || tables.get(tables.size() - 1).children().isEmpty()) {
				System.out.println(doc.location() + " " + team1 + " vs " + team2 + " NO SCOREBOARD TABLE");
				continue;
			}

			Element scoreboardTbody = tables.get(tables.size() - 1).child(0);
			if (scoreboardTbody.children().size() < 12) {
				System.out.println(doc.location() + " SKIPPING UNPLAYED GAME: " + team1 + " vs " + team2);
				continue;
			}

			int team1Score = Integer.parseInt(teams.child(1).ownText());
			int team2Score = Integer.parseInt(teams.child(2).ownText());
			
			if ((team1Score == 1 && team2Score == 0) || (team2Score == 1 && team1Score == 0) ) { // Must be new set, reset score to 0-0
				teamToWins.put(team1, 0);
				teamToWins.put(team2, 0);
			}
			
			int prevTeam1Score = teamToWins.get(team1);
			int prevTeam2Score = teamToWins.get(team2);
			
			teamToWins.put(team1, team1Score);
			teamToWins.put(team2, team2Score);
			
			String winner = null;
			if (prevTeam1Score < team1Score) {
				winner = team1;
			} else if (prevTeam2Score < team2Score) {
				winner = team2;
			} else {
				System.err.println(doc.location() + " " + team1 + " vs " + team2 + " THERE IS NO WINNER?");
				continue;
			}
			
			Element columnHeaders = scoreboardTbody.child(0);
			Map<String, Integer> colTitleToColNum = new HashMap<>();
			int colCount = 0;
			for (Element col : columnHeaders.children()) {
				String colspan = col.attr("colspan");
				if (colspan.isEmpty()) {
					colTitleToColNum.put(col.text(), colCount);
					++colCount;
				} else {
					colTitleToColNum.put(col.text(), colCount);
					colCount += Integer.parseInt(colspan);
				}				
			}

			parseFiveScoreboardRows(scoreboardTbody, 1, team1, team2, winner, colTitleToColNum, storedPlayers); // Team 1
			parseFiveScoreboardRows(scoreboardTbody, 7, team2, team1, winner, colTitleToColNum, storedPlayers); // Team 2
		}
	}

	/*
	 * Parse 5 rows of the scoreboard which corresponds to the 5 players on one team
	 */
	private static void parseFiveScoreboardRows(Element scoreboardTbody, int startIndex, String teamName, String enemyTeam, String winningTeam, Map<String, Integer> colTitleToColNum, Map<Player, Player> players) {
		for (int i = startIndex; i < startIndex + 5; ++i) {
			Element playerRow = scoreboardTbody.child(i);

			//TODO: get player name from link if possible to deal with inconsistent/misspelled names? (ex. Cody Sun = Cody, Steeelback = Steelback in Spring 2017 split)
			String playerName = playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[0])).child(0).ownText();
			String position = ORDERED_POSITIONS[i - startIndex];
			int kills, deaths, assists, creepScore;

			try {
				// TODO: no way to determine 3/4/5k from scoreboards currently
				kills = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[1])).ownText()).intValue();
				deaths = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[2])).ownText()).intValue();
				assists = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[3])).ownText()).intValue();
				creepScore = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[4])).ownText()).intValue();
			} catch (ParseException e) {
				e.printStackTrace();
				System.err.println(scoreboardTbody.ownerDocument().location() + " MISSING STATS, SKIPPING: " + teamName + " " + playerName);
				continue;
			}

			Player p = new Player(teamName, playerName, position);

			Player existing = players.get(p);
			if (existing != null) {
				existing.addMatch(teamName.equalsIgnoreCase(winningTeam), enemyTeam, kills, deaths, assists, creepScore);
			} else {
				players.put(p, p);
				p.newWeek();
				p.addMatch(teamName.equalsIgnoreCase(winningTeam), enemyTeam, kills, deaths, assists, creepScore);
			}
		}
	}
	
	// MAIN FOR TESTING
	public static void main(String[] args) {
		System.out.println(LeaguepediaScraper.class + ": MAIN START");
		
		List<String> currSplitUrls = new ArrayList<>();
		currSplitUrls.add("http://lol.gamepedia.com/2017_NA_LCS/Summer_Split/Scoreboards");
		currSplitUrls.add("http://lol.gamepedia.com/2017_EU_LCS/Summer_Split/Scoreboards");
		parseBaseScoreboardUrls(currSplitUrls);		
		
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

		System.out.println(LeaguepediaScraper.class + ": MAIN END");
	}
}
