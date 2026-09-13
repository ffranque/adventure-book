package com.pictet.adventurebook.adventure;

import com.pictet.adventurebook.adventure.dto.ChooseOptionRequest;
import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.adventure.dto.SectionResponse;
import com.pictet.adventurebook.domain.SectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdventureControllerTest {

    private final AdventureService adventureService = mock(AdventureService.class);
    private final AdventureController adventureController = new AdventureController(adventureService);

    private final SectionResponse beginSection =
            new SectionResponse(1, "You stand at the entrance.", SectionType.BEGIN, List.of());

    @Test
    void beginReturnsBeginningSectionFromService() {
        when(adventureService.begin("book-1")).thenReturn(beginSection);

        SectionResponse result = adventureController.begin("book-1");

        assertThat(result).isEqualTo(beginSection);
    }

    @Test
    void readReturnsRequestedSectionFromService() {
        SectionResponse midSection = new SectionResponse(5, "A fork in the path.", SectionType.NODE, List.of());
        when(adventureService.getSection("book-1", 5)).thenReturn(midSection);

        SectionResponse result = adventureController.read("book-1", 5);

        assertThat(result).isEqualTo(midSection);
    }

    @Test
    void chooseDelegatesOptionIndexAndCurrentHealthToService() {
        PlayResultResponse playResult = new PlayResultResponse(beginSection, 8, "You got hurt", false, false);
        when(adventureService.choose("book-1", 1, 0, 10)).thenReturn(playResult);

        PlayResultResponse result = adventureController.choose(
                "book-1", 1, new ChooseOptionRequest(0, 10));

        assertThat(result).isEqualTo(playResult);
        verify(adventureService).choose("book-1", 1, 0, 10);
    }
}
