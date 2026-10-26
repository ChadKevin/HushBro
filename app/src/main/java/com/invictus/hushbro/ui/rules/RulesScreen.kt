package com.invictus.hushbro.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.invictus.hushbro.ui.components.CategoryChip
import com.invictus.hushbro.ui.components.EmptyState
import com.invictus.hushbro.ui.components.FeatureCard
import com.invictus.hushbro.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun RulesScreen(ruleRepository: RuleRepository) {
    val rules by ruleRepository.observeRules().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editorRule by remember { mutableStateOf<RuleEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deletingRule by remember { mutableStateOf<RuleEntity?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader("Rules", "Decide what gets your attention and what can wait.")
        Button(
            onClick = { creating = true; editorRule = null },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Icon(Icons.Filled.Add, contentDescription = null); Spacer(Modifier.size(8.dp)); Text("Create a rule") }
        Spacer(Modifier.height(12.dp))
        if (rules.isEmpty()) {
            EmptyState("Set your first rule", "Choose a keyword or app, then decide whether to mute it or mark it important.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(rules.sortedBy { it.priority }, key = { it.id }) { rule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(rule.keyword, style = MaterialTheme.typography.titleLarge)
                                    Text(rule.appPackage?.takeIf { it.isNotBlank() } ?: "Any app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = rule.enabled,
                                    onCheckedChange = { enabled -> scope.launch { ruleRepository.upsert(rule.copy(enabled = enabled)) } },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CategoryChip(
                                    category = rule.action.name.lowercase(),
                                    onClick = null
                                )
                                Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Priority ${rule.priority + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { editorRule = rule; creating = false }, modifier = Modifier.weight(1f)) { Text("Edit") }
                                OutlinedButton(onClick = {
                                    val index = rules.sortedBy { it.priority }.indexOfFirst { it.id == rule.id }
                                    val sorted = rules.sortedBy { it.priority }
                                    if (index > 0) {
                                        val previous = sorted[index - 1]
                                        scope.launch { ruleRepository.swapPriority(rule.id, rule.priority, previous.id, previous.priority) }
                                    }
                                }, enabled = rules.sortedBy { it.priority }.indexOfFirst { it.id == rule.id } > 0) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Move rule up", modifier = Modifier.size(18.dp)) }
                                OutlinedButton(onClick = {
                                    val sorted = rules.sortedBy { it.priority }
                                    val index = sorted.indexOfFirst { it.id == rule.id }
                                    if (index >= 0 && index < sorted.lastIndex) {
                                        val next = sorted[index + 1]
                                        scope.launch { ruleRepository.swapPriority(rule.id, rule.priority, next.id, next.priority) }
                                    }
                                }, enabled = rules.sortedBy { it.priority }.indexOfFirst { it.id == rule.id } < rules.lastIndex) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Move rule down", modifier = Modifier.size(18.dp)) }
                                TextButton(onClick = { deletingRule = rule }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editorRule != null) {
        RuleEditorDialog(
            initial = editorRule,
            nextPriority = (rules.maxOfOrNull { it.priority } ?: -1) + 1,
            onDismiss = { creating = false; editorRule = null },
            onSave = { keyword, packageName, action, enabled, priority ->
                scope.launch {
                    val original = editorRule
                    ruleRepository.upsert(
                        RuleEntity(
                            id = original?.id ?: 0L,
                            priority = priority,
                            appPackage = packageName.trim().ifBlank { null },
                            keyword = keyword.trim(),
                            action = action,
                            channelLabel = original?.channelLabel,
                            enabled = enabled
                        )
                    )
                }
                creating = false
                editorRule = null
            }
        )
    }

    deletingRule?.let { rule ->
        AlertDialog(
            onDismissRequest = { deletingRule = null },
            title = { Text("Delete this rule?") },
            text = { Text("Notifications will no longer match ‘${rule.keyword}’ through this rule.") },
            confirmButton = { TextButton(onClick = { scope.launch { ruleRepository.deleteById(rule.id) }; deletingRule = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deletingRule = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RuleEditorDialog(
    initial: RuleEntity?,
    nextPriority: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, RuleAction, Boolean, Int) -> Unit
) {
    var keyword by remember(initial?.id) { mutableStateOf(initial?.keyword.orEmpty()) }
    var appPackage by remember(initial?.id) { mutableStateOf(initial?.appPackage.orEmpty()) }
    var action by remember(initial?.id) { mutableStateOf(initial?.action ?: RuleAction.MUTE) }
    var enabled by remember(initial?.id) { mutableStateOf(initial?.enabled ?: true) }
    var actionMenu by remember { mutableStateOf(false) }
    val priority = initial?.priority ?: nextPriority
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create a rule" else "Edit rule") },
        text = {
            Column {
                OutlinedTextField(value = keyword, onValueChange = { keyword = it }, label = { Text("Keyword or phrase") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = appPackage, onValueChange = { appPackage = it }, label = { Text("App package (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("When a notification matches", style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.layout.Box {
                    OutlinedButton(onClick = { actionMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(action.name.replace('_', ' ')) }
                    DropdownMenu(expanded = actionMenu, onDismissRequest = { actionMenu = false }) {
                        listOf(RuleAction.MUTE, RuleAction.MARK_IMPORTANT).forEach { option ->
                            DropdownMenuItem(text = { Text(option.name.replace('_', ' ')) }, onClick = { action = option; actionMenu = false })
                        }
                    }
                }
                Text("Priority ${priority + 1}. Use the up/down controls on the rule card to change order.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Enabled", modifier = Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        },
        confirmButton = {
            TextButton(enabled = keyword.isNotBlank(), onClick = { onSave(keyword, appPackage, action, enabled, priority) }) { Text("Save rule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
