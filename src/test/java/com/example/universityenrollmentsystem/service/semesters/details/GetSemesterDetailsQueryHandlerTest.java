package com.example.universityenrollmentsystem.service.semesters.details;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSemesterDetailsQueryHandlerTest {

    private static final Long SEMESTER_ID = 1L;
    private static final String TITLE = "Fall 2026";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 23);
    private static final LocalDate END_DATE = LocalDate.of(2027, 1, 20);

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private GetSemesterDetailsQueryValidator validator;

    @InjectMocks
    private GetSemesterDetailsQueryHandler handler;

    private Semester semester;

    @BeforeEach
    void setUp() {
        semester = new Semester(TITLE, START_DATE, END_DATE);
        ReflectionTestUtils.setField(semester, "id", SEMESTER_ID);
    }

    @Test
    @DisplayName("Should return semester details successfully")
    void shouldReturnSemesterDetailsSuccessfully() {
        GetSemesterDetailsQuery query = new GetSemesterDetailsQuery(SEMESTER_ID);
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));

        GetSemesterDetailsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.semesterId()).isEqualTo(SEMESTER_ID);
        assertThat(response.title()).isEqualTo(TITLE);
        assertThat(response.startDate()).isEqualTo(START_DATE);
        assertThat(response.endDate()).isEqualTo(END_DATE);
        assertThat(response.isDeleted()).isFalse();

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw SemesterNotFoundException when semester is not found")
    void shouldThrowExceptionWhenSemesterNotFound() {
        GetSemesterDetailsQuery query = new GetSemesterDetailsQuery(SEMESTER_ID);
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(SemesterNotFoundException.class);

        verify(validator).validate(query);
    }
}
