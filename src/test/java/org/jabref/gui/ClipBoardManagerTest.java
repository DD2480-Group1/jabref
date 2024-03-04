package org.jabref.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.Clipboard;

import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibEntryType;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.model.entry.BibtexString;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.preferences.PreferencesService;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClipBoardManagerTest {

    private BibEntryTypesManager entryTypesManager;
    private ClipBoardManager clipBoardManager;

    @BeforeEach
    void setUp() {
        // create preference service mock
        PreferencesService preferencesService = mock(PreferencesService.class);
        FieldPreferences fieldPreferences = mock(FieldPreferences.class);
        List<Field> fields = Arrays.asList(StandardField.URL);
        ObservableList<Field> nonWrappableFields = FXCollections.observableArrayList(fields);
        // set up mock behaviours for preferences service
        when(fieldPreferences.getNonWrappableFields()).thenReturn(nonWrappableFields);
        when(preferencesService.getFieldPreferences()).thenReturn(fieldPreferences);

        // create mock clipboard
        Clipboard clipboard = mock(Clipboard.class);
        // create primary clipboard and set a temporary value
        StringSelection selection = new StringSelection("test");
        java.awt.datatransfer.Clipboard clipboardPrimary = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboardPrimary.setContents(selection, selection);

        // create mock entry manager and set up behaviour for mock
        entryTypesManager = mock(BibEntryTypesManager.class);
        BibEntryType entryTypeMock = mock(BibEntryType.class);
        when(entryTypesManager.enrich(any(), any())).thenReturn(Optional.of(entryTypeMock));
        // initialize a clipBoardManager
        clipBoardManager = new ClipBoardManager(clipboard, clipboardPrimary, preferencesService);
    }

    @DisplayName("Check that the ClipBoardManager can set a bibentry as its content from the clipboard")
    @Test
    void testCopyStringBibEntry() {
        // Arrange
        String expected = "@Article{,\n author = {Claudepierre, S. G.},\n journal = {IEEE},\n}";

        // create BibEntry
        BibEntry bibEntry = new BibEntry();
        // construct an entry
        bibEntry.setType(StandardEntryType.Article);
        bibEntry.setField(StandardField.JOURNAL, "IEEE");
        bibEntry.setField(StandardField.AUTHOR, "Claudepierre, S. G.");
        // add entry to list
        List<BibEntry> bibEntries = new ArrayList<>();
        bibEntries.add(bibEntry);

        // Act
        try {
            clipBoardManager.setContent(bibEntries, entryTypesManager);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        // Assert
        String actual = ClipBoardManager.getContentsPrimary();
        assertNotEquals("test", StringUtils.normalizeSpace(actual));
        assertEquals(StringUtils.normalizeSpace(expected), StringUtils.normalizeSpace(actual));
    }

    @Test
    @DisplayName("Check that the ClipBoardManager can handle a bibentry with string constants correctly from the clipboard")
    void testCopyStringBibEntryWithStringConstants() {
        // Arrange
        String expected = "@String{grl = \"Geophys. Res. Lett.\"}@Article{,\n" + " author = {Claudepierre, S. G.},\n" +
                " journal = {grl},\n" + "}";
        // create BibEntry
        BibEntry bibEntry = new BibEntry();
        // construct an entry
        bibEntry.setType(StandardEntryType.Article);
        bibEntry.setField(StandardField.JOURNAL, "grl");
        bibEntry.setField(StandardField.AUTHOR, "Claudepierre, S. G.");
        // add entry to list
        List<BibEntry> bibEntries = new ArrayList<>();
        bibEntries.add(bibEntry);

        // string constants
        List<BibtexString> constants = new ArrayList<>();

        // Mock BibtexString
        BibtexString bibtexString = mock(BibtexString.class);

        // define return value for getParsedSerialization()
        when(bibtexString.getParsedSerialization()).thenReturn("@String{grl = \"Geophys. Res. Lett.\"}");
        // add the constant
        constants.add(bibtexString);

        // Act
        try {
            clipBoardManager.setContent(bibEntries, entryTypesManager, constants);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        // Assert
        String actual = ClipBoardManager.getContentsPrimary();
        assertNotEquals("test", StringUtils.normalizeSpace(actual));
        assertEquals(StringUtils.normalizeSpace(expected), StringUtils.normalizeSpace(actual));
    }
}
