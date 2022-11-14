package com.example.androidcomposetuts.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Colors
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

@Composable
fun TriviaHome(viewModel: QuestionsViewModel = hiltViewModel()) {
    QuestionsComponent(viewModel = viewModel)
}

@Composable
private fun QuestionsComponent(viewModel: QuestionsViewModel) {
    val questions = viewModel.data.data?.toMutableList()

    var questionIndex by remember { mutableStateOf(0) }

    if (viewModel.data.loading == true) {
        CircularProgressIndicator()
    } else {
        val question = try {
            questions?.get(questionIndex)
        } catch (e: Exception) {
            null
        }

        if (questions != null && question != null) {
            QuestionDisplay(
                questionsItem = question,
                totalQuestions = questions.size,
                questionIndex = questionIndex,
                viewModel = viewModel
            ) {
                questionIndex += 1
            }
        }
    }
}

@Composable
private fun QuestionDisplay(
    questionsItem: QuestionsItem,
    questionIndex: Int,
    totalQuestions: Int,
    viewModel: QuestionsViewModel,
    onNextClicked: () -> Unit,
) {
    val choices = remember(questionsItem) {
        questionsItem.choices.toMutableList()
    }

    var answer by remember(questionsItem) {
        mutableStateOf<Int?>(null)
    }
    var isAnswerCorrect by remember(questionsItem) {
        mutableStateOf<Boolean?>(null)
    }

    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(10f, 10f),
        phase = 0f,
    )

    val updateAnswer: (Int) -> Unit = { newSelectedIndex ->
        answer = newSelectedIndex
        isAnswerCorrect = choices[newSelectedIndex] == questionsItem.answer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        color = TriviaAppColors.mDarkPurple,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            if (questionIndex >= 3) {
                ShowProgress(
                    score = questionIndex,
                    totalQuestions = totalQuestions,
                )
            }

            QuestionTracker(
                counter = questionIndex + 1,
                outOf = totalQuestions,
            )
            DottedLine(pathEffect = pathEffect)
            Column {
                Text(
                    text = questionsItem.question,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(alignment = Alignment.Start)
                        .fillMaxHeight(0.3f),
                    fontSize = 17.sp,
                    color = TriviaAppColors.mOffWhite,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                )

                // choices
                choices.forEachIndexed { index, choice ->
                    Row(
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxWidth()
                            .height(45.dp)
                            .border(
                                4.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        TriviaAppColors.mOffDarkPurple,
                                        TriviaAppColors.mOffDarkPurple,
                                    ),
                                ),
                                shape = RoundedCornerShape(15.dp)
                            )
                            .clip(RoundedCornerShape(50))
                            .background(Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = answer == index,
                            onClick = {
                                updateAnswer(index)
                            },
                            modifier = Modifier.padding(start = 16.dp),
                            colors = RadioButtonDefaults.colors(
                                selectedColor =
                                if (isAnswerCorrect == true)
                                    Color.Green.copy(alpha = 0.5f)
                                else
                                    Color.Red.copy(alpha = 0.5f)
                            ),
                        )

                        val annotatedString = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    color = when {
                                        isAnswerCorrect == true && index == answer -> Color.Green
                                        isAnswerCorrect == false && index == answer -> Color.Red
                                        else -> TriviaAppColors.mOffWhite
                                    },
                                    fontSize = 17.sp,
                                )
                            ) {
                                append(choice)
                            }
                        }
                        Text(text = annotatedString)
                    }
                }

                // Next button
                Button(
                    modifier = Modifier
                        .padding(3.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = TriviaAppColors.mLightBlue
                    ),
                    enabled = isAnswerCorrect == true,
                    onClick = onNextClicked,
                ) {
                    Text(
                        text = "Next",
                        modifier = Modifier
                            .padding(4.dp),
                        color = TriviaAppColors.mOffWhite,
                        fontSize = 17.sp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ShowProgress(score: Int = 50, totalQuestions: Int = 100) {
    val gradient = Brush.linearGradient(listOf(
        Color(0xFFF95075),
        Color(0xFFBE6BE5),
    ))
    val progressFactor = remember(score, totalQuestions) {
        score.toFloat() / totalQuestions.toFloat()
    }

    Row(
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth()
            .height(45.dp)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TriviaAppColors.mLightPurple,
                        TriviaAppColors.mLightPurple,
                    )
                ),
                shape = RoundedCornerShape(34.dp),
            )
            .clip(RoundedCornerShape(50))
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            contentPadding = PaddingValues(1.dp),
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(progressFactor)
                .background(brush = gradient),
            enabled = false,
            elevation = null,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                disabledBackgroundColor = Color.Transparent,
            ),
        ) {
            Text(
                text = (score * 10).toString(),
                modifier = Modifier
                    .clip(RoundedCornerShape(23.dp))
                    .fillMaxHeight(0.87f)
                    .fillMaxWidth()
                    .padding(6.dp),
                color = TriviaAppColors.mOffWhite,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuestionTracker(
    counter: Int = 10,
    outOf: Int = 100,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
                withStyle(
                    style = SpanStyle(
                        color = TriviaAppColors.mLightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 27.sp,
                    )
                ) {
                    append("Question $counter/")
                }

                withStyle(
                    style = SpanStyle(
                        color = TriviaAppColors.mLightGray,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp
                    )
                ) {
                    append("$outOf")
                }
            }
        },
        modifier = Modifier.padding(20.dp),
    )
}

