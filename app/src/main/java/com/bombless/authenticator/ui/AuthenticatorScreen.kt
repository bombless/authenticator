package com.bombless.authenticator.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bombless.authenticator.data.EntryStore
import com.bombless.authenticator.data.OtpEntry
import com.bombless.authenticator.data.OtpUriParser
import com.bombless.authenticator.data.Totp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatorScreen() {
    val context = LocalContext.current
    val store = remember { EntryStore(context) }
    val entries = remember { mutableStateListOf<OtpEntry>().apply { addAll(store.load()) } }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var showScanner by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<OtpEntry?>(null) }
    var renameText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(250)
        }
    }

    fun persist() = store.save(entries.toList())

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("Authenticator", fontWeight = FontWeight.SemiBold)
                    Text("${entries.size} 个账号", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }, actions = {
                IconButton(onClick = { showScanner = true }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "扫描二维码")
                }
            })
        },
        floatingActionButton = {
            Button(onClick = { showScanner = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("添加账号")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("还没有验证码", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                Text("扫描服务提供的身份验证二维码，即可添加账号并实时生成动态验证码。", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                Button(onClick = { showScanner = true }, modifier = Modifier.padding(top = 20.dp)) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text("扫描二维码")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val code = remember(entry, now / (entry.period * 1000L)) { Totp.code(entry, now) }
                    val remaining = Totp.remainingSeconds(entry, now)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    if (entry.subtitle.isNotBlank()) Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { renameTarget = entry; renameText = entry.title }) {
                                    Icon(Icons.Default.Edit, contentDescription = "重命名")
                                }
                                IconButton(onClick = {
                                    entries.removeAll { it.id == entry.id }
                                    persist()
                                }) { Icon(Icons.Default.DeleteOutline, contentDescription = "删除") }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    code.chunked(3).joinToString(" "),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("OTP", code))
                                    Toast.makeText(context, "验证码已复制", Toast.LENGTH_SHORT).show()
                                }) { Icon(Icons.Default.ContentCopy, contentDescription = "复制验证码") }
                            }
                            Text("${remaining}s 后刷新", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showScanner) {
        AlertDialog(
            onDismissRequest = { showScanner = false },
            title = { Text("扫描身份验证二维码") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("将二维码放入取景框中")
                    QrScanner { raw ->
                        runCatching { OtpUriParser.parse(raw) }
                            .onSuccess { entry ->
                                if (entries.none { it.secret == entry.secret && it.account == entry.account }) {
                                    entries.add(entry)
                                    persist()
                                }
                                showScanner = false
                                Toast.makeText(context, "账号已添加", Toast.LENGTH_SHORT).show()
                            }
                            .onFailure {
                                showScanner = false
                                Toast.makeText(context, it.message ?: "无法识别二维码", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showScanner = false }) { Text("关闭") } }
        )
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名账号") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("显示名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val index = entries.indexOfFirst { it.id == target.id }
                    if (index >= 0 && renameText.isNotBlank()) {
                        entries[index] = target.copy(issuer = renameText.trim(), account = "")
                        persist()
                    }
                    renameTarget = null
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("取消") } }
        )
    }
}
