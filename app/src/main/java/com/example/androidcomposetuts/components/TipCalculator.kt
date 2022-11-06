package com.example.androidcomposetuts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun TipCalculator() {
    var totalPerPerson by remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        TopHeader(totalPerPerson = totalPerPerson)
        BillForm { totalPerPerson = it }
    }
}

@Preview
@Composable
private fun TopHeader(
    totalPerPerson: Double = 0.0,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .height(150.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        color = Color(0xFFE9D7F7)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val total = "%.2f".format(totalPerPerson)

            Text(
                text = "Total Per Person",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "$$total",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BillForm(
    modifier: Modifier = Modifier,
    updateTotalPerPerson: (Double) -> Unit = { },
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val totalBillState = remember { mutableStateOf("0") }
    val validState = remember(totalBillState) {
        totalBillState.value.trim().isNotEmpty()
    }
    val sliderPosition = remember { mutableStateOf(0.33f) }
    val splitByState = remember { mutableStateOf(1) }
    val tipPercentage = (sliderPosition.value * 100).toInt()
    val tipAmountState = remember(totalBillState.value, tipPercentage) {
        run {
            val totalBill = totalBillState
                .value
                .padStart(1, '0')
                .toDouble()
            if (totalBill > 1) {
                (totalBill * tipPercentage) / 100
            } else {
                0.0
            }
        }
    }
    val totalPerPerson = remember(tipAmountState, splitByState.value) {
        tipAmountState / splitByState.value
    }

    LaunchedEffect(totalPerPerson) {
        updateTotalPerPerson(totalPerPerson)
    }

    Surface(
        modifier = modifier
            .padding(2.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            InputField(
                label = "Enter Bill",
                valueState = totalBillState,
                keyboardActions = KeyboardActions {
                    if (validState.not()) return@KeyboardActions

                    keyboardController?.hide()
                }
            )

            // Split row
            Row(
                modifier = Modifier
                    .padding(3.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "Split",
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                )
                Spacer(modifier = Modifier.width(120.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    RoundIconButton(
                        imageVector = Icons.Default.Remove,
                        onClick = {
                            splitByState.value = (splitByState.value - 1)
                                .coerceAtLeast(1)
                        }
                    )
                    Text(
                        text = "${splitByState.value}",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 9.dp, end = 9.dp)
                    )
                    RoundIconButton(
                        imageVector = Icons.Default.Add,
                        onClick = {
                            splitByState.value = splitByState.value + 1
                        }
                    )
                }
            }

            // Tip row
            Row(
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Tip",
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Spacer(modifier = Modifier.width(200.dp))
                Text(
                    text = "$${tipAmountState}",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Percent
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "$tipPercentage%")

                Spacer(modifier = Modifier.height(14.dp))

                Slider(
                    value = sliderPosition.value,
                    onValueChange = {
                        sliderPosition.value = it
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    steps = 5,
                    onValueChangeFinished = {

                    }
                )
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    valueState: MutableState<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSingleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Number,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = { valueState.value = it },
        modifier = modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AttachMoney,
                contentDescription = "",
            )
        },
        singleLine = isSingleLine,
        enabled = enabled,
        textStyle = TextStyle(
            fontSize = 18.sp,
            color = MaterialTheme.colors.onBackground,
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

@Composable
private fun RoundIconButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Black.copy(alpha = 0.8f),
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = 4.dp,
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .then(Modifier.size(40.dp)),
        shape = CircleShape,
        backgroundColor = backgroundColor,
        elevation = elevation,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "",
            tint = tint,
        )
    }
}