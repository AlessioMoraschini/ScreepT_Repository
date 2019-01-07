package gUI.om;

import java.awt.image.BufferedImage;

public enum BufferedImageType {THREE_BYTE_BGR("3BYTE RGB", BufferedImage.TYPE_3BYTE_BGR,3, "Represents an image with 8-bit RGB color components, corresponding to a Windows-style BGR color model) with the colors Blue, Green, and Red stored in 3 bytes. There is no alpha. When data with non-opaque alpha is stored in an image of this type"), 
							   FOUR_BYTE_ABGR_PRE("4BYTE ABGR (Pre-multiplied)", BufferedImage.TYPE_4BYTE_ABGR_PRE,4, "Represents an image with 8-bit RGBA color components with the colors Blue, Green, and Red stored in 3 bytes and 1 byte of alpha. The image has a ComponentColorModel with alpha."), 
							   FOUR_BYTE_ABGR("4 BYTE ABGR", BufferedImage.TYPE_4BYTE_ABGR,4, "Represents an image with 8-bit RGBA color components with the colors Blue, Green, and Red stored in 3 bytes and 1 byte of alpha. The image has a ComponentColorModel with alpha."),
							   INT_ABGR("INT ARGB", BufferedImage.TYPE_INT_ARGB,4, "Represents an image with 8-bit RGBA color components packed into integer pixels. The image has a DirectColorModel with alpha."),
							   INT_ARGB_PRE("INT ARGB (Pre-multiplied)", BufferedImage.TYPE_INT_ARGB_PRE,4, "Represents an image with 8-bit RGBA color components packed into integer pixels. The image has a DirectColorModel with alpha. "),
							   INT_RGB("INT RGB", BufferedImage.TYPE_INT_RGB,3,"Represents an image with 8-bit RGB color components packed into integer pixels. The image has a DirectColorModel without alpha. " ),
							   INT_BGR("INT BGR", BufferedImage.TYPE_INT_BGR,3, "Represents an image with 8-bit RGB color components, corresponding to a Windows- or Solaris- style BGR color model, with the colors Blue, Green, and Red packed into integer pixels. There is no alpha"),
							   USHORT_555_RGB("USHORT_555_RGB", BufferedImage.TYPE_USHORT_555_RGB,2, "Represents an image with 5-5-5 RGB color components (5-bits red, 5-bits green, 5-bits blue) with no alpha. This image has a DirectColorModel. "),
							   USHORT_565_RGB("USHORT_565_RGB", BufferedImage.TYPE_USHORT_565_RGB,2, "Represents an image with 5-6-5 RGB color components (5-bits red, 6-bits green, 5-bits blue) with no alpha. This image has a DirectColorModel. ");
							   
	public int codice;
	public int multiplicator;
	public String nomeStringa;
	public String description;
	
	BufferedImageType(String nome, int codice, int mult, String descr) {
		this.nomeStringa = descr;
		this.codice = codice;
		this.nomeStringa = nome;
		multiplicator=mult;
		description = descr;
	}

	public int getCodice() {
		return codice;
	}

	public void setCodice(int codice) {
		this.codice = codice;
	}

	public int getMultiplicator() {
		return multiplicator;
	}

	public void setMultiplicator(int multiplicator) {
		this.multiplicator = multiplicator;
	}

	public String getNomeStringa() {
		return nomeStringa;
	}

	public void setNomeStringa(String nomeStringa) {
		this.nomeStringa = nomeStringa;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
