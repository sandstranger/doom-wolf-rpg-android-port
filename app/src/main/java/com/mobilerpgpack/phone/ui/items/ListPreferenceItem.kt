package com.mobilerpgpack.phone.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> ListPreferenceItem(title: String, initialValue: T, allValues : Collection<T>, onValueChange : (String) -> Unit) {
    var showValuesDialog by rememberSaveable  { mutableStateOf(false) }
    var activeValue by rememberSaveable (initialValue.toString()) { mutableStateOf(initialValue.toString()) }
    val stringValues: Collection<String> = allValues.map { it.toString() }
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showValuesDialog = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TranslatedText(title, Modifier.weight(0.4f, true))
        Spacer(Modifier.width(8.dp))
        Text(activeValue, modifier = Modifier.weight(0.6f).fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Right)
    }

    if (showValuesDialog){
        AlertDialog(
            onDismissRequest = { showValuesDialog = false },
            title = { TranslatedText(title) },
            text = {
                Column (modifier = Modifier.verticalScroll(scrollState)) {
                    stringValues.forEach { stringValue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeValue = stringValue
                                    onValueChange(stringValue)
                                    showValuesDialog = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = activeValue == stringValue, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringValue)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}