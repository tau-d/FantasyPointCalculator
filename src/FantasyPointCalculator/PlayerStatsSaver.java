package FantasyPointCalculator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class PlayerStatsSaver {
	
	private List<Player> allPlayers;
	private List<Player> tops;
	private List<Player> junglers;
	private List<Player> mids;
	private List<Player> adcs;
	private List<Player> supports;
	
	private Workbook wb;
	private CellStyle defaultStyle;
	private CellStyle headerStyle;
	private CellStyle doubleStyle;
	
	public PlayerStatsSaver(Collection<Player> players) {
		this.allPlayers = new ArrayList<>(players);
		tops = new ArrayList<>();
		junglers = new ArrayList<>();
		mids = new ArrayList<>();
		adcs = new ArrayList<>();
		supports = new ArrayList<>();
		
		Collections.sort(allPlayers, (o1, o2) -> (o1.getAvgPntsPerGame() > o2.getAvgPntsPerGame()) ? -1 : 1);
		
		// add to position lists after sort so they are already sorted
		for (Player p : allPlayers) {
			String pos = p.getPosition();
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
			} else {
				System.err.println("Invalid position: " + pos);
			}
		}
	}
	
	private void initXlsx() {
		this.wb = new XSSFWorkbook();
		
		short fontColor = IndexedColors.WHITE.getIndex();
		
		Font headerFont = wb.createFont();
		headerFont.setFontName("Calibri");
		headerFont.setBold(true);
		headerFont.setUnderline(Font.U_SINGLE);
		headerFont.setColor(fontColor);
		
		Font normalFont = wb.createFont();
		normalFont.setFontName("Calibri");
		normalFont.setColor(fontColor);
		
		short foregroundColor = IndexedColors.GREY_80_PERCENT.getIndex();
		FillPatternType fillType = FillPatternType.SOLID_FOREGROUND;
		short borderColor = IndexedColors.BLACK.getIndex();
		BorderStyle bs = BorderStyle.THIN;
		
		headerStyle = wb.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(headerFont);
		setupForeground(headerStyle, foregroundColor, fillType);
		setupBorder(headerStyle, bs, borderColor);
		
		doubleStyle = wb.createCellStyle();
		doubleStyle.setFont(normalFont);
		doubleStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("0.00"));
		setupForeground(doubleStyle, foregroundColor, fillType);
		setupBorder(doubleStyle, bs, borderColor);
		
		defaultStyle = wb.createCellStyle();
		defaultStyle.setFont(normalFont);
		setupForeground(defaultStyle, foregroundColor, fillType);
		setupBorder(defaultStyle, bs, borderColor);
	}
	
	private void setupForeground(CellStyle cellStyle, short foregroundColor, FillPatternType fillType) {
		cellStyle.setFillForegroundColor(foregroundColor);
		cellStyle.setFillPattern(fillType);
	}
	
	private void setupBorder(CellStyle cellStyle, BorderStyle bs, short borderColor) {
		cellStyle.setBorderTop(bs);
		cellStyle.setBorderBottom(bs);
		cellStyle.setBorderLeft(bs);
		cellStyle.setBorderRight(bs);
		cellStyle.setTopBorderColor(borderColor);
		cellStyle.setBottomBorderColor(borderColor);
		cellStyle.setLeftBorderColor(borderColor);
		cellStyle.setRightBorderColor(borderColor);
	}
		
	public void saveAsXLSX(String path, String filename) {
		initXlsx();
		
		Sheet allSheet = wb.createSheet("All Players");
		Sheet topSheet = wb.createSheet("Top");
		Sheet jglSheet = wb.createSheet("Jungle");
		Sheet midSheet = wb.createSheet("Mid");
		Sheet adcSheet = wb.createSheet("ADC");
		Sheet supSheet = wb.createSheet("Support");
		
		buildSheet(allSheet, allPlayers);
		// Position filter only for All Players sheet
		allSheet.setAutoFilter(new CellRangeAddress(0, allPlayers.size(), 0, 2)); 
		
		buildSheet(topSheet, tops);
		buildSheet(jglSheet, junglers);
		buildSheet(midSheet, mids);
		buildSheet(adcSheet, adcs);
		buildSheet(supSheet, supports);
		
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filename + ".xlsx");
			wb.write(fileOut);
		    fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to save XLSX file");
		}
		
		try {
			wb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void buildSheet(Sheet sheet, List<Player> players) {
		int rowNum = 0;
		
		// Create headers
		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		for (String s : Player.COL_HEADERS) {
			Cell c = row.createCell(colNum++);
			c.setCellValue(s);
			c.setCellStyle(headerStyle);
		}

		// Create player rows
		for (Player p : players) {
			colNum = 0;
			row = sheet.createRow(rowNum++);
			
			buildCell(row, colNum++, CellType.STRING, defaultStyle, p.getTeam());
			buildCell(row, colNum++, CellType.STRING, defaultStyle, p.getPlayerName());
			buildCell(row, colNum++, CellType.STRING, defaultStyle, p.getPosition());
			
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getNumGames());
			buildCell(row, colNum++, CellType.NUMERIC, doubleStyle, p.getAvgPntsPerGame());
			buildCell(row, colNum++, CellType.NUMERIC, doubleStyle, p.getStdevPntsPerGame());
			
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getNumWins());
			buildCell(row, colNum++, CellType.NUMERIC, doubleStyle, p.getAvgPntsPerWin());
			buildCell(row, colNum++, CellType.NUMERIC, doubleStyle, p.getStdevPntsPerWin());
			
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getNumLosses());
			buildCell(row, colNum++, CellType.NUMERIC, doubleStyle, p.getAvgPntsPerLoss());
			buildCell(row, colNum++, CellType.NUMERIC, doubleStyle, p.getStdevPntsPerLoss());
			
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getTotalKills());
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getTotalDeaths());
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getTotalAssists());
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getTotalCreepScore());
			buildCell(row, colNum++, CellType.NUMERIC, defaultStyle, p.getTotalTenPlusKorA());
		}
		
		// Freeze pane
		sheet.createFreezePane(3, 1);
		
		// Make sure blank cells are filled and autosize columns
		for (int i = 0; i < Player.COL_HEADERS.length + 20; ++i) {
			sheet.setDefaultColumnStyle(i, defaultStyle);
			sheet.autoSizeColumn(i);
		}
		
		// Set up filters for team and player name
		sheet.setAutoFilter(new CellRangeAddress(0, players.size(), 0, 1));
	}
	
	private void buildCell(Row r, int col, CellType type, CellStyle style, double d) {
		Cell c = r.createCell(col, type);
		c.setCellStyle(style);
		c.setCellValue(d);
	}
	
	private void buildCell(Row r, int col, CellType type, CellStyle style, String s) {
		Cell c = r.createCell(col, type);
		c.setCellStyle(style);
		c.setCellValue(s);
	}

	public void saveAsCSV(String dirPath) {
		writePlayerStatsToCsvFile(dirPath, "all_players", allPlayers);
		writePlayerStatsToCsvFile(dirPath, "mid", mids);
		writePlayerStatsToCsvFile(dirPath, "adc", adcs);
		writePlayerStatsToCsvFile(dirPath, "sup", supports);
		writePlayerStatsToCsvFile(dirPath, "top", tops);
		writePlayerStatsToCsvFile(dirPath, "jgl", junglers);
	}
	
	private void writePlayerStatsToCsvFile(String path, String filename, List<Player> players) {
		File file = new File(path);
		file.mkdirs();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path + filename + ".csv"))) {
			bw.write(Player.COL_HEADERS[0]);
			for (int i = 1; i < Player.COL_HEADERS.length; ++i) {
				bw.write("," + Player.COL_HEADERS[i]);
			}
			bw.write("\n");
			
			for (Player p : players) {
				bw.write(p.myToString(",") + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
