package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppBackupSnapshot
import com.example.model.AppManagementConfig
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppBackupScreen(
    backups: List<AppBackupSnapshot>,
    lastBackupJsonText: String,
    managementConfig: AppManagementConfig,
    onBack: () -> Unit,
    onCreateBackup: (label: String) -> AppBackupSnapshot,
    onRestoreBackup: (backupId: String) -> Boolean,
    onDeleteBackup: (backupId: String) -> Unit,
    onOpenAppManagement: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var customLabel by remember { mutableStateOf("") }
    val accent = managementConfig.themePreset.accentColor

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("backup_screen_back")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column {
                            Text(
                                text = "App Backup & Restore",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = accent
                            )
                            Text(
                                text = "ऐप के सभी डेटा, थीम, इमेज, टेक्स्ट व लिखावट का बैकअप",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Scroll Down Button inside Backup screen too
                    IconButton(
                        onClick = {
                            scope.launch {
                                scrollState.animateScrollTo((scrollState.value + 500).coerceAtMost(scrollState.maxValue))
                            }
                        },
                        modifier = Modifier.testTag("backup_scroll_down_btn")
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll Down", tint = accent)
                    }
                }
            }
        },
        containerColor = managementConfig.themePreset.backgroundColor,
        modifier = modifier
            .fillMaxSize()
            .testTag("app_backup_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Create New Full App Backup Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.5.dp, accent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.14f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = accent, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Create Full App Backup", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("1-टैप में पूरा डैशबोर्ड, कैटेगरी, प्रोडक्ट, थीम व फ़ॉन्ट सेव करें", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Backup Name (Optional, e.g. Diwali Theme Backup)", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val created = onCreateBackup(customLabel)
                                customLabel = ""
                                Toast.makeText(context, "Backup Saved: ${created.label}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("create_backup_now_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Backup Now", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (lastBackupJsonText.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(lastBackupJsonText))
                                    Toast.makeText(context, "Backup data copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Backup", modifier = Modifier.size(16.dp))
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Gyanvi Dresses App Backup")
                                        putExtra(Intent.EXTRA_TEXT, lastBackupJsonText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share App Backup"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Backup", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 2. Saved Backups List (Restore / Delete)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Saved Backups (${backups.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("पुराना बैकअप एक टैप में Restore या Delete करें", fontSize = 11.sp, color = TextSecondary)
                        }
                        TextButton(onClick = onOpenAppManagement) {
                            Text("Edit App →", color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (backups.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(34.dp))
                                Text("No Backups Yet", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("ऊपर 'Backup Now' बटन दबाकर पहला बैकअप बनाएं।", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        backups.forEach { bkp ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth().testTag("backup_item_${bkp.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(bkp.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                        Text(bkp.createdAt, fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text(bkp.summary, fontSize = 11.sp, color = Color(0xFF92400E))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                onDeleteBackup(bkp.id)
                                                Toast.makeText(context, "Backup deleted", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Delete", color = Color(0xFFDC2626), fontSize = 12.sp)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val ok = onRestoreBackup(bkp.id)
                                                Toast.makeText(
                                                    context,
                                                    if (ok) "Backup Restored!" else "Restore failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp).testTag("restore_backup_${bkp.id}")
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Restore", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
