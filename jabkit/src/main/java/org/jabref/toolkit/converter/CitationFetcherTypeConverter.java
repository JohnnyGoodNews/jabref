package org.jabref.toolkit.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jabref.logic.importer.fetcher.citation.CitationFetcherType;

import picocli.CommandLine;

public class CitationFetcherTypeConverter extends CaseInsensitiveEnumConverter<CitationFetcherType> {

    /// The function that is used for both domain conversion/lookup and String representation.
    public static final Function<CitationFetcherType, String> STRINGIFIER = Enum::name;
    public static final List<String> FETCHER_NAMES = Arrays.stream(CitationFetcherType.values()).map(STRINGIFIER).toList();

    public CitationFetcherTypeConverter() {
        super(CitationFetcherType.class);
    }

    @Override
    public CitationFetcherType convert(String value) {
        return Arrays.stream(CitationFetcherType.values())
                     .filter(t -> STRINGIFIER.apply(t).equalsIgnoreCase(value))
                     .findFirst()
                     .orElseThrow(() -> new CommandLine.TypeConversionException(
                             "Invalid citation provider. Must be one of: " + FETCHER_NAMES));
    }

    /// Inverse of the convert function: Converts the (enum) domain object to the valid cli argument values.
    public static class FetcherNames extends ArrayList<String> {
        FetcherNames() {
            super(FETCHER_NAMES);
        }
    }
}
