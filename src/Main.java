import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
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


public class Main {
	// TODO: get player stats by parsing scoreboards for older seasons/seasons without player stats
	// TODO: UI to let user pick a split and regions from a list
	// TODO: let user choose whether to split individual players by team/position
	// TODO: determine number of weeks
	
	private static final String[] ORDERED_POSITIONS = {Player.TOP, Player.JGL, Player.MID, Player.ADC, Player.SUP};
	private static final String[] SCOREBOARD_COLUMN_HEADERS = {"Player", "K", "D", "A", "CS"};
	// private static final String[] PLAYER_STATS_COLUMN_HEADERS = {"T", "Player", "P", "G", "K", "D", "A", "CS"};
	private static final NumberFormat NUM_FORMATTER = NumberFormat.getInstance(Locale.US);
	private static final String EMPTY_TEAM = "Unknown";
	
	private static class docGetter implements Runnable {

		private List<Document> docs;
		private String url;
		
		public docGetter(List<Document> docs, String url) {
			this.docs = docs;
			this.url = url;
		}
		
		@Override
		public void run() {
			Document doc = null;
			try {
				System.out.println(url + " STARTING");
				doc = Jsoup.connect(url).timeout(60*1000).get();
				System.out.println(url + " COMPLETE");
			} catch (Exception e) {
				e.printStackTrace();
			}
			docs.add(doc);
		}
		
	}
	
	// METHODS
	
