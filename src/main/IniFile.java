package main;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import java.awt.Color;
import java.io.File;

import java.util.prefs.Preferences;

public class IniFile {
  private String filename;
  private Ini ini;
  Preferences prefs;

  public IniFile(String name) throws Exception {
        filename = name;
        ini = new Ini(new File(filename));
        prefs = new IniPreferences(ini);
  }

  public int getIntValue(String section, String prop) {
        //return ini.get(section, prop, int.class);
        return prefs.node(section).getInt(prop, 0);
  }

  public double getDoubleValue(String section, String prop) {
        //return ini.get(section, prop, double.class);     
        return prefs.node(section).getDouble(prop, 0);
  }

  public String getStringValue(String section, String prop) {
    //return ini.get(section, prop);
    return prefs.node(section).get(prop, null);
  }

  public Color getColorValue(String section, String prop) {
    //return ini.get(section, prop);
    String c =  prefs.node(section).get(prop, null);
    Color mycolor;
    switch(c) {
      case "blue":
        mycolor = Color.BLUE;
        break;
      case "green":          
        mycolor = Color.GREEN;
        break;
      case "red":
        mycolor = Color.RED;
        break;        
      case "yellow":
        mycolor = Color.YELLOW;
        break;
      case "gray":
        mycolor = Color.GRAY;
        break;
      default:
          mycolor = Color.WHITE;
    }
    return mycolor;
  }

}
