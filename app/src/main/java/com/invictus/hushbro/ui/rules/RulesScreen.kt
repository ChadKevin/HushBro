package com.invictus.hushbro.ui.rules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.db.entity.RuleEntity
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.data.repo.RuleRepository
import kotlinx.coroutines.launch

@Composable
fun RulesScreen(ruleRepository: RuleRepository) {
    val rules by ruleRepository.observeRules().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var keyword by remember { mutableStateOf("") }
    var appPkg by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Rules", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        OutlinedTextField(value = keyword, onValueChange = { keyword = it }, label = { Text("Keyword") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = appPkg, onValueChange = { appPkg = it }, label = { Text("App package (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (keyword.isNotBlank()) scope.launch {
                ruleRepository.upsert(RuleEntity(priority = rules.size, appPackage = appPkg.ifBlank { null }, keyword = keyword, action = RuleAction.MUTE))
                keyword = ""; appPkg = ""
            }
        }) { Text("Add Rule (MUTE)") }
        rules.forEach { r ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${r.priority}: ${r.keyword} -> ${r.action} (${if (r.enabled) "on" else "off"})", Modifier.weight(1f))
                Button(onClick = { scope.launch { ruleRepository.upsert(r.copy(enabled = !r.enabled)) } }) { Text(if (r.enabled) "Disable" else "Enable") }
                Button(onClick = { scope.launch { ruleRepository.deleteById(r.id) } }) { Text("Delete") }
            }
        }
    }
}