@Composable
private fun DottedLine(pathEffect: PathEffect) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = TriviaAppColors.mLightGray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object JetTriviaAppModule {
    @Singleton
    @Provides
    fun provideQuestionRepository(api: QuestionApi) = QuestionRepository(api)

    @Singleton
    @Provides
    fun provideQuestionApi(): QuestionApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuestionApi::class.java)
    }
}

@Singleton
interface QuestionApi {
    @GET("world.json")
    suspend fun getAllQuestions(): Questions
}

typealias QuestionsResponse = JetTriviaAppHttpResponse<
        ArrayList<QuestionsItem>,
        Boolean,
        Exception,
        >

class QuestionRepository @Inject constructor(
    private val api: QuestionApi,
) {
    private val questionsResponse = QuestionsResponse()

    suspend fun getAllQuestions(): QuestionsResponse {
        try {
            questionsResponse.loading = true
            questionsResponse.data = api.getAllQuestions()
            if (questionsResponse.data.toString().isNotEmpty()) {
                questionsResponse.loading = false
            }
        } catch (exception: Exception) {
            questionsResponse.error = exception
            Log.d("Exception", "getAllQuestions: ${exception.localizedMessage}")
        }
        return questionsResponse
    }
}

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val repository: QuestionRepository,
) : ViewModel() {
    var data by mutableStateOf(
        QuestionsResponse(null, true, Exception())
    )

    init {
        getAllQuestions()
    }

    private fun getAllQuestions() {
        viewModelScope.launch {
            data.loading = true
            data = repository.getAllQuestions()
            if (data.data.toString().isNotEmpty()) {
                data.loading = false
            }
        }
    }
}

object TriviaAppColors {
    val mLightGray = Color(0xffb4b9cd)
    val mBlack = Color(0xff060814)
    val mOffWhite = Color(0xffeceeef)
    val mLightPurple = Color(0xff545a75)
    val mDarkPurple = Color(0xff262C49)
    val mOffDarkPurple = Color(0xFF22496B)
    val mLightBlue = Color(0xff127EEA)
    val mBrown = Color(0xff7f6749)
    val mBlue = Color(0xff4b5f9e)
}

private object Constants {
    // https://raw.githubusercontent.com/itmmckernan/triviaJSON/master/world.json
    const val BASE_URL = "https://raw.githubusercontent.com/itmmckernan/triviaJSON/master/"
}

data class JetTriviaAppHttpResponse<T, Boolean, Error : Exception>(
    var data: T? = null,
    var loading: Boolean? = null,
    var error: Error? = null,
)

class Questions : ArrayList<QuestionsItem>()

data class QuestionsItem(
    val answer: String,
    val category: String,
    val choices: List<String>,
    val question: String
)