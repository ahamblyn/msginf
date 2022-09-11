package nz.co.pukeko.msginf.infrastructure.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipBoard implements ClipboardOwner {
	
	public ClipBoard() {
	}
	
	public void setClipboardContents(String aString) {
		StringSelection stringSelection = new StringSelection(aString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	public String getClipboardContents() {
	    String result = "";
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    //odd: the Object param of getContents is not currently used
	    Transferable contents = clipboard.getContents(null);
	    boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
	    if ( hasTransferableText ) {
	      try {
	        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
	      } catch (UnsupportedFlavorException ex) {
	        System.out.println(ex);
	      } catch (IOException ex) {
	        System.out.println(ex);
	      }
	    }
	    return result;
	}
	
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
}