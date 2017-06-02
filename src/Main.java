import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {

	private static void parseUrlAndAddPlayers(String url, List<Player> players, List<Player> mids, List<Player> adcs, 
				List<Player> supports, List<Player> tops, List<Player> junglers) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(10*1000).get();
			
			Elements links = doc.getElementsByClass("sortable wikitable");
			Element table = links.get(1);
			Element tbody = table.child(0);
			Elements rows = tbody.getElementsByTag("tr");
						
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			
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
				int numGames = format.parse(row.child(3).ownText()).intValue();
				int kills = format.parse(row.child(7).ownText()).intValue();
				int deaths = format.parse(row.child(8).ownText()).intValue();
				int assists = format.parse(row.child(9).ownText()).intValue();
				int creepScore = format.parse(row.child(11).ownText()).intValue();
								
				Player p = new Player(team, playerName, position, numGames, kills, deaths, assists, creepScore);
				players.add(p);
			}
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
				bw.write(p.toString(",") + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		List<Player> players = new ArrayList<>();
		List<Player> mids = new ArrayList<>();
		List<Player> adcs = new ArrayList<>();
		List<Player> supports = new ArrayList<>();
		List<Player> tops = new ArrayList<>();
		List<Player> junglers = new ArrayList<>();
		
		// North America
		parseUrlAndAddPlayers("http://lol.gamepedia.com/2017_NA_LCS/Spring_Split/Player_Statistics",
				players, mids, adcs, supports, tops, junglers);
		
		// Europe
		parseUrlAndAddPlayers("http://lol.gamepedia.com/2017_EU_LCS/Spring_Split/Players",
				players, mids, adcs, supports, tops, junglers);
		
		// Korea
		/*parseUrlAndAddPlayers("http://lol.gamepedia.com/2017_LCK/Spring_Split/Players",
				players, mids, adcs, supports, tops, junglers);*/
		
	
		Collections.sort(players, (o1, o2) -> (o1.getAPPG() > o2.getAPPG()) ? -1 : 1);
		
		// add to position lists after sort so they are already sorted
		for (Player p : players) {
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
		
		/*for (Player p : players) {
			System.out.println(p);
		}*/
		
		/*System.out.println("\nMIDDLE");
		for (Player p : mids) {
			System.out.println(p);
		}
		System.out.println("\nADC");
		for (Player p : adcs) {
			System.out.println(p);
		}
		System.out.println("\nSUPPORT");
		for (Player p : supports) {
			System.out.println(p);
		}
		System.out.println("\nTOP");
		for (Player p : tops) {
			System.out.println(p);
		}
		System.out.println("\nJUNGLE");
		for (Player p : junglers) {
			System.out.println(p);
		}*/
		
		String path = "data\\";
				
		writePlayersToCsvFile(path, "all_players", players);
		writePlayersToCsvFile(path, "mid", mids);
		writePlayersToCsvFile(path, "adc", adcs);
		writePlayersToCsvFile(path, "sup", supports);
		writePlayersToCsvFile(path, "top", tops);
		writePlayersToCsvFile(path, "jgl", junglers);
	}

}