package com.example.universityenrollmentsystem.service.semesters.archive;

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
class ArchiveSemesterCommandHandlerTest {

    private static final Long SEMESTER_ID = 1L;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private ArchiveSemesterCommandValidator validator;

    @InjectMocks
    private ArchiveSemesterCommandHandler handler;

    private Semester semester;

    @BeforeEach
    void setUp() {
        semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
    }

    @Test
    @DisplayName("Should archive semester successfully and mark as deleted")
    void shouldArchiveSemesterSuccessfully() {
        ArchiveSemesterCommand command = new ArchiveSemesterCommand(SEMESTER_ID);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));

        handler.handle(command);

        assertThat(semester.isDeleted()).isTrue();
        verify(validator).validate(command);
        verify(semesterRepository).save(semester);
    }

    @Test
    @DisplayName("Should throw SemesterNotFoundException when semester is not found")
    void shouldThrowExceptionWhenSemesterNotFound() {
        ArchiveSemesterCommand command = new ArchiveSemesterCommand(SEMESTER_ID);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(SemesterNotFoundException.class);

        verify(validator).validate(command);
        verify(semesterRepository, never()).save(any());
    }
}
