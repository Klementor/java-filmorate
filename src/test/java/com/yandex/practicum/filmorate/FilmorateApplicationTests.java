package com.yandex.practicum.filmorate;

import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import com.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)

@Sql(scripts = {"classpath:/fillSampleDataMargarita.sql"},
		executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApplicationTests {
	private final FilmDbStorage filmDbStorage;
	@Test
	void contextLoads() {
	}

	@Test
	public void testSearchFilmShouldNotThrowError() {
		List<Film> allFilms = filmDbStorage.search("тит",false,true);
		List<String> expectedFilms = List.of("Титаник");

		assertThat(allFilms)
				.isNotEmpty()
				.hasSize(1)
				.extracting(Film::getName)
				.containsExactlyElementsOf(expectedFilms);
	}


}
