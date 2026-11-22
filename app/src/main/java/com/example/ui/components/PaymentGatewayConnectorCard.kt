package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.ui.theme.*
import com.example.util.GatewayConnectionTestResult
import com.example.util.PaymentGatewayUrlHelper
import kotlinx.coroutines.launch

@Composable
fun PaymentGatewayConnectorSection(
    config: PaymentGatewaySettingsConfig,
    onSaveAndConnect: (
        id: String?,
        name: String,
        url: String,
        provider: PaymentGatewayProviderPreset,
        lastStatus: String,
        connectNow: Boolean
    ) -> Result<PaymentGatewayLinkItem>,
    onConnectGateway: (String) -> Unit,
    onDisconnectGateway: () -> Unit,
    onDeleteGateway: (String) -> Unit,
    onToggleAppendParams: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sectionTitle: String = "All Payment Gateway URLs 🔗",
    sectionSubtitle: String = "Razorpay, PhonePe PG, Paytm, Cashfree, Google Pay, PayU, Instamojo, CCAvenue, Easebuzz, BillDesk, Stripe & PayPal",
    onLoadAllPresets: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingGateway by remember { mutableStateOf<PaymentGatewayLinkItem?>(null) }
    var showAllSavedGateways by remember { mutableStateOf(false) }
    var quickPayAmountInput by remember { mutableStateOf("499") }
    var showQuickPayBar by remember { mutableStateOf(false) }
    val activeGateway = config.activeGateway

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, if (activeGateway != null) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeGateway != null) Color(0xFFDCFCE7) else Color(0xFFFDF2F8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeGateway != null) Icons.Filled.Link else Icons.Outlined.AddLink,
                            contentDescription = "Payment Gateway Link",
                            tint = if (activeGateway != null) Color(0xFF15803D) else MeeshoMagenta
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sectionTitle, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = TextPrimary)
                        Text(sectionSubtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeGateway != null) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (activeGateway != null) "CONNECTED" else "NOT LINKED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeGateway != null) Color(0xFF15803D) else TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Active Connected Gateway Summary
            if (activeGateway != null) {
                val host = remember(activeGateway.url) {
                    PaymentGatewayUrlHelper.validateHttpsGatewayUrl(activeGateway.url).host.ifBlank { activeGateway.url }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Active Gateway: ${activeGateway.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                                Text("${activeGateway.provider.displayName} • $host", fontSize = 11.sp, color = Color(0xFF15803D))
                                Text(
                                    text = activeGateway.url,
                                    fontSize = 10.5.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Filled.Verified, contentDescription = "Verified HTTPS", tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val payAmt = quickPayAmountInput.toIntOrNull()?.coerceAtLeast(1) ?: 499
                                    val refId = "GD-${System.currentTimeMillis() % 1000000}"
                                    val checkoutUrl = PaymentGatewayUrlHelper.buildCheckoutGatewayUrl(
                                        baseUrl = activeGateway.url,
                                        amountInr = payAmt,
                                        orderRef = refId,
                                        appendParams = config.appendOrderParams
                                    ) ?: activeGateway.url
                                    val res = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, checkoutUrl)
                                    if (!res.isValid) {
                                        Toast.makeText(context, res.errorMessage ?: "Invalid URL", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).heightIn(min = 42.dp).testTag("open_connected_gateway_btn")
                            ) {
                                Icon(Icons.Outlined.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay / Open 🔗", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showQuickPayBar = !showQuickPayBar },
                                modifier = Modifier.heightIn(min = 42.dp).testTag("toggle_quick_pay_btn")
                            ) {
                                Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(16.dp), tint = MeeshoMagenta)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay ₹", fontSize = 11.5.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onDisconnectGateway()
                                    Toast.makeText(context, "Gateway disconnected", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.heightIn(min = 42.dp).testTag("disconnect_gateway_btn")
                            ) {
                                Icon(Icons.Outlined.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                            }
                        }

                        if (showQuickPayBar) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = quickPayAmountInput,
                                    onValueChange = { quickPayAmountInput = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Amount (₹)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val payAmt = quickPayAmountInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                        val refId = "GD-PAY-${System.currentTimeMillis() % 1000000}"
                                        val payUrl = PaymentGatewayUrlHelper.buildCheckoutGatewayUrl(
                                            baseUrl = activeGateway.url,
                                            amountInr = payAmt,
                                            orderRef = refId,
                                            appendParams = config.appendOrderParams
                                        ) ?: activeGateway.url
                                        val res = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, payUrl)
                                        if (!res.isValid) {
                                            Toast.makeText(context, res.errorMessage ?: "Invalid URL", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text("Pay Now 🔗", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Saved Gateway Links List
            if (config.gateways.isNotEmpty()) {
                val visibleGateways = if (showAllSavedGateways || config.gateways.size <= 4) {
                    config.gateways
                } else {
                    val prioritized = listOfNotNull(activeGateway) + config.gateways.filter { it.id != activeGateway?.id }
                    prioritized.take(4)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Gateway URLs (${config.gateways.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    if (config.gateways.size > 4) {
                        TextButton(onClick = { showAllSavedGateways = !showAllSavedGateways }) {
                            Text(
                                text = if (showAllSavedGateways) "Show Less ▲" else "View All ${config.gateways.size} Gateways ▼",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagenta
                            )
                        }
                    }
                }
                visibleGateways.forEach { item ->
                    val isConnected = item.id == config.activeGatewayId && item.isEnabled
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isConnected) Color(0xFFFDF2F8) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isConnected) MeeshoMagenta else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)
                                    Text(
                                        text = item.url,
                                        fontSize = 11.sp,
                                        color = Color(0xFF2563EB),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text("${item.provider.displayName} • ${item.lastStatus}", fontSize = 10.sp, color = TextSecondary)
                                }
                                if (isConnected) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = MeeshoMagenta) {
                                        Text(
                                            "Active",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            onConnectGateway(item.id)
                                            Toast.makeText(context, "Connected to ${item.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.heightIn(min = 40.dp)
                                    ) {
                                        Text("Connect", fontWeight = FontWeight.Bold, color = MeeshoMagenta, fontSize = 12.sp)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        val payAmt = quickPayAmountInput.toIntOrNull()?.coerceAtLeast(1) ?: 499
                                        val refId = "GD-${System.currentTimeMillis() % 1000000}"
                                        val payUrl = PaymentGatewayUrlHelper.buildCheckoutGatewayUrl(
                                            baseUrl = item.url,
                                            amountInr = payAmt,
                                            orderRef = refId,
                                            appendParams = config.appendOrderParams
                                        ) ?: item.url
                                        val res = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, payUrl)
                                        if (!res.isValid) {
                                            Toast.makeText(context, res.errorMessage ?: "Invalid URL", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Outlined.OpenInBrowser, contentDescription = "Pay via Gateway", modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pay / Open", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = {
                                        editingGateway = item
                                        showAddEditDialog = true
                                    }
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Edit Gateway", modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit URL", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = {
                                        onDeleteGateway(item.id)
                                        Toast.makeText(context, "Removed ${item.name}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete Gateway", tint = Color(0xFFDC2626), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // Pass order params toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pass Order Amount & Ref in URL", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Appends ?amount=...&currency=INR&order_ref=... safely", fontSize = 10.5.sp, color = TextSecondary)
                }
                Switch(
                    checked = config.appendOrderParams,
                    onCheckedChange = onToggleAppendParams,
                    colors = SwitchDefaults.colors(checkedTrackColor = MeeshoMagenta)
                )
            }

            if (config.gateways.size < com.example.model.PaymentGatewayDefaults.allGateways.size) {
                OutlinedButton(
                    onClick = {
                        if (onLoadAllPresets != null) {
                            onLoadAllPresets()
                        } else {
                            val existingProviders = config.gateways.map { it.provider }.toSet()
                            com.example.model.PaymentGatewayDefaults.allGateways.forEach { def ->
                                if (!existingProviders.contains(def.provider)) {
                                    onSaveAndConnect(def.id, def.name, def.url, def.provider, def.lastStatus, false)
                                }
                            }
                        }
                        Toast.makeText(context, "All 12 Payment Gateway URLs added 🔗", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).testTag("load_all_gateways_btn")
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load All 12 Gateway URLs 🔗", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                }
            }

            // Primary Add Gateway Button
            Button(
                onClick = {
                    editingGateway = null
                    showAddEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("add_payment_gateway_url_btn")
            ) {
                Icon(Icons.Outlined.AddLink, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Payment Gateway URL 🔗", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
    }

    if (showAddEditDialog) {
        AddEditPaymentGatewayDialog(
            initialGateway = editingGateway,
            onDismiss = { showAddEditDialog = false },
            onSaveAndConnect = { id, name, url, provider, status ->
                val res = onSaveAndConnect(id, name, url, provider, status, true)
                if (res.isSuccess) {
                    showAddEditDialog = false
                    Toast.makeText(context, "Payment Gateway connected: $name 🔗", Toast.LENGTH_SHORT).show()
                }
                res
            }
        )
    }
}

@Composable
fun AddEditPaymentGatewayDialog(
    initialGateway: PaymentGatewayLinkItem? = null,
    onDismiss: () -> Unit,
    onSaveAndConnect: (
        id: String?,
        name: String,
        url: String,
        provider: PaymentGatewayProviderPreset,
        lastStatus: String
    ) -> Result<PaymentGatewayLinkItem>
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var selectedProvider by remember(initialGateway) {
        mutableStateOf(initialGateway?.provider ?: PaymentGatewayProviderPreset.RAZORPAY)
    }
    var gatewayName by remember(initialGateway) {
        mutableStateOf(initialGateway?.name ?: selectedProvider.displayName)
    }
    var gatewayUrl by remember(initialGateway) {
        mutableStateOf(initialGateway?.url ?: "")
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<GatewayConnectionTestResult?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("add_payment_gateway_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.AddLink, contentDescription = null, tint = MeeshoMagenta)
                        Text(
                            text = if (initialGateway == null) "Add Payment Gateway URL 🔗" else "Edit Payment Gateway URL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text("Select Gateway Type:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentGatewayProviderPreset.entries.forEach { preset ->
                        val isSel = selectedProvider == preset
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedProvider = preset
                                if (initialGateway == null && (gatewayName.isBlank() || PaymentGatewayProviderPreset.entries.any { it.displayName == gatewayName })) {
                                    gatewayName = preset.displayName
                                }
                            },
                            label = { Text(preset.displayName.substringBefore(" Link").substringBefore(" Payment"), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeeshoMagenta,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = gatewayName,
                    onValueChange = { gatewayName = it },
                    label = { Text("Gateway Name (जैसे Razorpay / Store Pay)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("gateway_name_input")
                )

                OutlinedTextField(
                    value = gatewayUrl,
                    onValueChange = {
                        gatewayUrl = it
                        validationError = null
                        testResult = null
                    },
                    label = { Text("Payment Gateway HTTPS URL Link 🔗") },
                    placeholder = { Text(selectedProvider.hintUrl) },
                    isError = validationError != null,
                    supportingText = {
                        Text(
                            text = validationError ?: "Only https:// payment links are allowed for buyer safety.",
                            color = if (validationError != null) Color(0xFFDC2626) else TextSecondary,
                            fontSize = 11.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val pasted = clipboard.getText()?.text?.trim().orEmpty()
                                if (pasted.isNotBlank()) {
                                    gatewayUrl = pasted
                                    validationError = null
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste URL", tint = MeeshoMagenta)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("gateway_url_input")
                )

                if (testResult != null) {
                    val tr = testResult!!
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (tr.isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, if (tr.isSuccess) Color(0xFF86EFAC) else Color(0xFFFCD34D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tr.statusLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (tr.isSuccess) Color(0xFF166534) else Color(0xFF92400E))
                            Text(tr.detail, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        enabled = !isTesting,
                        onClick = {
                            val check = PaymentGatewayUrlHelper.validateHttpsGatewayUrl(gatewayUrl)
                            if (!check.isValid) {
                                validationError = check.errorMessage
                            } else {
                                gatewayUrl = check.normalizedUrl
                                isTesting = true
                                scope.launch {
                                    val res = PaymentGatewayUrlHelper.testPaymentGatewayConnection(check.normalizedUrl)
                                    testResult = res
                                    isTesting = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("test_gateway_connection_btn")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Outlined.WifiFind, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            val check = PaymentGatewayUrlHelper.validateHttpsGatewayUrl(gatewayUrl)
                            if (!check.isValid) {
                                validationError = check.errorMessage
                            } else {
                                val status = testResult?.statusLabel ?: "Connected (HTTPS Verified)"
                                val saveRes = onSaveAndConnect(
                                    initialGateway?.id,
                                    gatewayName,
                                    check.normalizedUrl,
                                    selectedProvider,
                                    status
                                )
                                if (saveRes.isFailure) {
                                    validationError = saveRes.exceptionOrNull()?.message ?: "Unable to save URL"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("save_connect_gateway_btn")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
