package FantasyPointCalculator;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

// TODO: let user choose whether to split individual players by team/position?
// TODO: let user choose save format 

public class UrlInputDialog extends JDialog {

	private static final long serialVersionUID = -8755424440192603683L;
	
	private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;
    private JPanel urlInputPanel;
    private JScrollPane urlScrollPanel;
    private JTextArea urlTextArea;
    private JLabel urlTextAreaLabel;
	
	public UrlInputDialog() {
		super();
		
		urlInputPanel = new JPanel();
        urlTextAreaLabel = new JLabel();
        urlScrollPanel = new JScrollPane();
        urlTextArea = new JTextArea();
        buttonPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Fantasy Point Calculator");

        urlTextAreaLabel.setText("Leaguepedia Scoreboard URLs (one URL per line and strip \"/week_XX\"): ");
        urlTextAreaLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        urlTextAreaLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        urlTextArea.setColumns(20);
        urlTextArea.setRows(5);
        urlTextArea.setText("http://lol.gamepedia.com/2017_NA_LCS/Summer_Split/Scoreboards\nhttp://lol.gamepedia.com/2017_EU_LCS/Summer_Split/Scoreboards");
                
        urlScrollPanel.setViewportView(urlTextArea);
		urlScrollPanel.setPreferredSize(new Dimension(500, 100));
		urlScrollPanel.setAlignmentX(LEFT_ALIGNMENT);
        
		urlInputPanel.setLayout(new BoxLayout(urlInputPanel, BoxLayout.PAGE_AXIS));
		urlInputPanel.setAlignmentX(LEFT_ALIGNMENT);
		urlInputPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        urlInputPanel.add(urlTextAreaLabel);
        urlInputPanel.add(urlScrollPanel);        
        
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(urlInputPanel);
        contentPane.add(buttonPanel);
        
        pack();
	}

	protected void okButtonActionPerformed(ActionEvent evt) {		
		String[] lines = urlTextArea.getText().split("\n");
		
		// TODO: handle bad URLs
		List<String> urlList = new ArrayList<>();
		for (String url : lines) {
			String trimmed = url.trim();
			if (!trimmed.isEmpty()) urlList.add(trimmed);
		}
		
		Collection<Player> players = LeaguepediaScraper.getPlayerStatsFromBaseScoreboardUrls(urlList);
		PlayerStatsSaver pss = new PlayerStatsSaver(players);
		
		// TODO: check if file is being used
		System.out.println("SAVING START");
		String dirPath = "scoreboard_data\\";
		pss.saveAsCSV(dirPath);
		pss.saveAsXLSX("", "workbook");
		System.out.println("SAVING COMPLETE");
		
		dispose();
	}
	
	private void centerOnScreen() {
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
	}
	
	public static void makeDialog() {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UrlInputDialog dialog = new UrlInputDialog();
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.centerOnScreen();
                dialog.setVisible(true);
            }
        });
	}
}
