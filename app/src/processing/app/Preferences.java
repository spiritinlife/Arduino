/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-09 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app;

import static processing.app.I18n._;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import processing.app.helpers.FileUtils;
import processing.app.helpers.PreferencesMap;
import processing.app.syntax.SyntaxStyle;
import processing.core.PApplet;
import processing.core.PConstants;


/**
 * Storage class for user preferences and environment settings.
 * <P>
 * This class no longer uses the Properties class, since
 * properties files are iso8859-1, which is highly likely to
 * be a problem when trying to save sketch folders and locations.
 * <p>
 * The GUI portion in here is really ugly, as it uses exact layout. This was
 * done in frustration one evening (and pre-Swing), but that's long since past,
 * and it should all be moved to a proper swing layout like BoxLayout.
 * <p>
 * This is very poorly put together, that the preferences panel and the actual
 * preferences i/o is part of the same code. But there hasn't yet been a
 * compelling reason to bother with the separation aside from concern about
 * being lectured by strangers who feel that it doesn't look like what they
 * learned in CS class.
 * <p>
 * Would also be possible to change this to use the Java Preferences API.
 * Some useful articles
 * <a href="http://www.onjava.com/pub/a/onjava/synd/2001/10/17/j2se.html">here</a> and
 * <a href="http://www.particle.kth.se/~lindsey/JavaCourse/Book/Part1/Java/Chapter10/Preferences.html">here</a>.
 * However, haven't implemented this yet for lack of time, but more
 * importantly, because it would entail writing to the registry (on Windows),
 * or an obscure file location (on Mac OS X) and make it far more difficult to
 * find the preferences to tweak them by hand (no! stay out of regedit!)
 * or to reset the preferences by simply deleting the preferences.txt file.
 */
public class Preferences {

  static final String PREFS_FILE = "preferences.txt";

  class Language {
    Language(String _name, String _originalName, String _isoCode) {
      name = _name;
      originalName = _originalName;
      isoCode = _isoCode;
    }

    public String toString() {
      if (originalName.length() == 0)
        return name;
      return originalName + " (" + name + ")";
    };

    String name;
    String originalName;
    String isoCode;
  }

  Language languages[] = {
      new Language(_("System Default"), "", ""),
      new Language(_("Albanian"), "shqip", "sq"),
      new Language(_("Arabic"), "العربية", "ar"),
      new Language(_("Aragonese"), "Aragonés", "an"),
      new Language(_("Belarusian"), "Беларуская мова", "be"),
      new Language(_("Bulgarian"), "български", "bg"),
      new Language(_("Catalan"), "Català", "ca"),
      new Language(_("Chinese Simplified"), "简体中文", "zh_CN"),
      new Language(_("Chinese Traditional"), "繁體中文", "zh_TW"),
      new Language(_("Croatian"), "Hrvatski", "hr_HR"),
      new Language(_("Czech"), "český", "cs_CZ"),
      new Language(_("Danish"), "Dansk", "da_DK"),
      new Language(_("Dutch"), "Nederlands", "nl"),
      new Language(_("English"), "English", "en"),
      new Language(_("English (United Kingdom)"), "English (United Kingdom)", "en_GB"),
      new Language(_("Estonian"), "Eesti", "et"),
      new Language(_("Estonian (Estonia)"), "Eesti keel", "et_EE"),
      new Language(_("Filipino"), "Pilipino", "fil"),
      new Language(_("Finnish"), "Suomi", "fi"),
      new Language(_("French"), "Français", "fr"),
      new Language(_("Canadian French"), "Canadienne-français", "fr_CA"),
      new Language(_("Galician"), "Galego", "gl"),
      new Language(_("Georgian"), "საქართველოს", "ka_GE"),
      new Language(_("German"), "Deutsch", "de_DE"),
      new Language(_("Greek"), "ελληνικά", "el_GR"),
      new Language(_("Hebrew"), "עברית", "he"),
      new Language(_("Hindi"), "हिंदी", "hi"),
      new Language(_("Hungarian"), "Magyar", "hu"),
      new Language(_("Indonesian"), "Bahasa Indonesia", "id"),
      new Language(_("Italian"), "Italiano", "it_IT"),
      new Language(_("Japanese"), "日本語", "ja_JP"),
      new Language(_("Korean"), "한국어", "ko_KR"),
      new Language(_("Latvian"), "Latviešu", "lv_LV"),
      new Language(_("Lithuaninan"), "Lietuvių Kalba", "lt_LT"),
      new Language(_("Norwegian Bokmål"), "Norsk bokmål", "nb_NO"),
      new Language(_("Persian"), "فارسی", "fa"),
      new Language(_("Polish"), "Język Polski", "pl"),
      new Language(_("Portuguese (Brazil)"), "Português (Brazil)", "pt_BR"),
      new Language(_("Portuguese (Portugal)"), "Português (Portugal)", "pt_PT"),
      new Language(_("Romanian"), "Română", "ro"),
      new Language(_("Russian"), "Русский", "ru"),
      new Language(_("Slovenian"), "Slovenščina", "sl_SI"),
      new Language(_("Spanish"), "Español", "es"),
      new Language(_("Swedish"), "Svenska", "sv"),
      new Language(_("Tamil"), "தமிழ்", "ta"),
      new Language(_("Turkish"), "Türk", "tr"),
      new Language(_("Ukrainian"), "Український", "uk"), 
      new Language(_("Vietnamese"), "Tiếng Việt", "vi"), 
      };

