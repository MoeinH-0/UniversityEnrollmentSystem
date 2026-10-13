package com.example.universityenrollmentsystem.repository.academic;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SemesterRepositoryTest {

    private static final String TITLE = "Fall 2026";
    private static final String NON_EXISTING_TITLE = "Spring 2099";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 23);
    private static final LocalDate END_DATE = LocalDate.of(2027, 1, 20);
    private static final LocalDate RESCHEDULED_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate RESCHEDULED_END = LocalDate.of(2027, 2, 1);

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    @DisplayName("Should return true when semester title exists in database")
    void shouldReturnTrueWhenSemesterTitleExists() {
        Semester semester = new Semester(TITLE, START_DATE, END_DATE);
        semesterRepository.saveAndFlush(semester);

        boolean exists = semesterRepository.existsByTitle(TITLE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when semester title does not exist in database")
    void shouldReturnFalseWhenSemesterTitleDoesNotExist() {
        boolean exists = semesterRepository.existsByTitle(NON_EXISTING_TITLE);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when inserting duplicate semester title")
    void shouldThrowExceptionWhenInsertingDuplicateSemesterTitle() {
        Semester semester1 = new Semester(TITLE, START_DATE, END_DATE);
        semesterRepository.saveAndFlush(semester1);

        Semester semester2 = new Semester(TITLE, START_DATE.plusDays(1), END_DATE.plusDays(1));

        assertThatThrownBy(() -> semesterRepository.saveAndFlush(semester2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should return true for existsByTitle even when semester is soft deleted")
    void shouldReturnTrueForExistsByTitleEvenWhenSemesterIsSoftDeleted() {
        Semester semester = new Semester(TITLE, START_DATE, END_DATE);
        semester.markAsDeleted();
        semesterRepository.saveAndFlush(semester);

        boolean exists = semesterRepository.existsByTitle(TITLE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should persist rescheduled semester dates correctly")
    void shouldPersistRescheduledDates() {
        Semester semester = new Semester(TITLE, START_DATE, END_DATE);
        Semester saved = semesterRepository.saveAndFlush(semester);

        saved.reschedule(RESCHEDULED_START, RESCHEDULED_END);
        semesterRepository.saveAndFlush(saved);

        Optional<Semester> updated = semesterRepository.findById(saved.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getStartDate()).isEqualTo(RESCHEDULED_START);
        assertThat(updated.get().getEndDate()).isEqualTo(RESCHEDULED_END);
    }

    @Test
    @DisplayName("Should persist soft delete state when semester is marked as deleted")
    void shouldPersistSoftDeleteState() {
        Semester semester = new Semester(TITLE, START_DATE, END_DATE);
        semester.markAsDeleted();
        Semester saved = semesterRepository.saveAndFlush(semester);

        Optional<Semester> found = semesterRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
    }
}
