package dialogutils;

import java.io.File;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import resources.IconsPathConfigurator;

public class MyFileView extends FileView{
	Hashtable<String,ImageIcon> table;
	ImageIcon dirIcon;

    public MyFileView()
    {
        this.table = defaultFileIcons();
        this.dirIcon = new ImageIcon(IconsPathConfigurator.iconFolderPath);
    }
    
    public MyFileView(Hashtable<String,ImageIcon> table,ImageIcon dirIcon)
    {
        this.table = (table != null)? table : defaultFileIcons();
        this.dirIcon = (dirIcon != null)? dirIcon : new ImageIcon(IconsPathConfigurator.iconFolderPath);
    }

    public Icon getIcon(File f)
    {
    // Do display custom icons

        // If dir
        if(f.isDirectory()) 
        {
            if(dirIcon!=null) 
            	return dirIcon;
        }

        // Get the name
        String name=f.getName();
        int idx=name.lastIndexOf(".");

        if(idx>-1)
        {
        String ext=name.substring(idx);
            if(table.containsKey(ext.toLowerCase()))
        return table.get(ext.toLowerCase());
    }

    // For other files
    return new ImageIcon(IconsPathConfigurator.F_ICON_GENERIC);
    }
    
    public Hashtable<String,ImageIcon> defaultFileIcons() {
    	
    	Hashtable<String,ImageIcon> table=new Hashtable<>();

	    // Add extensions and icons
	    table.put(".jpg",new ImageIcon(IconsPathConfigurator.F_ICON_JPG));
	    table.put(".jpeg",new ImageIcon(IconsPathConfigurator.F_ICON_JPG));
	    table.put(".png",new ImageIcon(IconsPathConfigurator.F_ICON_PNG));
	    table.put(".txt",new ImageIcon(IconsPathConfigurator.F_ICON_TXT));
	    table.put(".pdf",new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
	    table.put(".html",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".class",new ImageIcon(IconsPathConfigurator.F_ICON_EXE));
	    table.put(".java",new ImageIcon(IconsPathConfigurator.F_ICON_JAVA));
	    table.put(".jar",new ImageIcon(IconsPathConfigurator.F_ICON_JAR));
	    table.put(".js",new ImageIcon(IconsPathConfigurator.F_ICON_JS));
	    table.put(".json",new ImageIcon(IconsPathConfigurator.F_ICON_JSON));
	    table.put(".css",new ImageIcon(IconsPathConfigurator.F_ICON_CSS));
	    table.put(".exe",new ImageIcon(IconsPathConfigurator.F_ICON_EXE));
	    table.put(".bat",new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
	    table.put(".sh",new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
	    table.put(".php",new ImageIcon(IconsPathConfigurator.F_ICON_PHP));
	    table.put(".xml",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".zip",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".sql",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".db",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".log",new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    table.put(".lnk",new ImageIcon(IconsPathConfigurator.F_ICON_LINK));
	    table.put(".properties",new ImageIcon(IconsPathConfigurator.F_ICON_PROPERTIES));
	    
	    // generics video
	    table.put(".mov",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".mpeg",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".mpg",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".flv",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".mp4",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".gif",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    
	    // generics audio
	    table.put(".mp3",new ImageIcon(IconsPathConfigurator.F_ICON_AUDIO));
	    table.put(".wav",new ImageIcon(IconsPathConfigurator.F_ICON_WAV));
	    
	    // generics image
	    table.put(".tiff",new ImageIcon(IconsPathConfigurator.F_ICON_IMAGE));
	    table.put(".gif",new ImageIcon(IconsPathConfigurator.F_ICON_IMAGE));
	    
	    return table;
    }
}