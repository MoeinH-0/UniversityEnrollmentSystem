package com.example.universityenrollmentsystem.service.semesters.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.SemesterAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSemesterCommandHandlerTest {

    private static final String TITLE = "Fall 2026";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 23);
    private static final LocalDate END_DATE = LocalDate.of(2027, 1, 20);

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private CreateSemesterCommandValidator validator;

    @InjectMocks
    private CreateSemesterCommandHandler handler;

    private CreateSemesterCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateSemesterCommand(TITLE, START_DATE, END_DATE);
    }

    @Test
    @DisplayName("Should create semester successfully when title does not exist")
    void shouldCreateSemesterSuccessfully() {
        when(semesterRepository.existsByTitle(TITLE)).thenReturn(false);

        handler.handle(command);

        verify(validator).validate(command);
        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    @DisplayName("Should throw SemesterAlreadyExistsException when semester title already exists")
    void shouldThrowExceptionWhenTitleAlreadyExists() {
        when(semesterRepository.existsByTitle(TITLE)).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(SemesterAlreadyExistsException.class);

        verify(validator).validate(command);
        verify(semesterRepository, never()).save(any());
    }
}
