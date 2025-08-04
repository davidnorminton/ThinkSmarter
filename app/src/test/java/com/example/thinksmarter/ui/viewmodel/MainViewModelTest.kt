package com.example.thinksmarter.ui.viewmodel

import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import com.example.thinksmarter.domain.repository.AnswerEvaluation
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var mockRepository: ThinkSmarterRepository

    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)
        coEvery { mockRepository.getAllQuestionsWithAnswers() } returns MutableStateFlow(emptyList())
        coEvery { mockRepository.getDifficultyLevel() } returns 5
        
        viewModel = MainViewModel(mockRepository)
    }

    @Test
    fun `handleEvent UpdateUserAnswer updates userAnswer`() {
        // Given
        val newAnswer = "This is my answer"

        // When
        viewModel.handleEvent(MainUiEvent.UpdateUserAnswer(newAnswer))

        // Then
        assertEquals(newAnswer, viewModel.uiState.value.userAnswer)
    }

    @Test
    fun `handleEvent ClearError clears error`() {
        // Given
        viewModel.handleEvent(MainUiEvent.UpdateUserAnswer(""))
        viewModel.handleEvent(MainUiEvent.SubmitAnswer) // This will set an error

        // When
        viewModel.handleEvent(MainUiEvent.ClearError)

        // Then
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `viewModel can be instantiated`() {
        // This test verifies that the ViewModel can be created without errors
        assert(viewModel != null)
    }
} 