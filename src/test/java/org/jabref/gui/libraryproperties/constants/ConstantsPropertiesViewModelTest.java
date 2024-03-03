package org.jabref.gui.libraryproperties.constants;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.StringProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import org.jabref.gui.ClipBoardManager;
import org.jabref.gui.DialogService;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibtexString;
import org.jabref.preferences.FilePreferences;
import org.jabref.preferences.PreferencesService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ConstantsPropertiesViewModelTest {

    private DialogService service;
    private FilePreferences filePreferences;
    private PreferencesService preferencesService;

    @BeforeEach
    void setUp() {
        service = mock(DialogService.class);
        filePreferences = mock(FilePreferences.class);
        preferencesService = mock(PreferencesService.class);
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

   /*
   @Test
   void storeSettingStringNull() {
       // Setup
       BibDatabase db = new BibDatabase();
       BibDatabaseContext context = new BibDatabaseContext(db);
       List<String> expected = List.of("KTH", "Royal Institute of Technology");

       ConstantsPropertiesViewModel model = new ConstantsPropertiesViewModel(context, service, filePreferences);

       ClipboardContent content = new ClipboardContent();
       content.putString("@article{claudepierre:20a,\n    author = {Claudepierre, S. G.},\n    journal = grl\n}");
       Clipboard clipboard = mock(Clipboard.class);
       clipboard.setContent(content);

       java.awt.datatransfer.Clipboard test = mock(java.awt.datatransfer.Clipboard.class);

       ClipBoardManager clipBoardManager = new ClipBoardManager(clipboard, test, preferencesService);

       //var stringsList = model.stringsListProperty();
       //stringsList.add(new ConstantsItemModel(null, "Royal Institute of Technology"));

       // Act
       //model.storeSettings();

       System.out.println(clipBoardManager.getContents());

       assertEquals(clipBoardManager.getContents(), "test");
       assertNotNull(clipBoardManager.getContents());
   }
   */
}
