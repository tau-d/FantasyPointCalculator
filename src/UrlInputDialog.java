import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;

//TODO: let user choose whether to split individual players by team/position?

public class UrlInputDialog extends JDialog {

	private static final long serialVersionUID = -8755424440192603683L;
	
	private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel urlInputPanel;
    private javax.swing.JScrollPane urlScrollPanel;
    private javax.swing.JTextArea urlTextArea;
    private javax.swing.JLabel urlTextAreaLabel;
	
	public UrlInputDialog() {
		super();
		
		urlInputPanel = new javax.swing.JPanel();
        urlTextAreaLabel = new javax.swing.JLabel();
        urlScrollPanel = new javax.swing.JScrollPane();
        urlTextArea = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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
			if (!url.isEmpty()) urlList.add(url);
		}
		
		LeaguepediaScraper.parseUrls(urlList);
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
