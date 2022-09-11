package nz.govt.nzqa.emi.infrastructure.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import nz.govt.nzqa.emi.infrastructure.util.Util;

public class XMLFileFilter extends FileFilter {

	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		String extension = Util.getExtension(file);
		if (extension != null) {
			if (extension.equals("xml")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public String getDescription() {
		return "XML Files";
	}
}