  // Incomplete languages 
  Language missingLanguages[] = {
      new Language(_("Armenian"), "Հայերեն", "hy"),
      new Language(_("Asturian"), "Asturianu", "ast"),
      new Language(_("Basque"), "Euskara", "eu"),
      new Language(_("Bosnian"), "Bosanski", "bs"),
      new Language(_("Burmese (Myanmar)"), "ဗမာစကား", "my_MM"),
      new Language(_("Chinese (China)"), "", "zh_CN"),
      new Language(_("Chinese (Hong Kong)"), "", "zh_HK"),
      new Language(_("Chinese (Taiwan)"), "", "zh_TW"),
      new Language(_("Chinese (Taiwan) (Big5)"), "", "zh_TW.Big5"),
      new Language(_("Dutch (Netherlands)"), "Nederlands", "nl_NL"),
      new Language(_("Nepali"), "नेपाली", "ne"),
      new Language(_("N'Ko"), "ߒߞߏ", "nqo"),
      new Language(_("Marathi"), "मराठी", "mr"),
      new Language(_("Portugese"), "Português", "pt"),
      new Language(_("Persian (Iran)"), "فارسی (Iran)", "fa_IR"),
      };

  /**
   * Standardized width for buttons. Mac OS X 10.3 wants 70 as its default,
   * Windows XP needs 66, and my Ubuntu machine needs 80+, so 80 seems proper.
   */
  static public int BUTTON_WIDTH  = 80;

  /**
   * Standardized button height. Mac OS X 10.3 (Java 1.4) wants 29,
   * presumably because it now includes the blue border, where it didn't
   * in Java 1.3. Windows XP only wants 23 (not sure what default Linux
   * would be). Because of the disparity, on Mac OS X, it will be set
   * inside a static block.
   */
  static public int BUTTON_HEIGHT = 24;

  // value for the size bars, buttons, etc

  static final int GRID_SIZE     = 33;


  // indents and spacing standards. these probably need to be modified
  // per platform as well, since macosx is so huge, windows is smaller,
  // and linux is all over the map

  static final int GUI_BIG     = 13;
  static final int GUI_BETWEEN = 10;
  static final int GUI_SMALL   = 6;

  // gui elements

  JFrame dialog;
  int wide, high;

  JTextField sketchbookLocationField;
  JCheckBox exportSeparateBox;
  JCheckBox verboseCompilationBox;
  JCheckBox verboseUploadBox;
  JCheckBox displayLineNumbersBox;
  JCheckBox verifyUploadBox;
  JCheckBox externalEditorBox;
  JCheckBox memoryOverrideBox;
  JTextField memoryField;
  JCheckBox checkUpdatesBox;
  JTextField fontSizeField;
  JCheckBox updateExtensionBox;
  JCheckBox autoAssociateBox;
  JComboBox comboLanguage;


