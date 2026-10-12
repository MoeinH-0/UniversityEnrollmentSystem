package com.example.universityenrollmentsystem.service.majors.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.MajorAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMajorCommandHandlerTest {

    private static final String MAJOR_NAME = "Computer Engineering";

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private CreateMajorCommandValidator validator;

    @InjectMocks
    private CreateMajorCommandHandler handler;

    private CreateMajorCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateMajorCommand(MAJOR_NAME);
    }

    @Test
    @DisplayName("Should create major successfully when name is not duplicated")
    void shouldCreateMajorSuccessfully() {
        when(majorRepository.existsByName(MAJOR_NAME)).thenReturn(false);

        handler.handle(command);

        verify(validator).validate(command);
        verify(majorRepository).save(any(Major.class));
    }

    @Test
    @DisplayName("Should throw MajorAlreadyExistsException when major name already exists")
    void shouldThrowExceptionWhenNameAlreadyExists() {
        when(majorRepository.existsByName(MAJOR_NAME)).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorAlreadyExistsException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }
}