	private static void parseAllSplitScoreboards(String url) {
		Map<Player, Player> storedPlayers = new HashMap<>();
		List<Document> docs = Collections.synchronizedList(new ArrayList<>(9));
		
		final int NUM_WEEKS = 9;
		ExecutorService es = Executors.newFixedThreadPool(NUM_WEEKS);
		for (int i = 1; i <= NUM_WEEKS; ++i) {
			es.execute(new docGetter(docs, url + "/Week_" + i));
		}
		es.shutdown();
		
		try {
			es.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("All docs ready!");
		
		for (Document d : docs) {
			parseOneWeekScoreboard(d, storedPlayers);
		}
		
		List<Player> all_players = new ArrayList<>(storedPlayers.values());
		saveStats("scoreboard_data\\", all_players);
	}
	
	private static void parseOneWeekScoreboard(Document doc, Map<Player, Player> storedPlayers) {
		if (doc == null) {
			System.err.println("Document is null");
			return;
		}
		
		Elements gameTables = doc.getElementsByClass("match-recap");
		if (gameTables.isEmpty()) {
			System.out.println(doc.location() + " NO GAME TABLES");
			return;
		}
		
		for (Element game : gameTables) {
			Element tbody = game.child(game.children().size() - 1);

			Element teams = tbody.child(1);
			String team1 = teams.child(0).ownText();
			String team2 = teams.child(3).ownText();
			
			// System.out.println(team1 + " vs " + team2);
									
			Elements tables = tbody.getElementsByTag("table");
			if (tables.isEmpty() || tables.get(tables.size() - 1).children().isEmpty()) {
				System.out.println(doc.location() + " " + team1 + " vs " + team2 + " NO SCOREBOARD TABLE");
			}
			
			Element scoreboardTbody = tables.get(tables.size() - 1).child(0);
			if (scoreboardTbody.children().size() < 12) {
				System.out.println(doc.location() + " SKIPPING UNPLAYED GAME: " + team1 + " vs " + team2);
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
			
			// Team 1
			parseFiveScoreboardRows(scoreboardTbody, 1, team1, colTitleToColNum, storedPlayers);
			
			// Team 2
			parseFiveScoreboardRows(scoreboardTbody, 7, team2, colTitleToColNum, storedPlayers);
		}
	}
	
	private static void parseFiveScoreboardRows(Element scoreboardTbody, int startIndex, String teamName, Map<String, Integer> colTitleToColNum, Map<Player, Player> players) {
		for (int i = startIndex; i < startIndex + 5; ++i) {
			Element playerRow = scoreboardTbody.child(i);
			
			String playerName = playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[0])).child(0).ownText();
			String position = ORDERED_POSITIONS[i - startIndex];
			int kills, deaths, assists, creepScore;
			
			try {
				kills = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[1])).ownText()).intValue();
				deaths = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[2])).ownText()).intValue();
				assists = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[3])).ownText()).intValue();
				creepScore = NUM_FORMATTER.parse(playerRow.child(colTitleToColNum.get(SCOREBOARD_COLUMN_HEADERS[4])).ownText()).intValue();
			} catch (ParseException e) {
				e.printStackTrace();
				System.err.println(scoreboardTbody.ownerDocument().location() + " MISSING STATS, SKIPPING: " + teamName + " " + playerName);
				continue;
			}
			
			int tenPlusKillsOrAssists = (kills >= 10 || assists >= 10) ? 1 : 0;
			
			// TODO: no way to determine 3/4/5k from scoreboards currently
			Player p = new Player(teamName, playerName, position, 1, kills, deaths, assists, creepScore, tenPlusKillsOrAssists);
			
			Player existing = players.get(p);
			if (existing != null) {
				// Add new stats to player object already in the map
				combinePlayerStats(existing, p);
			} else {
				players.put(p, p);
			}
		}
	}
	
	private static void combinePlayerStats(Player main, Player secondary) {
		main.numGames += secondary.numGames;
		main.kills += secondary.kills;
		main.deaths += secondary.deaths;
		main.assists += secondary.assists;
		main.creepScore += secondary.creepScore;
		main.tenPlusKillsOrAssists += secondary.tenPlusKillsOrAssists;
	}
	
	// Obsolete method?
	private static void parsePlayerStatsAndAddPlayers(String url, List<Player> players) {
		Document doc = null;
		try {
			System.out.println(url + " STARTING");
			doc = Jsoup.connect(url).timeout(60*1000).get();
			
			Elements links = doc.getElementsByClass("wikitable");
			Element table = links.get(links.size() - 1);
			Element tbody = table.child(0);
			Elements rows = tbody.getElementsByTag("tr");
			
			for (int i = 1; i < rows.size(); ++i) {
				Element row = rows.get(i);
				
				// for players who switched teams mid-season				
				String team = "";
				for (Element e : row.child(0).children()) {
					team += e.attr("title") + "/";
				}
				team = team.substring(0, team.length() - 1);
								
				String playerName = row.child(1).ownText();
				String position = row.child(2).ownText();
				int numGames = NUM_FORMATTER.parse(row.child(3).ownText()).intValue();
				int kills = NUM_FORMATTER.parse(row.child(7).ownText()).intValue();
				int deaths = NUM_FORMATTER.parse(row.child(8).ownText()).intValue();
				int assists = NUM_FORMATTER.parse(row.child(9).ownText()).intValue();
				int creepScore = NUM_FORMATTER.parse(row.child(11).ownText()).intValue();
				
				// TODO: no way to determine 10+ kills/assists or 3/4/5k from player stats currently
				Player p = new Player(team, playerName, position, numGames, kills, deaths, assists, creepScore, 0);
				players.add(p);
			}
			System.out.println(url + " COMPLETE");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writePlayersToCsvFile(String path, String filename, List<Player> players) {
		File file = new File(path);
		file.mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path + filename + ".csv"))) {
			bw.write("Team,Player,Position,Games,Kills,Deaths,Assists,CS,10+ K/A,Avg Points/Game\n");
			for (Player p : players) {
				bw.write(p.myToString(",") + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private static void calcAndSavePointsFromPlayerStats() {
		List<Player> all_players = new ArrayList<>();
		
		// North America
		parsePlayerStatsAndAddPlayers("http://lol.gamepedia.com/2017_NA_LCS/Spring_Split/Player_Statistics", all_players);
		
		// Europe
		//parsePlayerStatsAndAddPlayers("http://lol.gamepedia.com/2017_EU_LCS/Spring_Split/Players", all_players);
		
		// Korea
		///parseUrlAndAddPlayers("http://lol.gamepedia.com/2017_LCK/Spring_Split/Players", all_players);
		
		// Old format test
		//parsePlayerStatsAndAddPlayers("http://lol.gamepedia.com/2015_NA_LCS_Spring/Statistics", all_players);
		
		saveStats("player_stats_data\\", all_players);
	}
	
	private static void saveStats(String dirPath, List<Player> all_players) {
		Collections.sort(all_players, (o1, o2) -> (o1.calcAvgPointsPerGame() > o2.calcAvgPointsPerGame()) ? -1 : 1);
		
		List<Player> mids = new ArrayList<>();
		List<Player> adcs = new ArrayList<>();
		List<Player> supports = new ArrayList<>();
		List<Player> tops = new ArrayList<>();
		List<Player> junglers = new ArrayList<>();
		
		// add to position lists after sort so they are already sorted
		for (Player p : all_players) {
			String pos = p.getPos();
			if (pos.equals(Player.MID)) {
				mids.add(p);
			} else if (pos.equals(Player.ADC)) {
				adcs.add(p);
			} else if (pos.equals(Player.SUP)) {
				supports.add(p);
			} else if (pos.equals(Player.TOP)) {
				tops.add(p);
			} else if (pos.equals(Player.JGL)) {
				junglers.add(p);
			}
		}			
		
		writePlayersToCsvFile(dirPath, "all_players", all_players);
		writePlayersToCsvFile(dirPath, "mid", mids);
		writePlayersToCsvFile(dirPath, "adc", adcs);
		writePlayersToCsvFile(dirPath, "sup", supports);
		writePlayersToCsvFile(dirPath, "top", tops);
		writePlayersToCsvFile(dirPath, "jgl", junglers);
	}
	
	public static void main(String[] args) {
		//calcAndSavePointsFromPlayerStats();
		
		// Last NA/EU splits
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2017_NA_LCS/Spring_Split/Scoreboards");
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2017_EU_LCS/Spring_Split/Scoreboards");
		
		// Current incomplete split
		parseAllSplitScoreboards("http://lol.gamepedia.com/2017_NA_LCS/Summer_Split/Scoreboards"); 
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2017_EU_LCS/Summer_Split/Scoreboards");
		
		// Older splits
		//parseAllSplitScoreboards("http://lol.gamepedia.com/Riot_League_Championship_Series/North_America/Season_3/Scoreboards/Summer_Season"); // oldest season on site
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2016_NA_LCS/Spring_Split/Scoreboards"); // Week 2 | NRG vs Echo Fox: game table with no stats, I assume the game was forfeited so they didnt play?
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2015_NA_LCS_Spring/Scoreboards/Round_Robin"); // Week 2 | Team Impulse vs Team 8: Impact, Rush, and XiaoWeiXiao have empty creepscores, skip only those players
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2015_EU_LCS_Spring/Scoreboards/Round_Robin");
		//parseAllSplitScoreboards("http://lol.gamepedia.com/CBLoL_2016/Stage_1/Scoreboards"); // BR League only 6 weeks
		//parseAllSplitScoreboards("http://lol.gamepedia.com/Riot_League_Championship_Series/North_America/2014_Season/Summer_Round_Robin/Scoreboards/Summer_Season"); // 11 weeks
		
		System.out.println("PROGRAM COMPLETE");
	}

}