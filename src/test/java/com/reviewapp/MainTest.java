package com.reviewapp;

import org.junit.jupiter.api.Test
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void mainRunsWithoutError() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }

    @Test
    void main_parsingRequired_branch() {
        try (MockedStatic<com.reviewapp.boot.bootstrap.FileStalenessChecker> stalenessMock = Mockito.mockStatic(com.reviewapp.boot.bootstrap.FileStalenessChecker.class);
             MockedStatic<com.reviewapp.adapter.ingest.JsonParser> parserMock = Mockito.mockStatic(com.reviewapp.adapter.ingest.JsonParser.class)) {
            stalenessMock.when(() -> com.reviewapp.boot.bootstrap.FileStalenessChecker.isParsingRequiredFromJson(Mockito.anyString())).thenReturn(true);
            parserMock.when(() -> com.reviewapp.adapter.ingest.JsonParser.parseReviewsFromFile(Mockito.anyString())).thenReturn(Collections.emptyList());
            assertDoesNotThrow(() -> Main.main(new String[]{}));
        }
    }

    @Test
    void main_existingDatastore_branch() {
        try (MockedStatic<com.reviewapp.boot.bootstrap.FileStalenessChecker> stalenessMock = Mockito.mockStatic(com.reviewapp.boot.bootstrap.FileStalenessChecker.class)) {
            stalenessMock.when(() -> com.reviewapp.boot.bootstrap.FileStalenessChecker.isParsingRequiredFromJson(Mockito.anyString())).thenReturn(false);
            assertDoesNotThrow(() -> Main.main(new String[]{}));
        }
    }

    @Test
    void main_jsonParserThrows_exceptionHandled() {
        try (MockedStatic<com.reviewapp.boot.bootstrap.FileStalenessChecker> stalenessMock = Mockito.mockStatic(com.reviewapp.boot.bootstrap.FileStalenessChecker.class);
             MockedStatic<com.reviewapp.adapter.ingest.JsonParser> parserMock = Mockito.mockStatic(com.reviewapp.adapter.ingest.JsonParser.class)) {
            stalenessMock.when(() -> com.reviewapp.boot.bootstrap.FileStalenessChecker.isParsingRequiredFromJson(Mockito.anyString())).thenReturn(true);
            parserMock.when(() -> com.reviewapp.adapter.ingest.JsonParser.parseReviewsFromFile(Mockito.anyString())).thenThrow(new RuntimeException("parse fail"));
            assertDoesNotThrow(() -> Main.main(new String[]{}));
        }
    }

    @Test
    void main_reviewServiceThrows_exceptionHandled() {
        try (MockedStatic<com.reviewapp.boot.bootstrap.FileStalenessChecker> stalenessMock = Mockito.mockStatic(com.reviewapp.boot.bootstrap.FileStalenessChecker.class);
             MockedStatic<com.reviewapp.adapter.ingest.JsonParser> parserMock = Mockito.mockStatic(com.reviewapp.adapter.ingest.JsonParser.class)) {
            stalenessMock.when(() -> com.reviewapp.boot.bootstrap.FileStalenessChecker.isParsingRequiredFromJson(Mockito.anyString())).thenReturn(true);
            parserMock.when(() -> com.reviewapp.adapter.ingest.JsonParser.parseReviewsFromFile(Mockito.anyString())).thenReturn(List.of(Mockito.mock(com.reviewapp.domain.model.Review.class)));
            MockedStatic<com.reviewapp.boot.factory.ReviewRepositoryFactory> factoryMock = Mockito.mockStatic(com.reviewapp.boot.factory.ReviewRepositoryFactory.class);
            com.reviewapp.boot.factory.ReviewRepositoryFactory.RepositoryBundle bundle = Mockito.mock(com.reviewapp.boot.factory.ReviewRepositoryFactory.RepositoryBundle.class);
            Mockito.when(bundle.query()).thenReturn(Mockito.mock(com.reviewapp.domain.port.ReviewQueryPort.class));
            Mockito.when(bundle.write()).thenReturn(new com.reviewapp.domain.port.ReviewWritePort() {
                @Override public void saveReviews(List<com.reviewapp.domain.model.Review> reviews) { throw new RuntimeException("save fail"); }
            });
            Mockito.when(bundle.stats()).thenReturn(Mockito.mock(com.reviewapp.domain.port.ReviewStatsPort.class));
            factoryMock.when(() -> com.reviewapp.boot.factory.ReviewRepositoryFactory.create(Mockito.any())).thenReturn(bundle);
            assertDoesNotThrow(() -> Main.main(new String[]{}));
            factoryMock.close();
        }
    }
}
