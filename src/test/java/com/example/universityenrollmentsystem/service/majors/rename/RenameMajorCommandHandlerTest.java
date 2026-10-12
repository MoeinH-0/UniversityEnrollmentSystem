package com.example.universityenrollmentsystem.service.majors.rename;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.MajorAlreadyExistsException;
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
class RenameMajorCommandHandlerTest {

    private static final Long MAJOR_ID = 1L;
    private static final String OLD_NAME = "Computer Engineering";
    private static final String NEW_NAME = "Computer Science";

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private RenameMajorCommandValidator validator;

    @InjectMocks
    private RenameMajorCommandHandler handler;

    private Major major;

    @BeforeEach
    void setUp() {
        major = new Major(OLD_NAME);
    }

    @Test
    @DisplayName("Should rename major successfully when new name is not taken")
    void shouldRenameMajorSuccessfully() {
        RenameMajorCommand command = new RenameMajorCommand(MAJOR_ID, NEW_NAME);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));
        when(majorRepository.existsByName(NEW_NAME)).thenReturn(false);

        handler.handle(command);

        assertThat(major.getName()).isEqualTo(NEW_NAME);
        verify(validator).validate(command);
        verify(majorRepository).save(major);
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when major is not found")
    void shouldThrowExceptionWhenMajorNotFound() {
        RenameMajorCommand command = new RenameMajorCommand(MAJOR_ID, NEW_NAME);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorNotFoundException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw MajorAlreadyExistsException when new name already exists")
    void shouldThrowExceptionWhenNewNameAlreadyExists() {
        RenameMajorCommand command = new RenameMajorCommand(MAJOR_ID, NEW_NAME);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));
        when(majorRepository.existsByName(NEW_NAME)).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorAlreadyExistsException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }
}
