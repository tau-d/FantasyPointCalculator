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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {
	// TODO: get player stats by parsing scoreboards for older seasons/seasons without player stats
	// TODO: take url for the split (http://lol.gamepedia.com/2017_NA_LCS/Spring_Split), use player stats if the page exists, scoreboards otherwise
	// TODO: parse column headers and use to cover any stat ordering
	
	private static final String[] ORDERED_POSITIONS = {Player.TOP, Player.JGL, Player.MID, Player.ADC, Player.SUP};
	private static final NumberFormat NUM_FORMATTER = NumberFormat.getInstance(Locale.US);
	
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
		List<Thread> threads = new ArrayList<>();
				
		for (int i = 1; i <= 9; ++i) {
			Thread t = new Thread(new docGetter(docs, url + "/Week_" + i));
			t.start();
			threads.add(t);			
		}
		
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All docs ready!");
		
		for (Document d : docs) {
			parseOneWeekScoreboard(d, storedPlayers);
		}
		
		List<Player> all_players = new ArrayList<>(storedPlayers.values());
		saveStats("scoreboard_data\\", all_players);
	}
	
	private static void parseOneWeekScoreboard(Document doc, Map<Player, Player> storedPlayers) {
		Elements gameTables = doc.getElementsByClass("match-recap");
		for (Element game : gameTables) {
			Element tbody = game.child(game.children().size() - 1);

			Element teams = tbody.child(1);
			String team1 = teams.child(0).ownText();
			String team2 = teams.child(3).ownText();
			
			Element playerStatRows = tbody.child(6).child(0).child(1).child(0);
			
			try {
				// Team 1
				parseFiveScoreboardRows(playerStatRows, 1, team1, storedPlayers);
				
				// Team 2
				parseFiveScoreboardRows(playerStatRows, 7, team2, storedPlayers);
			} catch (ParseException e) {
				e.printStackTrace();
				System.err.println(team1 + " vs " + team2);
			}			
		}
	}
	
	private static void parseFiveScoreboardRows(Element playerStatRows, int startIndex, String teamName, Map<Player, Player> players) throws ParseException {
		for (int i = startIndex; i < startIndex + 5; ++i) {
			Element playerRow = playerStatRows.child(i);
			
			String playerName = playerRow.child(1).child(0).attr("title");
			String position = ORDERED_POSITIONS[i - startIndex];
			int kills = NUM_FORMATTER.parse(playerRow.child(5).ownText()).intValue();
			int deaths = NUM_FORMATTER.parse(playerRow.child(6).ownText()).intValue();
			int assists = NUM_FORMATTER.parse(playerRow.child(7).ownText()).intValue();
			int creepScore = NUM_FORMATTER.parse(playerRow.child(16).ownText()).intValue();

			Player p = new Player(teamName, playerName, position, 1, kills, deaths, assists, creepScore);
			
			Player existing = players.get(p);
			if (existing != null) {
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
	}
	
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
								
				Player p = new Player(team, playerName, position, numGames, kills, deaths, assists, creepScore);
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
			bw.write("Team,Player,Position,Games,Kills,Deaths,Assists,CS,Avg Points/Game\n");
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
		// TODO: take URLs as args?
		
		//calcAndSavePointsFromPlayerStats();
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2017_NA_LCS/Spring_Split/Scoreboards");
		//parseAllSplitScoreboards("http://lol.gamepedia.com/2017_EU_LCS/Spring_Split/Scoreboards");
		parseAllSplitScoreboards("http://lol.gamepedia.com/2015_NA_LCS_Spring/Scoreboards/Round_Robin");		
	}

}