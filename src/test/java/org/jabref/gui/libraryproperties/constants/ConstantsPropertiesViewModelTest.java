package org.jabref.gui.libraryproperties.constants;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javafx.beans.property.StringProperty;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import org.jabref.gui.ClipBoardManager;
import org.jabref.gui.DialogService;
import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibEntryType;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.model.entry.BibtexString;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.preferences.FilePreferences;
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

class ConstantsPropertiesViewModelTest {

    private DialogService service;
    private FilePreferences filePreferences;
    private PreferencesService preferencesService;
    private FieldPreferences fieldPreferences;
    private BibEntryTypesManager entryTypesManager;
    private Clipboard clipboard;

    @BeforeEach
    void setUp() {
        service = mock(DialogService.class);
        filePreferences = mock(FilePreferences.class);
        preferencesService = mock(PreferencesService.class);
        fieldPreferences = mock(FieldPreferences.class);

        List<Field> test = Arrays.asList(StandardField.URL);
        ObservableList<Field> nonWrappableFields = FXCollections.observableArrayList(test);
        when(fieldPreferences.getNonWrappableFields()).thenReturn(nonWrappableFields);
        when(preferencesService.getFieldPreferences()).thenReturn(fieldPreferences);

        // create mock clipboard
        clipboard = mock(Clipboard.class);

        entryTypesManager = mock(BibEntryTypesManager.class);
        BibEntryType entryTypeMock = mock(BibEntryType.class);
        when(entryTypesManager.enrich(any(), any())).thenReturn(Optional.of(entryTypeMock));
    }

    @DisplayName("Check that the list of strings is sorted according to their keys")
    @Test
    void stringsListPropertySorting() {
        BibtexString string1 = new BibtexString("TSE", "Transactions on Software Engineering");
        BibtexString string2 = new BibtexString("ICSE", "International Conference on Software Engineering");
        BibDatabase db = new BibDatabase();
        db.setStrings(List.of(string1, string2));
        BibDatabaseContext context = new BibDatabaseContext(db);
        List<String> expected = List.of(string2.getName(), string1.getName()); // ICSE before TSE

        ConstantsPropertiesViewModel model = new ConstantsPropertiesViewModel(context, service, filePreferences);
        model.setValues();

        List<String> actual = model.stringsListProperty().stream()
                .map(ConstantsItemModel::labelProperty)
                .map(StringProperty::getValue)
                .toList();

        assertEquals(expected, actual);
    }

    @DisplayName("Check that the list of strings is sorted after resorting it")
    @Test
    void stringsListPropertyResorting() {
        BibDatabase db = new BibDatabase();
        BibDatabaseContext context = new BibDatabaseContext(db);
        List<String> expected = List.of("ICSE", "TSE");

        ConstantsPropertiesViewModel model = new ConstantsPropertiesViewModel(context, service, filePreferences);
        var stringsList = model.stringsListProperty();
        stringsList.add(new ConstantsItemModel("TSE", "Transactions on Software Engineering"));
        stringsList.add(new ConstantsItemModel("ICSE", "International Conference on Software Engineering"));

        model.resortStrings();

        List<String> actual = model.stringsListProperty().stream()
                .map(ConstantsItemModel::labelProperty)
                .map(StringProperty::getValue)
                .toList();

        assertEquals(expected, actual);
    }

   @Test
   void storeSettingsTest() {
       // Setup
       BibDatabase db = new BibDatabase();
       BibDatabaseContext context = new BibDatabaseContext(db);
       List<String> expected = List.of("KTH", "Royal Institute of Technology");

       ConstantsPropertiesViewModel model = new ConstantsPropertiesViewModel(context, service, filePreferences);

       var stringsList = model.stringsListProperty();
       stringsList.add(new ConstantsItemModel("KTH", "Royal Institute of Technology"));

       // Act
       model.storeSettings();

       // Assert
       List<String> names = context.getDatabase().getStringValues().stream()
                                    .map(BibtexString::getName).toList();

       List<String> content = context.getDatabase().getStringValues().stream()
                                          .map(BibtexString::getContent).toList();

       List<String> actual = Stream.concat(names.stream(), content.stream()).toList();

       assertEquals(actual, expected);
   }

   @Test
   void testCopyStringBibEntry() {
       // Arrange
       String expected = "@Article{,\n author = {Claudepierre, S. G.},\n journal = {IEEE},\n}";
       // create clipboards and set temporary value
       StringSelection selection = new StringSelection("test");

       java.awt.datatransfer.Clipboard clipboardAlternative = Toolkit.getDefaultToolkit().getSystemClipboard();
       clipboardAlternative.setContents(selection, selection);

       ClipBoardManager clipBoardManager = new ClipBoardManager(clipboard, clipboardAlternative, preferencesService);

       // create BibEntry
       BibEntry bibEntry = new BibEntry();
       // construct an entry
       bibEntry.setType(StandardEntryType.Article);
       bibEntry.setField(StandardField.JOURNAL, "IEEE");
       bibEntry.setField(StandardField.AUTHOR, "Claudepierre, S. G.");

       List<BibEntry> bibEntries = new ArrayList<>();
       bibEntries.add(bibEntry);

       // Act
       try {
           clipBoardManager.setContent(bibEntries, entryTypesManager);
       } catch (Exception e) {
           e.printStackTrace(System.out);
       }

       // Assert
       String actual = clipBoardManager.getContentsPrimary();
       assertNotEquals("test", StringUtils.normalizeSpace(actual));
       assertEquals(StringUtils.normalizeSpace(expected), StringUtils.normalizeSpace(actual));
   }

    @Test
    void testCopyStringBibEntryWithStringConstants() {
        // Arrange
        String expected = "@String{grl = \"Geophys. Res. Lett.\"}@Article{,\n" + " author = {Claudepierre, S. G.},\n" + " journal = {grl},\n" + "}";
        // create clipboards and set temporary value
        ClipboardContent content = new ClipboardContent();
        content.putString("test");

        StringSelection selection = new StringSelection("test");

        java.awt.datatransfer.Clipboard clipboardAlternative = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboardAlternative.setContents(selection, selection);

        ClipBoardManager clipBoardManager = new ClipBoardManager(clipboard, clipboardAlternative, preferencesService);

        // create BibEntry
        BibEntry bibEntry = new BibEntry();
        // construct an entry
        bibEntry.setType(StandardEntryType.Article);
        bibEntry.setField(StandardField.JOURNAL, "grl");
        bibEntry.setField(StandardField.AUTHOR, "Claudepierre, S. G.");

        List<BibEntry> bibEntries = new ArrayList<>();
        bibEntries.add(bibEntry);

        // string constants
        List<BibtexString> constants = new ArrayList<>();

        // Mock BibtexString
        BibtexString bibtexString = mock(BibtexString.class);

        // define return value for getParsedSerialization()
        when(bibtexString.getParsedSerialization()).thenReturn("@String{grl = \"Geophys. Res. Lett.\"}");

        constants.add(bibtexString);

        // Act
        try {
            clipBoardManager.setContent(bibEntries, entryTypesManager, constants);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        // Assert
        String actual = clipBoardManager.getContentsPrimary();
        assertNotEquals("test", StringUtils.normalizeSpace(actual));
        assertEquals(StringUtils.normalizeSpace(expected), StringUtils.normalizeSpace(actual));
    }
}
