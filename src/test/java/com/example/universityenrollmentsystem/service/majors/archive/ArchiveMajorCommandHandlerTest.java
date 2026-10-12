package com.example.universityenrollmentsystem.service.majors.archive;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveMajorCommandHandlerTest {

    private static final Long MAJOR_ID = 1L;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private ArchiveMajorCommandValidator validator;

    @InjectMocks
    private ArchiveMajorCommandHandler handler;

    private Major major;

    @BeforeEach
    void setUp() {
        major = new Major("Computer Engineering");
    }

    @Test
    @DisplayName("Should archive major successfully and set isDeleted to true")
    void shouldArchiveMajorSuccessfully() {
        ArchiveMajorCommand command = new ArchiveMajorCommand(MAJOR_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));

        handler.handle(command);

        assertThat(major.isDeleted()).isTrue();
        verify(validator).validate(command);
        verify(majorRepository).save(major);
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when major is not found")
    void shouldThrowExceptionWhenMajorNotFound() {
        ArchiveMajorCommand command = new ArchiveMajorCommand(MAJOR_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorNotFoundException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }
}
