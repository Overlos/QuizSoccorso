package com.example.quizsoccorso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun StudyScreen(

    categoryStats: List<CategoryStat>,

    sectionLabel: String = "",

    onCategorySelected: (String) -> Unit,

    onAllQuestionsSelected: () -> Unit

) {


    Column(

        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {


        Text(

            text = androidx.compose.ui.res.stringResource(R.string.study_mode)

        )

        if (sectionLabel.isNotBlank()) {

            Text(

                text = androidx.compose.ui.res.stringResource(R.string.section_format, sectionLabel),

                style = MaterialTheme.typography.labelMedium

            )

        }


        Button(

            modifier = Modifier.fillMaxWidth(),

            onClick = {

                onAllQuestionsSelected()

            }

        ) {

            Text(androidx.compose.ui.res.stringResource(R.string.all_questions))

        }


        Spacer(

            modifier = Modifier.height(16.dp)

        )


        Text(

            text = androidx.compose.ui.res.stringResource(R.string.choose_chapter)

        )

        if (categoryStats.isEmpty()) {

            Text(
                text = androidx.compose.ui.res.stringResource(R.string.no_questions_available),
                style = MaterialTheme.typography.bodyMedium
            )

        }


        categoryStats.forEach { stat ->


            Button(

                modifier = Modifier.fillMaxWidth(),

                onClick = {

                    onCategorySelected(stat.category)

                }

            ) {

                Column(

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    Text(stat.category)

                    if (stat.precisionPercent >= 0) {

                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.precision_study_format, stat.precisionPercent, stat.answeredCount, stat.totalCount, precisionLabel(stat.precisionPercent)),
                            style = MaterialTheme.typography.labelSmall
                        )

                    } else {

                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.questions_count_not_attempted_study, stat.totalCount),
                            style = MaterialTheme.typography.labelSmall
                        )

                    }

                }

            }

        }

    }

}
