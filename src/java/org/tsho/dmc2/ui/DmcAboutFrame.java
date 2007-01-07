/*
 * Derived from: 
 * ---------------
 * AboutPanel.java
 * ---------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 */
package org.tsho.dmc2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.jfree.JCommon;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.about.AboutFrame;
import org.jfree.ui.about.Contributor;
import org.jfree.ui.about.ContributorsPanel;
import org.jfree.ui.about.Library;
import org.jfree.ui.about.LibraryPanel;
import org.jfree.ui.about.Licences;
import org.jfree.ui.about.ProjectInfo;
import org.jfree.ui.about.SystemPropertiesPanel;
import org.tsho.dmc2.DmcDue;


public class DmcAboutFrame extends JFrame {

    private static final String myInfo = 
        "iDMC the interactive Dynamical Model Calculator simulates and performs "
        + "graphical and numerical analysis of systems of differential and " 
        + "difference equations."
        + "\n"
        + "\n"
        + "The software program was developed within a research project financed "
        + "by the Italian Ministry of Universities, the Universities of Udine and "
        + "Ca'Foscari of Venice, the Friuli-Venezia Giulia Region.";

    
    
    DmcAboutFrame() {
        this("iDmc", new DmcDueInfo());
    }

    static class DmcDueInfo extends ProjectInfo {

        public DmcDueInfo() {
            setName(DmcDue.Defaults.name);
            setVersion(DmcDue.Defaults.version);
            setInfo(myInfo);
            setCopyright("Copyright 2004-2007 Marji Lines and Alfredo Medio");
            setLogo(null);
            setLicenceName("GPL");
            setLicenceText(Licences.getInstance().getGPL());

            setContributors(Arrays.asList(
                new Contributor[]{
                    new Contributor("Daniele Pizzoni", "auouo@tin.it"),
			  new Contributor("Alexei Grigoriev","alexei_grigoriev@libero.it"),
			  new Contributor("Antonio, Fabio Di Narzo", "antonio.fabio@gmail.com")
                }
            ));

            setLibraries(Arrays.asList(
                new Library[]{
                    new Library(JFreeChart.INFO),
                    new Library(JCommon.INFO),
                    new Library("JGoodies Forms", "1.0.3", "BSD", "http://www.jgoodies.com/"),
                    new Library("idmclib", "0.1.1", "GPL", ""),
            }
            ));
        }
    }



    /** The preferred size for the frame. */
    public static final Dimension PREFERRED_SIZE = new Dimension(560, 360);

    /** The default border for the panels in the tabbed pane. */
    public static final Border STANDARD_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    /** Localised resources. */
    private ResourceBundle resources;

    /** The application name. */
    private String application;

    /** The application version. */
    private String version;

    /** The copyright string. */
    private String copyright;

    /** Other info about the application. */
    private String info;

    /** The project logo. */
    private Image logo;

    /** A list of contributors. */
    private List contributors;

    /** The licence. */
    private String licence;

    /** A list of libraries. */
    private List libraries;

    /**
     * Constructs an about frame.
     *
     * @param title  the frame title.
     * @param project  information about the project.
     */
    public DmcAboutFrame(String title, ProjectInfo project) {

        this(title,
             project.getName(),
             "Version " + project.getVersion(),
             project.getInfo(),
             project.getLogo(),
             project.getCopyright(),
             project.getLicenceText(),
             project.getContributors(),
             project.getLibraries());

    }

    /**
     * Constructs an 'About' frame.
     *
     * @param title  the frame title.
     * @param application  the application name.
     * @param version  the version.
     * @param info  other info.
     * @param logo  an optional logo.
     * @param copyright  the copyright notice.
     * @param licence  the licence.
     * @param contributors  a list of developers/contributors.
     * @param libraries  a list of libraries.
     */
    public DmcAboutFrame(String title,
                      String application, String version, String info,
                      Image logo,
                      String copyright, String licence,
                      List contributors,
                      List libraries) {

        super(title);

        this.application = application;
        this.version = version;
        this.copyright = copyright;
        this.info = info;
        this.logo = logo;
        this.contributors = contributors;
        this.licence = licence;
        this.libraries = libraries;

        String baseName = "org.jfree.ui.about.resources.AboutResources";
        this.resources = ResourceBundle.getBundle(baseName);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(STANDARD_BORDER);

        JTabbedPane tabs = createTabs();
        content.add(tabs);
        setContentPane(content);

        pack();

    }

    /**
     * Returns the preferred size for the about frame.
     *
     * @return the preferred size.
     */
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    /**
     * Creates a tabbed pane containing an about panel and a system properties panel.
     *
     * @return a tabbed pane.
     */
    private JTabbedPane createTabs() {

        JTabbedPane tabs = new JTabbedPane();

        JPanel aboutPanel = createAboutPanel();
        aboutPanel.setBorder(AboutFrame.STANDARD_BORDER);
        String aboutTab = this.resources.getString("about-frame.tab.about");
        tabs.add(aboutTab, aboutPanel);

        JPanel systemPanel = new SystemPropertiesPanel();
        systemPanel.setBorder(AboutFrame.STANDARD_BORDER);
        String systemTab = this.resources.getString("about-frame.tab.system");
        tabs.add(systemTab, systemPanel);

        return tabs;

    }

    /**
     * Creates a panel showing information about the application, including the name, version,
     * copyright notice, URL for further information, and a list of contributors.
     *
     * @return a panel.
     */
    private JPanel createAboutPanel() {

        JPanel about = new JPanel(new BorderLayout());

        JPanel details = new DmcAboutPanel(this.application, this.version, this.copyright, this.info,
                                        this.logo);

        boolean includetabs = false;
        JTabbedPane tabs = new JTabbedPane();

        if (this.contributors != null) {
            JPanel contributorsPanel = new ContributorsPanel(this.contributors);
            contributorsPanel.setBorder(AboutFrame.STANDARD_BORDER);
            String contributorsTab = this.resources.getString("about-frame.tab.contributors");
            tabs.add(contributorsTab, contributorsPanel);
            includetabs = true;
        }

        if (this.licence != null) {
            JPanel licencePanel = createLicencePanel();
            licencePanel.setBorder(STANDARD_BORDER);
            String licenceTab = this.resources.getString("about-frame.tab.licence");
            tabs.add(licenceTab, licencePanel);
            includetabs = true;
        }

        if (this.libraries != null) {
            JPanel librariesPanel = new LibraryPanel(this.libraries);
            librariesPanel.setBorder(AboutFrame.STANDARD_BORDER);
            String librariesTab = this.resources.getString("about-frame.tab.libraries");
            tabs.add(librariesTab, librariesPanel);
            includetabs = true;
        }

        about.add(details, BorderLayout.NORTH);
        if (includetabs) {
            about.add(tabs);
        }

        return about;

    }

    /**
     * Creates a panel showing the licence.
     *
     * @return a panel.
     */
    private JPanel createLicencePanel() {

        JPanel licencePanel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea(this.licence);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setEditable(false);
        licencePanel.add(new JScrollPane(area));
        return licencePanel;

    }


}
