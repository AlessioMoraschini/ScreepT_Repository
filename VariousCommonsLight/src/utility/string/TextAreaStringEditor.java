package utility.string;

import javax.swing.JTextArea;

import om.SelectionDTO;
import om.SelectionDtoFull;
import utility.SafeLogger;

public class TextAreaStringEditor {
	
	private static SafeLogger logger = new SafeLogger(TextAreaStringEditor.class);
	
	public static void makeUppercasePostDotsNSeparators(JTextArea textArea) {
		
		SelectionDtoFull txtAreaDto = new SelectionDtoFull(textArea);
		SelectionDTO result = getUppercasePostDotsNSeparators(txtAreaDto);

		TextAreaStringEditor.applySelectionDTO(result, textArea);
	}

	public static SelectionDTO getUppercasePostDotsNSeparators(SelectionDtoFull selectionDtoFull) {
		SelectionDTO result = new SelectionDTO(selectionDtoFull.first, selectionDtoFull.last, "");

		if (selectionDtoFull.selected) {
			result.text = selectionDtoFull.text
					.concat(StringWorker.upperAfterDots(selectionDtoFull.selection, '.').concat(selectionDtoFull.end));
		} else {
			result.text = StringWorker.upperAfterDots(selectionDtoFull.getCompleteString(), '.');
		}
		
		return result;
	}

	public static void makeLowercasePostDotsNSeparators(JTextArea textArea) {
		
		SelectionDtoFull txtAreaDto = new SelectionDtoFull(textArea);
		SelectionDTO result = getLowercasePostDotsNSeparators(txtAreaDto);

		TextAreaStringEditor.applySelectionDTO(result, textArea);
	}
	
	public static SelectionDTO getLowercasePostDotsNSeparators(SelectionDtoFull selectionDtoFull) {
		SelectionDTO result = new SelectionDTO(selectionDtoFull.first, selectionDtoFull.last, "");
		
		if (selectionDtoFull.selected) {
		result.text = result.text
				.concat(StringWorker.lowerAfterDots(selectionDtoFull.selection, '.').concat(selectionDtoFull.end));
		} else {
			result.text = StringWorker.lowerAfterDots(selectionDtoFull.getCompleteString(), '.');
		}

		return result;
	}
	
	public static void makeWordsUppercase(JTextArea textArea) {
		SelectionDTO result = getWordsUppercase(new SelectionDtoFull(textArea));
		applySelectionDTO(result, textArea);
	}
	
	public static SelectionDTO getWordsUppercase(SelectionDtoFull selectionDtoFull) {
		String textBackup = new String(selectionDtoFull.text);
		String selected = selectionDtoFull.selection;
		String start = "";
		String end = "";
		int strtN = 0;
		int endN = textBackup.length();
		if ("".equals(selected)) {
			// select ALL if no selection
			selected = textBackup;
		} else {
			strtN = selectionDtoFull.first;
			endN = selectionDtoFull.last;
			start = textBackup.substring(0, strtN);
			end = textBackup.substring(endN, textBackup.length());
		}
		String backupText = new String(selected);// create backup copy by value

		selected = StringWorker.uppercaseEveryFirstLetterNoSpace(selected);
		if (selected.equals(backupText)) {
			selected = StringWorker.lowerCaseEveryFirstLetterNoSpace(selected);
		}
		
		selected = start + selected + end;
		
		logger.debug("Tt Tt function start selection: " + strtN);
		logger.debug("Tt Tt function end selection: " + endN);
		
		return new SelectionDTO(strtN, endN, selected);
	}
	
	public static void makeToggleUppercase(JTextArea textArea, boolean isUppercase) {
		SelectionDTO result = getToggleUppercase(new SelectionDtoFull(textArea), isUppercase);
		applySelectionDTO(result, textArea);
	}
	
	public static SelectionDTO getToggleUppercase(SelectionDtoFull selectionDtoFull, boolean isUppercase) {
		String text = selectionDtoFull.text;
		String textBackup = new String(text);
		String selected = selectionDtoFull.selection;
		String start = "";
		String end = "";
		int strtN = 0;
		int endN = text.length();
		if ("".equals(selected)) {
			// select ALL if no selection
			selected = new String(text);
		} else {
			strtN = selectionDtoFull.first;
			endN = selectionDtoFull.last;
			start = textBackup.substring(0, strtN);
			end = textBackup.substring(endN, textBackup.length());
		}
		
		if (!isUppercase) {
			selected = selected.toUpperCase();
			isUppercase = true;
		} else {
			selected = selected.toLowerCase();
			isUppercase = false;
		}

		selected = start + selected + end;
		
		logger.debug("TT function start selection: " + strtN);
		logger.debug("TT function start selection: " + endN);
		
		return new SelectionDTO(strtN, endN, selected, selected);
	}

	public static void applySelectionDTO(SelectionDTO result, JTextArea textArea) {
		if (result != null) {
			textArea.setText(result.text);
			textArea.requestFocus();
			textArea.setSelectionStart(result.start);
			textArea.setSelectionEnd(result.end);
		}
	}
}
