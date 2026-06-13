package org.jabref.toolkit.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jabref.logic.exporter.BibDatabaseWriter;
import org.jabref.logic.exporter.ExportPreferences;
import org.jabref.logic.exporter.SelfContainedSaveConfiguration;
import org.jabref.logic.importer.ParserResult;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.metadata.SaveOrder;
import org.jabref.model.metadata.SelfContainedSaveOrder;
import org.jabref.toolkit.commands.AbstractJabKitTest;
import org.jabref.toolkit.exception.ExportServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ExportServiceTest extends AbstractJabKitTest {

    @BeforeEach
    void setup() {
        SelfContainedSaveOrder selfContainedSaveOrder = new SelfContainedSaveOrder(SaveOrder.OrderType.ORIGINAL, List.of());
        SelfContainedSaveConfiguration selfContainedSaveConfiguration = new SelfContainedSaveConfiguration(selfContainedSaveOrder, false, BibDatabaseWriter.SaveType.WITH_JABREF_META_DATA, false);
        when(preferences.getSelfContainedExportConfiguration()).thenReturn(selfContainedSaveConfiguration);
    }

    @ParameterizedTest
    @CsvSource({"bibtex", "html", "simplehtml", "tablerefs", "oocsv", "hayagrivayaml", "iso690rtf"})
    void differentOutputFormatsExportFile(String format, @TempDir Path tempDir) throws Exception {
        Path source = getClassResourceAsPath("origin.bib").toAbsolutePath();
        ParserResult parserResult = ImportService.importBibTexFile(source, preferences, true);
        Path output = tempDir.resolve("output." + format);

        new ExportService(preferences, false).exportParserResultToFile(parserResult, output, format);

        assertFileExists(output);
    }

    @Test
    void simpleOutputTest(@TempDir Path tempDir) throws Exception {
        Path source = getClassResourceAsPath("origin.bib").toAbsolutePath();
        ParserResult parserResult = ImportService.importBibTexFile(source, preferences, true);
        Path output = tempDir.resolve("output.bibtex");

        new ExportService(preferences, false).exportParserResultToFile(parserResult, output, "bibtex");

        assertTrue(Files.readString(output).contains("Darwin1888"));
    }

    @Test
    void wrongOutputFormatFails(@TempDir Path tempDir) throws Exception {
        Path source = getClassResourceAsPath("origin.bib").toAbsolutePath();
        ParserResult parserResult = ImportService.importBibTexFile(source, preferences, true);
        Path output = tempDir.resolve("output.bibtex");

        String invalidFormat = "Klingon";

        assertThrows(ExportServiceException.class,
                () -> new ExportService(preferences, false)
                        .exportParserResultToFile(parserResult, output, invalidFormat));
    }

    @Test
    void convertBibtexToTableRefsAsBib(@TempDir Path tempDir) throws Exception {
        Path source = getClassResourceAsPath("origin.bib").toAbsolutePath();
        ParserResult parserResult = ImportService.importBibTexFile(source, preferences, true);
        Path outputHtml = tempDir.resolve("output.html").toAbsolutePath();

        SaveOrder saveOrder = new SaveOrder(SaveOrder.OrderType.TABLE, List.of());
        ExportPreferences exportPreferences = new ExportPreferences(".html", tempDir, saveOrder, List.of());
        when(preferences.getExportPreferences()).thenReturn(exportPreferences);

        new ExportService(preferences, false).exportParserResultToFile(parserResult, outputHtml, "tablerefsabsbib");

        assertFileExists(outputHtml);
    }

    @Test
    void savingDatabaseContextPreservesCitationKeys(@TempDir Path tempDir) throws Exception {
        Path source = getClassResourceAsPath("origin.bib").toAbsolutePath();
        ParserResult parserResult = ImportService.importBibTexFile(source, preferences, true);
        BibDatabaseContext databaseContext = parserResult.getDatabaseContext();

        new ExportService(preferences, false)
                .saveDatabaseContext(databaseContext, tempDir.resolve("output.bib"));

        assertFileExists(tempDir.resolve("output.bib"));
        assertTrue(Files.readString(tempDir.resolve("output.bib")).contains("Darwin1888"));
    }

    @Test
    void exportDatabaseContextPreservesCitationKeys(@TempDir Path tempDir) throws Exception {
        Path source = getClassResourceAsPath("origin.bib").toAbsolutePath();
        ParserResult parserResult = ImportService.importBibTexFile(source, preferences, true);
        BibDatabaseContext databaseContext = parserResult.getDatabaseContext();
        new ExportService(preferences, false)
                .exportBibDatabaseContextToFile(databaseContext, databaseContext.getEntries(),
                        tempDir.resolve("output.bib"), "bibtex");

        assertFileExists(tempDir.resolve("output.bib"));
        assertTrue(Files.readString(tempDir.resolve("output.bib")).contains("Darwin1888"));
    }

    @Test
    void exportGeneratesCitationKeys(@TempDir Path tempDir) throws Exception {
        List<BibEntry> entries = List.of(new BibEntry()
                .withField(StandardField.TITLE, "my ﬁrst research")
                .withField(StandardField.DOI, "10.1000/xyz123")
                .withField(StandardField.AUTHOR, "Jane Doe")
                .withField(StandardField.YEAR, "2023")
                .withChanged(true));

        new ExportService(preferences, false)
                .exportEntriesToFile(entries, tempDir.resolve("output.bib"), "bibtex");

        assertFileExists(tempDir.resolve("output.bib"));
        assertTrue(Files.readString(tempDir.resolve("output.bib")).contains("Doe2023"));
    }
}