  // the calling editor, so updates can be applied

  Editor editor;


  // data model

  static PreferencesMap defaults;
  static PreferencesMap prefs = new PreferencesMap();
  static File preferencesFile;
  static boolean doSave = true;


  static protected void init(File file) {
    if (file != null)
      preferencesFile = file;
    else
      preferencesFile = Base.getSettingsFile(Preferences.PREFS_FILE);

    // start by loading the defaults, in case something
    // important was deleted from the user prefs
    try {
      prefs.load(Base.getLibStream("preferences.txt"));
    } catch (IOException e) {
      Base.showError(null, _("Could not read default settings.\n" +
                             "You'll need to reinstall Arduino."), e);
    }

    // set some runtime constants (not saved on preferences file)
    File hardwareFolder = Base.getHardwareFolder();
    prefs.put("runtime.ide.path", hardwareFolder.getParentFile().getAbsolutePath());
    prefs.put("runtime.ide.version", "" + Base.REVISION);
    
    // clone the hash table
    defaults = new PreferencesMap(prefs);

    if (preferencesFile.exists()) {
      // load the previous preferences file
      try {
        prefs.load(preferencesFile);
      } catch (IOException ex) {
        Base.showError(_("Error reading preferences"),
                       I18n.format(_("Error reading the preferences file. "
                                       + "Please delete (or move)\n"
                                       + "{0} and restart Arduino."),
                                   preferencesFile.getAbsolutePath()), ex);
      }
    }

    // load the I18n module for internationalization
    try {
      I18n.init(get("editor.languages.current"));
    } catch (MissingResourceException e) {
      I18n.init("en");
      set("editor.languages.current", "en");
    }

    // set some other runtime constants (not saved on preferences file)
    set("runtime.os", PConstants.platformNames[PApplet.platform]);

    // other things that have to be set explicitly for the defaults
    setColor("run.window.bgcolor", SystemColor.control);

    fixPreferences();
  }

  private static void fixPreferences() {
    String baud = get("serial.debug_rate");
    if ("14400".equals(baud) || "28800".equals(baud) || "38400".equals(baud)) {
      set("serial.debug_rate", "9600");
    }
  }


