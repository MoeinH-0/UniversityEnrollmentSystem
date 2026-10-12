package com.example.universityenrollmentsystem.service.semesters.reschedule;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RescheduleSemesterCommandHandlerTest {

    private static final Long SEMESTER_ID = 1L;
    private static final LocalDate OLD_START = LocalDate.of(2026, 9, 23);
    private static final LocalDate OLD_END = LocalDate.of(2027, 1, 20);
    private static final LocalDate NEW_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate NEW_END = LocalDate.of(2027, 2, 1);

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private RescheduleSemesterCommandValidator validator;

    @InjectMocks
    private RescheduleSemesterCommandHandler handler;

    private Semester semester;

    @BeforeEach
    void setUp() {
        semester = new Semester("Fall 2026", OLD_START, OLD_END);
    }

    @Test
    @DisplayName("Should reschedule semester successfully")
    void shouldRescheduleSemesterSuccessfully() {
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(SEMESTER_ID, NEW_START, NEW_END);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));

        handler.handle(command);

        assertThat(semester.getStartDate()).isEqualTo(NEW_START);
        assertThat(semester.getEndDate()).isEqualTo(NEW_END);
        verify(validator).validate(command);
        verify(semesterRepository).save(semester);
    }

    @Test
    @DisplayName("Should throw SemesterNotFoundException when semester is not found")
    void shouldThrowExceptionWhenSemesterNotFound() {
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(SEMESTER_ID, NEW_START, NEW_END);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(SemesterNotFoundException.class);

        verify(validator).validate(command);
        verify(semesterRepository, never()).save(any());
    }
}
