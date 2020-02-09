package om.themes;

import java.awt.Color;

public class ThemeElement {

	String tokenName;
	Color background;
	Color foreground;
	int tokenFontStyle;
	
	public ThemeElement(String tokenName, Color background, Color foreground, int tokenFontStyle){
		this.tokenFontStyle = tokenFontStyle;
		this.tokenName = tokenName != null ? tokenName : "UNDEFINED";
		this.background = background != null ? background : Color.WHITE;
		this.foreground = foreground != null ? foreground : Color.BLACK;
	}

	public String getTokenName() {
		return tokenName;
	}

	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}

	public int getTokenFontStyle() {
		return tokenFontStyle;
	}

	public void setTokenFontStyle(int tokenFontStyle) {
		this.tokenFontStyle = tokenFontStyle;
	}

	public Color getBackground() {
		return background;
	}

	public void setBackground(Color background) {
		this.background = background;
	}

	public Color getForeground() {
		return foreground;
	}

	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}

	@Override
	public String toString() {
		return getTokenName();
	}
	
}