  public Preferences() {

    // setup dialog for the prefs

    //dialog = new JDialog(editor, "Preferences", true);
    dialog = new JFrame(_("Preferences"));
    dialog.setResizable(false);

    Container pain = dialog.getContentPane();
    pain.setLayout(null);

    int top = GUI_BIG;
    int left = GUI_BIG;
    int right = 0;

    JLabel label;
    JButton button; //, button2;
    //JComboBox combo;
    Dimension d, d2; //, d3;
    int h, vmax;


    // Sketchbook location:
    // [...............................]  [ Browse ]

    label = new JLabel(_("Sketchbook location:"));
    pain.add(label);
    d = label.getPreferredSize();
    label.setBounds(left, top, d.width, d.height);
    top += d.height; // + GUI_SMALL;

    sketchbookLocationField = new JTextField(40);
    pain.add(sketchbookLocationField);
    d = sketchbookLocationField.getPreferredSize();

    button = new JButton(I18n.PROMPT_BROWSE);
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          File dflt = new File(sketchbookLocationField.getText());
          File file =
                  Base.selectFolder(_("Select new sketchbook location"), dflt, dialog);
          if (file != null) {
            String path = file.getAbsolutePath();
            if (Base.getPortableFolder() != null) {
              path = FileUtils.relativePath(Base.getPortableFolder().toString(), path);
              if (path == null) {
                path = Base.getPortableSketchbookFolder();
              }
            }
            sketchbookLocationField.setText(path);
          }
        }
      });
    pain.add(button);
    d2 = button.getPreferredSize();

    // take max height of all components to vertically align em
    vmax = Math.max(d.height, d2.height);
    sketchbookLocationField.setBounds(left, top + (vmax-d.height)/2,
                                      d.width, d.height);
    h = left + d.width + GUI_SMALL;
    button.setBounds(h, top + (vmax-d2.height)/2,
                     d2.width, d2.height);

    right = Math.max(right, h + d2.width + GUI_BIG);
    top += vmax + GUI_BETWEEN;


    // Preferred language: [        ] (requires restart of Arduino)
    Container box = Box.createHorizontalBox();
    label = new JLabel(_("Editor language: "));
    box.add(label);
    comboLanguage = new JComboBox(languages);
    String currentLanguage = Preferences.get("editor.languages.current");
    for (Language language : languages) {
      if (language.isoCode.equals(currentLanguage))
        comboLanguage.setSelectedItem(language);
    }
    box.add(comboLanguage);
    label = new JLabel(_("  (requires restart of Arduino)"));
    box.add(label);
    pain.add(box);
    d = box.getPreferredSize();
    box.setForeground(Color.gray);
    box.setBounds(left, top, d.width, d.height);
    right = Math.max(right, left + d.width);
    top += d.height + GUI_BETWEEN;
    
    // Editor font size [    ]

    box = Box.createHorizontalBox();
    label = new JLabel(_("Editor font size: "));
    box.add(label);
    fontSizeField = new JTextField(4);
    box.add(fontSizeField);
    label = new JLabel(_("  (requires restart of Arduino)"));
    box.add(label);
    pain.add(box);
    d = box.getPreferredSize();
    box.setBounds(left, top, d.width, d.height);
    Font editorFont = Preferences.getFont("editor.font");
    fontSizeField.setText(String.valueOf(editorFont.getSize()));
    top += d.height + GUI_BETWEEN;


    // Show verbose output during: [ ] compilation [ ] upload
    
    box = Box.createHorizontalBox();
    label = new JLabel(_("Show verbose output during: "));
    box.add(label);
    verboseCompilationBox = new JCheckBox(_("compilation "));
    box.add(verboseCompilationBox);
    verboseUploadBox = new JCheckBox(_("upload"));
    box.add(verboseUploadBox);
    pain.add(box);
    d = box.getPreferredSize();
    box.setBounds(left, top, d.width, d.height);
    top += d.height + GUI_BETWEEN;

	// [ ] Display line numbers
    
    displayLineNumbersBox = new JCheckBox(_("Display line numbers"));
    pain.add(displayLineNumbersBox);
    d = displayLineNumbersBox.getPreferredSize();
    displayLineNumbersBox.setBounds(left, top, d.width + 10, d.height);
    right = Math.max(right, left + d.width);
    top += d.height + GUI_BETWEEN;
	
    // [ ] Verify code after upload
    
    verifyUploadBox = new JCheckBox(_("Verify code after upload"));
    pain.add(verifyUploadBox);
    d = verifyUploadBox.getPreferredSize();
    verifyUploadBox.setBounds(left, top, d.width + 10, d.height);
    right = Math.max(right, left + d.width);
    top += d.height + GUI_BETWEEN;
    
    // [ ] Use external editor

    externalEditorBox = new JCheckBox(_("Use external editor"));
    pain.add(externalEditorBox);
    d = externalEditorBox.getPreferredSize();
    externalEditorBox.setBounds(left, top, d.width + 10, d.height);
    right = Math.max(right, left + d.width);
    top += d.height + GUI_BETWEEN;


    // [ ] Check for updates on startup

    checkUpdatesBox = new JCheckBox(_("Check for updates on startup"));
    pain.add(checkUpdatesBox);
    d = checkUpdatesBox.getPreferredSize();
    checkUpdatesBox.setBounds(left, top, d.width + 10, d.height);
    right = Math.max(right, left + d.width);
    top += d.height + GUI_BETWEEN;
    
    // [ ] Update sketch files to new extension on save (.pde -> .ino)
    
    updateExtensionBox = new JCheckBox(_("Update sketch files to new extension on save (.pde -> .ino)"));
    pain.add(updateExtensionBox);
    d = updateExtensionBox.getPreferredSize();
    updateExtensionBox.setBounds(left, top, d.width + 10, d.height);
    right = Math.max(right, left + d.width);
    top += d.height + GUI_BETWEEN;    

    // [ ] Automatically associate .pde files with Processing

    if (Base.isWindows()) {
      autoAssociateBox =
        new JCheckBox(_("Automatically associate .ino files with Arduino"));
      pain.add(autoAssociateBox);
      d = autoAssociateBox.getPreferredSize();
      autoAssociateBox.setBounds(left, top, d.width + 10, d.height);
      right = Math.max(right, left + d.width);
      top += d.height + GUI_BETWEEN;

      // If using portable mode, it's bad manner to change PC setting.
      if (Base.getPortableFolder() != null)
        autoAssociateBox.setEnabled(false);
    }

    // More preferences are in the ...

    label = new JLabel(_("More preferences can be edited directly in the file"));
    pain.add(label);
    d = label.getPreferredSize();
    label.setForeground(Color.gray);
    label.setBounds(left, top, d.width, d.height);
    right = Math.max(right, left + d.width);
    top += d.height; // + GUI_SMALL;

    label = new JLabel(preferencesFile.getAbsolutePath());
    final JLabel clickable = label;
    label.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          Base.openFolder(preferencesFile.getParentFile());
        }

        public void mouseEntered(MouseEvent e) {
          clickable.setForeground(new Color(0, 0, 140));
        }

        public void mouseExited(MouseEvent e) {
          clickable.setForeground(Color.BLACK);
        }
      });
    pain.add(label);
    d = label.getPreferredSize();
    label.setBounds(left, top, d.width, d.height);
    right = Math.max(right, left + d.width);
    top += d.height;

    label = new JLabel(_("(edit only when Arduino is not running)"));
    pain.add(label);
    d = label.getPreferredSize();
    label.setForeground(Color.gray);
    label.setBounds(left, top, d.width, d.height);
    right = Math.max(right, left + d.width);
    top += d.height; // + GUI_SMALL;


    // [  OK  ] [ Cancel ]  maybe these should be next to the message?

    button = new JButton(I18n.PROMPT_OK);
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          applyFrame();
          disposeFrame();
        }
      });
    pain.add(button);
    d2 = button.getPreferredSize();
    BUTTON_HEIGHT = d2.height;

    h = right - (BUTTON_WIDTH + GUI_SMALL + BUTTON_WIDTH);
    button.setBounds(h, top, BUTTON_WIDTH, BUTTON_HEIGHT);
    h += BUTTON_WIDTH + GUI_SMALL;

    button = new JButton(I18n.PROMPT_CANCEL);
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          disposeFrame();
        }
      });
    pain.add(button);
    button.setBounds(h, top, BUTTON_WIDTH, BUTTON_HEIGHT);

    top += BUTTON_HEIGHT + GUI_BETWEEN;


    // finish up

    wide = right + GUI_BIG;
    high = top + GUI_SMALL;


    // closing the window is same as hitting cancel button

    dialog.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          disposeFrame();
        }
      });

    ActionListener disposer = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          disposeFrame();
        }
      };
    Base.registerWindowCloseKeys(dialog.getRootPane(), disposer);
    Base.setIcon(dialog);

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation((screen.width - wide) / 2,
                      (screen.height - high) / 2);

    dialog.pack(); // get insets
    Insets insets = dialog.getInsets();
    dialog.setSize(wide + insets.left + insets.right,
                  high + insets.top + insets.bottom);


    // handle window closing commands for ctrl/cmd-W or hitting ESC.

    pain.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          //System.out.println(e);
          KeyStroke wc = Editor.WINDOW_CLOSE_KEYSTROKE;
          if ((e.getKeyCode() == KeyEvent.VK_ESCAPE) ||
              (KeyStroke.getKeyStrokeForEvent(e).equals(wc))) {
            disposeFrame();
          }
        }
      });
  }


  // .................................................................


  /**
   * Close the window after an OK or Cancel.
   */
  protected void disposeFrame() {
    dialog.dispose();
  }


  /**
   * Change internal settings based on what was chosen in the prefs,
   * then send a message to the editor saying that it's time to do the same.
   */
  protected void applyFrame() {
    // put each of the settings into the table
    setBoolean("build.verbose", verboseCompilationBox.isSelected());
    setBoolean("upload.verbose", verboseUploadBox.isSelected());
    setBoolean("editor.linenumbers", displayLineNumbersBox.isSelected());
    setBoolean("upload.verify", verifyUploadBox.isSelected());
    
//    setBoolean("sketchbook.closing_last_window_quits",
//               closingLastQuitsBox.isSelected());
    //setBoolean("sketchbook.prompt", sketchPromptBox.isSelected());
    //setBoolean("sketchbook.auto_clean", sketchCleanBox.isSelected());

    // if the sketchbook path has changed, rebuild the menus
    String oldPath = get("sketchbook.path");
    String newPath = sketchbookLocationField.getText();
    if (newPath.isEmpty()) {
      if (Base.getPortableFolder() == null)
        newPath = editor.base.getDefaultSketchbookFolder().toString();
      else
        newPath = Base.getPortableSketchbookFolder();
    }
    if (!newPath.equals(oldPath)) {
      editor.base.rebuildSketchbookMenus();
      set("sketchbook.path", newPath);
    }

    setBoolean("editor.external", externalEditorBox.isSelected());
    setBoolean("update.check", checkUpdatesBox.isSelected());

    /*
      // was gonna use this to check memory settings,
      // but it quickly gets much too messy
    if (getBoolean("run.options.memory")) {
      Process process = Runtime.getRuntime().exec(new String[] {
          "java", "-Xms" + memoryMin + "m", "-Xmx" + memoryMax + "m"
        });
      processInput = new SystemOutSiphon(process.getInputStream());
      processError = new MessageSiphon(process.getErrorStream(), this);
    }
    */

    String newSizeText = fontSizeField.getText();
    try {
      int newSize = Integer.parseInt(newSizeText.trim());
      String pieces[] = PApplet.split(get("editor.font"), ',');
      pieces[2] = String.valueOf(newSize);
      set("editor.font", PApplet.join(pieces, ','));

    } catch (Exception e) {
      System.err.println(I18n.format(_("ignoring invalid font size {0}"), newSizeText));
    }

    if (autoAssociateBox != null) {
      setBoolean("platform.auto_file_type_associations",
                 autoAssociateBox.isSelected());
    }
    
    setBoolean("editor.update_extension", updateExtensionBox.isSelected());

    // adds the selected language to the preferences file
    Language newLanguage = (Language) comboLanguage.getSelectedItem();
    set("editor.languages.current", newLanguage.isoCode);

    editor.applyPreferences();
  }


  protected void showFrame(Editor editor) {
    this.editor = editor;

    // set all settings entry boxes to their actual status
    verboseCompilationBox.setSelected(getBoolean("build.verbose"));
    verboseUploadBox.setSelected(getBoolean("upload.verbose"));
    displayLineNumbersBox.setSelected(getBoolean("editor.linenumbers"));
    verifyUploadBox.setSelected(getBoolean("upload.verify"));

    //closingLastQuitsBox.
    //  setSelected(getBoolean("sketchbook.closing_last_window_quits"));
    //sketchPromptBox.
    //  setSelected(getBoolean("sketchbook.prompt"));
    //sketchCleanBox.
    //  setSelected(getBoolean("sketchbook.auto_clean"));

    sketchbookLocationField.
      setText(get("sketchbook.path"));
    externalEditorBox.
      setSelected(getBoolean("editor.external"));
    checkUpdatesBox.
      setSelected(getBoolean("update.check"));

    if (autoAssociateBox != null) {
      autoAssociateBox.
        setSelected(getBoolean("platform.auto_file_type_associations"));
    }
    
    updateExtensionBox.setSelected(get("editor.update_extension") == null ||
                                   getBoolean("editor.update_extension"));

    dialog.setVisible(true);
  }


  // .................................................................


  static public String[] loadStrings(InputStream input) {
    try {
      BufferedReader reader =
              new BufferedReader(new InputStreamReader(input, "UTF-8"));

      String lines[] = new String[100];
      int lineCount = 0;
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (lineCount == lines.length) {
          String temp[] = new String[lineCount << 1];
          System.arraycopy(lines, 0, temp, 0, lineCount);
          lines = temp;
        }
        lines[lineCount++] = line;
      }
      reader.close();

      if (lineCount == lines.length) {
        return lines;
      }

      // resize array to appropriate amount for these lines
      String output[] = new String[lineCount];
      System.arraycopy(lines, 0, output, 0, lineCount);
      return output;

    } catch (IOException e) {
      e.printStackTrace();
      //throw new RuntimeException("Error inside loadStrings()");
    }
    return null;
  }



  // .................................................................


  static protected void save() {
    if (!doSave) return;
//    try {
    // on startup, don't worry about it
    // this is trying to update the prefs for who is open
    // before Preferences.init() has been called.
    if (preferencesFile == null) return;

    // Fix for 0163 to properly use Unicode when writing preferences.txt
    PrintWriter writer = PApplet.createWriter(preferencesFile);

    String[] keys = prefs.keySet().toArray(new String[0]);
    Arrays.sort(keys);
    for (String key: keys) {
      if (key.startsWith("runtime."))
        continue;
      writer.println(key + "=" + prefs.get(key));
    }

    writer.flush();
    writer.close();
  }


  // .................................................................

  static public String get(String attribute) {
    return prefs.get(attribute);
  }

  static public String get(String attribute, String defaultValue) {
    String value = get(attribute);
    return (value == null) ? defaultValue : value;
  }

  public static boolean has(String key) {
    return prefs.containsKey(key);
  }

  public static void remove(String key) {
    prefs.remove(key);
  }

  static public String getDefault(String attribute) {
    return defaults.get(attribute);
  }


  static public void set(String attribute, String value) {
    prefs.put(attribute, value);
  }


  static public void unset(String attribute) {
    prefs.remove(attribute);
  }


  static public boolean getBoolean(String attribute) {
    return prefs.getBoolean(attribute);
  }


  static public void setBoolean(String attribute, boolean value) {
    prefs.putBoolean(attribute, value);
  }


  static public int getInteger(String attribute) {
    return Integer.parseInt(get(attribute));
  }


  static public void setInteger(String key, int value) {
    set(key, String.valueOf(value));
  }


  static public Color getColor(String name) {
    Color parsed = prefs.getColor(name);
    if (parsed != null)
      return parsed;
    return Color.GRAY; // set a default
  }


  static public void setColor(String attr, Color what) {
    prefs.putColor(attr, what);
  }


  static public Font getFont(String attr) {
    Font font = prefs.getFont(attr);
    if (font == null) {
      String value = defaults.get(attr);
      prefs.put(attr, value);
      font = prefs.getFont(attr);
    }
    return font;
  }


  static public SyntaxStyle getStyle(String what) {
    String str = get("editor." + what + ".style");

    StringTokenizer st = new StringTokenizer(str, ",");

    String s = st.nextToken();
    if (s.indexOf("#") == 0) s = s.substring(1);
    Color color = Color.DARK_GRAY;
    try {
      color = new Color(Integer.parseInt(s, 16));
    } catch (Exception e) { }

    s = st.nextToken();
    boolean bold = (s.indexOf("bold") != -1);
    boolean italic = (s.indexOf("italic") != -1);
    boolean underlined = (s.indexOf("underlined") != -1);

    return new SyntaxStyle(color, italic, bold, underlined);
  }
  
  // get a copy of the Preferences
  static public PreferencesMap getMap() 
  {
    return new PreferencesMap(prefs);
  }

  // Decide wether changed preferences will be saved. When value is
  // false, Preferences.save becomes a no-op.
  static public void setDoSave(boolean value)
  {
    doSave = value;
  }
}
