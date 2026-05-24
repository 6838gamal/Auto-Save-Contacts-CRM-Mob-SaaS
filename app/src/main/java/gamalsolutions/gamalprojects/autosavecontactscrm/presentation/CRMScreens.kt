package gamalsolutions.gamalprojects.autosavecontactscrm.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import gamalsolutions.gamalprojects.autosavecontactscrm.R
import gamalsolutions.gamalprojects.autosavecontactscrm.database.ContactEntity
import gamalsolutions.gamalprojects.autosavecontactscrm.database.LogEntity
import gamalsolutions.gamalprojects.autosavecontactscrm.managers.PermissionManager
import gamalsolutions.gamalprojects.autosavecontactscrm.utils.CsvExcelExporter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusManager

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backup,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.app_name_ar),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "AutoSave Contacts CRM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: CRMViewModel, onLoginComplete: () -> Unit) {
    val deviceEmails by viewModel.deviceEmails.collectAsState()
    
    val emailsToDisplay = remember(deviceEmails) {
        (deviceEmails + listOf(
            "gamalalmaqtary6838@gmail.com",
            "applicationsdeveloper6838@gmail.com"
        )).distinct()
    }

    var selectedEmail by remember { mutableStateOf("") }
    
    LaunchedEffect(emailsToDisplay) {
        if (selectedEmail.isEmpty() && emailsToDisplay.isNotEmpty()) {
            selectedEmail = emailsToDisplay.first()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDeviceEmails()
    }

    var customEmail by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "تسجيل الدخول للنظام سحابياً",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "يرجى تحديد أو كتابة بريد Gmail الخاص بك لضبط تهيئة الحفظ والمزامنة السحابية التلقائية لقاعدة بيانات العملاء والنسخ الاحتياطي.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "اختر من حسابات الجهاز المكتشفة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Right
                )

                emailsToDisplay.forEach { email ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!useCustom && selectedEmail == email)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                useCustom = false
                                selectedEmail = email
                                errorText = ""
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (!useCustom && selectedEmail == email),
                            onClick = {
                                useCustom = false
                                selectedEmail = email
                                errorText = ""
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (!useCustom && selectedEmail == email) FontWeight.Bold else FontWeight.Normal,
                            color = if (!useCustom && selectedEmail == email) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (useCustom)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable {
                            useCustom = true
                            errorText = ""
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = useCustom,
                        onClick = {
                            useCustom = true
                            errorText = ""
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "استخدام بريد إلكتروني مخصص آخر",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (useCustom) FontWeight.Bold else FontWeight.Normal,
                        color = if (useCustom) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (useCustom) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = {
                            customEmail = it
                            errorText = ""
                        },
                        placeholder = { Text("example@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val emailToSave = if (useCustom) customEmail.trim() else selectedEmail
                        if (emailToSave.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailToSave).matches()) {
                            errorText = "يرجى إدخال بريد إلكتروني صحيح للجاهزية"
                        } else {
                            viewModel.updateUserGmail(emailToSave)
                            onLoginComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الدخول وبدء المراقبة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Elegant Material 3 Color Schemes for AutoSave CRM Theme Modes
private val CRMDarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B5E20),
    background = Color(0xFF121212),
    onBackground = Color(0xFFDCDCDC),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFDCDCDC),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

private val CRMLightColorScheme = lightColorScheme(
    primary = Color(0xFF1E88E5),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF4CAF50),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFD2E5FA),
    onPrimaryContainer = Color(0xFF0D47A1),
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = Color(0xFF37474F)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: CRMViewModel) {
    val userGmail by viewModel.userGmail.collectAsState()
    val appLang by viewModel.appLanguage.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    
    var appPhase by remember { mutableStateOf("splash") }
    var currentScreen by remember { mutableStateOf("home") }
    val isRunning by viewModel.isServiceRunning.collectAsState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Dynamically apply selected app language locale
    val context = LocalContext.current
    val locale = remember(appLang) { Locale(appLang) }
    val localizedContext = remember(appLang) {
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }
    val layoutDirection = remember(appLang) {
        if (appLang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    // Dynamically apply selected app appearance theme
    val systemDarkMode = androidx.compose.foundation.isSystemInDarkTheme()
    val useDarkMode = remember(appTheme, systemDarkMode) {
        when (appTheme) {
            "dark" -> true
            "light" -> false
            else -> systemDarkMode
        }
    }
    val activeColorScheme = if (useDarkMode) CRMDarkColorScheme else CRMLightColorScheme

    LaunchedEffect(userGmail) {
        // Give 2 seconds to splash screen loaded
        kotlinx.coroutines.delay(2000)
        appPhase = if (userGmail.isNullOrBlank()) "login" else "main"
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(colorScheme = activeColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (appPhase) {
                    "splash" -> SplashScreen()
                    "login" -> LoginScreen(viewModel = viewModel, onLoginComplete = { appPhase = "main" })
                    "main" -> {
                        AppMainContent(
                            viewModel = viewModel,
                            currentScreen = currentScreen,
                            onScreenChange = { currentScreen = it },
                            isRunning = isRunning,
                            layoutDirection = layoutDirection,
                            focusManager = focusManager
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainContent(
    viewModel: CRMViewModel,
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    isRunning: Boolean,
    layoutDirection: LayoutDirection,
    focusManager: FocusManager
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userGmail by viewModel.userGmail.collectAsState()
    val appLang by viewModel.appLanguage.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name_ar),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AutoSave Contacts CRM",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        if (!userGmail.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userGmail!!,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val menuItems = listOf(
                    NavigationMenuItem("home", stringResource(R.string.nav_home), Icons.Default.Dashboard),
                    NavigationMenuItem("contacts", stringResource(R.string.nav_contacts), Icons.Default.People),
                    NavigationMenuItem("logs", stringResource(R.string.nav_logs), Icons.Default.ListAlt),
                    NavigationMenuItem("export", stringResource(R.string.nav_export), Icons.Default.Backup),
                    NavigationMenuItem("settings", stringResource(R.string.nav_settings), Icons.Default.Settings),
                    NavigationMenuItem("permissions", stringResource(R.string.nav_permissions), Icons.Default.Security),
                    NavigationMenuItem("contact_dev", stringResource(R.string.nav_contact_dev), Icons.Default.ContactMail)
                )

                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentScreen == item.route,
                        onClick = {
                            onScreenChange(item.route)
                            focusManager.clearFocus()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Language and Theme Action Controls in Sidebar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (appLang == "ar") "لغة التطبيق" else "Language",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.updateAppLanguage("ar") },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (appLang == "ar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (appLang == "ar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("عربي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { viewModel.updateAppLanguage("en") },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (appLang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (appLang == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("EN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (appTheme) {
                                    "dark" -> Icons.Default.DarkMode
                                    "light" -> Icons.Default.LightMode
                                    else -> Icons.Default.SettingsSuggest
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (appLang == "ar") "مظهر التطبيق" else "App Theme",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { viewModel.updateAppTheme("light") },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (appTheme == "light") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (appTheme == "light") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.LightMode, contentDescription = "Light Mode", modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { viewModel.updateAppTheme("dark") },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (appTheme == "dark") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (appTheme == "dark") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode", modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { viewModel.updateAppTheme("system") },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (appTheme == "system") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (appTheme == "system") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.SettingsSuggest, contentDescription = "System Default", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(R.string.nav_version),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                "home" -> stringResource(R.string.nav_home)
                                "contacts" -> stringResource(R.string.nav_contacts)
                                "logs" -> stringResource(R.string.nav_logs)
                                "export" -> stringResource(R.string.nav_export)
                                "settings" -> stringResource(R.string.nav_settings)
                                "permissions" -> stringResource(R.string.nav_permissions)
                                "contact_dev" -> stringResource(R.string.nav_contact_dev)
                                else -> stringResource(R.string.app_name)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.lbl_drawer_desc))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "home" -> HomeScreen(viewModel, onNavigate = onScreenChange)
                    "contacts" -> ContactsScreen(viewModel)
                    "logs" -> LogsScreen(viewModel)
                    "export" -> ExportScreen(viewModel)
                    "settings" -> SettingsScreen(viewModel)
                    "permissions" -> PermissionsScreen(viewModel)
                    "contact_dev" -> ContactDeveloperScreen()
                }
            }
        }
    }
}

data class NavigationMenuItem(val route: String, val label: String, val icon: ImageVector)

// ======================= HOME SCREEN =======================
@Composable
fun HomeScreen(viewModel: CRMViewModel, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val isRunning by viewModel.isServiceRunning.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val logs by viewModel.logs.collectAsState()

    val hasRuntimePerms by viewModel.hasPermissions.collectAsState()
    val hasNotifAccess by viewModel.hasNotificationAccess.collectAsState()
    val hasAccessibility by viewModel.hasAccessibility.collectAsState()
    val hasBatteryExcl by viewModel.isBatteryOptimizationIgnored.collectAsState()

    var simPhone by remember { mutableStateOf("") }
    var simName by remember { mutableStateOf("") }

    // Auto update statuses
    LaunchedEffect(Unit) {
        viewModel.refreshIntegrationStatuses()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) Color(0xFF4CAF50) else Color(0xFFF44336))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.stat_service_status),
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (isRunning) stringResource(R.string.status_running) else stringResource(R.string.status_stopped),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = {
                            if (isRunning) viewModel.stopService() else viewModel.startService()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = if (isRunning) stringResource(R.string.btn_stop_service) else stringResource(R.string.btn_start_service))
                    }
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("contacts") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.stat_saved_contacts), style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = contacts.size.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("logs") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.stat_total_logs), style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = logs.size.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Checklist status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("permissions") }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.lbl_perms_integration_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    StatusRowItem(stringResource(R.string.check_permissions), hasRuntimePerms)
                    StatusRowItem(stringResource(R.string.check_notification_access), hasNotifAccess)
                    StatusRowItem(stringResource(R.string.check_accessibility), hasAccessibility)
                    StatusRowItem(stringResource(R.string.check_battery), hasBatteryExcl)
                }
            }
        }

        // Live test area (Simulator)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.lbl_simulator_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = simPhone,
                        onValueChange = { simPhone = it },
                        label = { Text(stringResource(R.string.lbl_simulator_phone_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("simulator_phone_input")
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = simName,
                        onValueChange = { simName = it },
                        label = { Text("Lead/Sender Name (Optional for WA)") },
                        modifier = Modifier.fillMaxWidth().testTag("simulator_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (simPhone.isNotEmpty()) {
                                    viewModel.simulatePhoneCall(simPhone)
                                    Toast.makeText(context, context.getString(R.string.lbl_simulator_incoming_call_simulated), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("sim_call_button")
                        ) {
                            Icon(Icons.Default.PhoneCallback, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.lbl_simulator_incoming_call), fontSize = 11.sp, overflow = TextOverflow.Ellipsis)
                        }

                        Button(
                            onClick = {
                                if (simPhone.isNotEmpty()) {
                                    viewModel.simulateWhatsAppMessage(simPhone, simName.ifEmpty { "Testing" })
                                    Toast.makeText(context, context.getString(R.string.lbl_simulator_whatsapp_simulated), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("sim_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.lbl_simulator_whatsapp), fontSize = 11.sp, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Last saved contact card preview
        item {
            Text(
                text = stringResource(R.string.last_saved_num_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (contacts.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_saved_contacts_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val lastContact = contacts.first()
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    ListItem(
                        headlineContent = { Text(lastContact.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(lastContact.phoneNumber) },
                        trailingContent = {
                            Text(
                                text = "Source: ${lastContact.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusRowItem(title: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isOk) stringResource(R.string.state_granted) else stringResource(R.string.lbl_require_activation),
                color = if (isOk) Color(0xFF4CAF50) else Color(0xFFF44336),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (isOk) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ======================= CONTACTS SCREEN =======================
@Composable
fun ContactsScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()
    var searchTxt by remember { mutableStateOf("") }
    
    // Dialog state
    var showEditDialog by remember { mutableStateOf<ContactEntity?>(null) }
    var editName by remember { mutableStateOf("") }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var addNameInput by remember { mutableStateOf("") }
    var addPhoneInput by remember { mutableStateOf("") }

    val filteredContacts = contacts.filter { 
        it.name.contains(searchTxt, ignoreCase = true) || it.phoneNumber.contains(searchTxt)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Upper Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddContactDialog = true },
                modifier = Modifier.weight(1f).testTag("add_contact_manual_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة عميل يدوياً", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    viewModel.importDeviceContacts { imported, skipped ->
                        Toast.makeText(
                            context,
                            "تم استيراد $imported جهات اتصال بنجاز! وتم تجاوز $skipped جهات اتصال مكررة.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.weight(1f).testTag("import_contacts_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Contacts, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("استيراد جهات الجهاز", fontSize = 12.sp)
            }
        }

        OutlinedTextField(
            value = searchTxt,
            onValueChange = { searchTxt = it },
            label = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("contacts_search_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredContacts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchTxt.isEmpty()) stringResource(R.string.no_saved_contacts_yet) else stringResource(R.string.lbl_no_search_results),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts, key = { it.id }) { contact ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(contact.phoneNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            showEditDialog = contact
                                            editName = contact.name
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit_name))
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteContact(contact)
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete_client))
                                    }
                                }
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stringResource(R.string.source_label)} ${contact.source}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${stringResource(R.string.interactions_count_label)} ${contact.interactionCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Add Contact Dialog
    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("إضافة عميل جديد يدوياً") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = addNameInput,
                        onValueChange = { addNameInput = it },
                        label = { Text("اسم العميل") },
                        modifier = Modifier.fillMaxWidth().testTag("add_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = addPhoneInput,
                        onValueChange = { addPhoneInput = it },
                        label = { Text("رقم الهاتف (العميل) مع مفتاح الدولة") },
                        placeholder = { Text("+967774440982") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("add_phone_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addContactManually(addNameInput, addPhoneInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showAddContactDialog = false
                                addNameInput = ""
                                addPhoneInput = ""
                            }
                        }
                    }
                ) {
                    Text("حفظ وتصدير للبريد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Edit Dialog
    if (showEditDialog != null) {
        val contactToEdit = showEditDialog!!
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text(stringResource(R.string.dialog_edit_title)) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.client_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateContactName(contactToEdit, editName)
                        showEditDialog = null
                    }
                ) {
                    Text(stringResource(R.string.save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// ======================= LOGS SCREEN =======================
@Composable
fun LogsScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.lbl_logs_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (logs.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearLogs() }
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_clear_logs))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_logs_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(log.timestamp))
                    
                    val statusColor = when (log.status) {
                        "SUCCESS" -> Color(0xFF4CAF50)
                        "IGNORED" -> Color(0xFF9E9E9E)
                        else -> Color(0xFFF44336)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.name.ifEmpty { log.phoneNumber },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = log.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    val statusText = when (log.status) {
                                        "SUCCESS" -> stringResource(R.string.log_status_success)
                                        "IGNORED" -> stringResource(R.string.log_status_ignored)
                                        else -> stringResource(R.string.log_status_error)
                                    }
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================= EXPORT SCREEN =======================
@Composable
fun ExportScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BackupTable,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.export_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.export_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.lbl_export_available, contacts.size),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (contacts.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.lbl_no_contacts_to_export), Toast.LENGTH_SHORT).show()
                } else {
                    val csvBytes = CsvExcelExporter.exportToCsv(contacts)
                    shareCsvFile(context, csvBytes)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("export_csv_button"),
            enabled = contacts.isNotEmpty()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_export_csv))
        }
    }
}

private fun shareCsvFile(context: Context, bytes: ByteArray) {
    try {
        val outputDir = context.cacheDir
        val outputFile = File(outputDir, "crm_exported_contacts.csv")
        
        FileOutputStream(outputFile).use { fos ->
            fos.write(bytes)
        }

        val providerAuthority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, providerAuthority, outputFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.lbl_export_intent_subject))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_file)))
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ======================= PERMISSIONS SCREEN =======================
@Composable
fun PermissionsScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    
    val hasKeys by viewModel.hasPermissions.collectAsState()
    val hasNotif by viewModel.hasNotificationAccess.collectAsState()
    val hasAccessib by viewModel.hasAccessibility.collectAsState()
    val hasBattery by viewModel.isBatteryOptimizationIgnored.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshIntegrationStatuses()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_permissions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.permissions_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Contact Permission Card
        item {
            PermissionCard(
                title = stringResource(R.string.perm_contacts_title),
                desc = stringResource(R.string.perm_contacts_desc),
                isGranted = hasKeys,
                onRequest = {
                    // Handled automatically via activity launcher or showing Toast guide
                    Toast.makeText(context, "Please trigger authorization from Main Launcher screen", Toast.LENGTH_LONG).show()
                }
            )
        }

        // Notification Access Card
        item {
            PermissionCard(
                title = stringResource(R.string.perm_notifications_title),
                desc = stringResource(R.string.perm_notifications_desc),
                isGranted = hasNotif,
                onRequest = {
                    try {
                        context.startActivity(PermissionManager.getNotificationSettingsIntent())
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.lbl_notif_settings_error), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Accessibility Access Card
        item {
            PermissionCard(
                title = stringResource(R.string.perm_accessibility_title),
                desc = stringResource(R.string.perm_accessibility_desc),
                isGranted = hasAccessib,
                onRequest = {
                    try {
                        context.startActivity(PermissionManager.getAccessibilitySettingsIntent())
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.lbl_accessibility_settings_error), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Battery Optimization Card
        item {
            PermissionCard(
                title = stringResource(R.string.perm_battery_title),
                desc = stringResource(R.string.perm_battery_desc),
                isGranted = hasBattery,
                onRequest = {
                    try {
                        context.startActivity(PermissionManager.getBatteryOptimizationSettingsIntent(context))
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.lbl_battery_settings_error), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun PermissionCard(title: String, desc: String, isGranted: Boolean, onRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(
                            if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFF44336).copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGranted) "Active" else stringResource(R.string.lbl_require_activation),
                        color = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.btn_request))
                }
            }
        }
    }
}

// ======================= SETTINGS SCREEN =======================
@Composable
fun SettingsScreen(viewModel: CRMViewModel) {
    val autoSaveCalls by viewModel.autoSaveCalls.collectAsState()
    val autoSaveSms by viewModel.autoSaveSms.collectAsState()
    val autoSaveWhatsApp by viewModel.autoSaveWhatsApp.collectAsState()
    val namePrefix by viewModel.namePrefix.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Toggles Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.lbl_settings_granted_channels),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.setting_auto_save_calls))
                        Switch(checked = autoSaveCalls, onCheckedChange = { viewModel.toggleAutoSaveCalls(it) })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.setting_auto_save_sms))
                        Switch(checked = autoSaveSms, onCheckedChange = { viewModel.toggleAutoSaveSms(it) })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.setting_auto_save_whatsapp))
                        Switch(checked = autoSaveWhatsApp, onCheckedChange = { viewModel.toggleAutoSaveWhatsApp(it) })
                    }
                }
            }
        }

        // Prefix Config Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.setting_prefix_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = namePrefix,
                        onValueChange = { viewModel.updateNamePrefix(it) },
                        label = { Text(stringResource(R.string.setting_prefix_hint)) },
                        modifier = Modifier.fillMaxWidth().testTag("prefix_input")
                    )
                }
            }
        }

        // Gmail Cloud Sync Config Card
        item {
            val userGmail by viewModel.userGmail.collectAsState()
            val autoGmailSync by viewModel.autoGmailSync.collectAsState()
            val context = LocalContext.current
            val contacts by viewModel.contacts.collectAsState()
            var emailInput by remember(userGmail) { mutableStateOf(userGmail ?: "") }
            var isEditingEmail by remember { mutableStateOf(false) }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إعدادات الربط والمزامنة مع Gmail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بريد الحفظ النشط", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            if (isEditingEmail) {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    suffix = {
                                        IconButton(onClick = {
                                            viewModel.updateUserGmail(emailInput)
                                            isEditingEmail = false
                                        }) {
                                            Icon(Icons.Default.Check, contentDescription = "حفظ")
                                        }
                                    }
                                )
                            } else {
                                Text(
                                    text = userGmail ?: "لم يتم الربط بعد",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (!isEditingEmail) {
                            TextButton(onClick = { isEditingEmail = true }) {
                                Text("تعديل")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("المزامنة التلقائية للتصدير", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "تصدير فوري بنسخة احتياطية للبريد تلقائياً عند رصد أي عميل جديد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = autoGmailSync, onCheckedChange = { viewModel.toggleAutoGmailSync(it) })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (contacts.isEmpty()) {
                                Toast.makeText(context, "لا توجد جهات اتصال محلية لتصديرها", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val csvFile = File(context.cacheDir, "CRM_Contacts_Gmail_Backup.csv")
                            try {
                                val out = FileOutputStream(csvFile)
                                out.write(0xEF)
                                out.write(0xBB)
                                out.write(0xBF)
                                
                                val writer = out.bufferedWriter()
                                writer.write("الاسم,رقم الهاتف,المصدر,تاريخ الحفظ,عدد التفاعلات\n")
                                contacts.forEach { contact ->
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(contact.savedTimestamp))
                                    writer.write("\"${contact.name}\",\"${contact.phoneNumber}\",\"${contact.source}\",\"$dateStr\",${contact.interactionCount}\n")
                                }
                                writer.flush()
                                writer.close()
                                
                                val authority = "${context.packageName}.fileprovider"
                                val uri = FileProvider.getUriForFile(context, authority, csvFile)
                                
                                val gmailIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(userGmail ?: ""))
                                    putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية لقاعدة بيانات CRM للعملاء - الحفظ التلقائي")
                                    putExtra(Intent.EXTRA_TEXT, "مرحباً جمال،\n\nنرفق لكم بموجب هذا ملف CSV المحدث لكامل العملاء المسجلين في نظام الحفظ التلقائي CRM للتواصل والمتابعة.\n\nإجمالي جهات الاتصال: ${contacts.size}\nتاريخ التصدير: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                
                                context.startActivity(Intent.createChooser(gmailIntent, "تصدير يدوي إلى Gmail"))
                                
                                viewModel.insertManualBackupLog(
                                    "تم تصدير نسخة احتياطية لكامل جهات الاتصال (${contacts.size} عميل) وإرسالها يدوياً لحساب Gmail: ${userGmail ?: ""}"
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "حدث خطأ أثناء تصدير الملف: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("gmail_sync_now_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مزامنة وتصدير يدوي فوري للبريد (Gmail)")
                    }
                }
            }
        }
    }
}

// ======================= CONTACT DEVELOPER SCREEN =======================
@Composable
fun ContactDeveloperScreen() {
    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Developer Profile Branding Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.dev_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.dev_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.dev_contact_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Direct Email Quick Action Button
        item {
            Button(
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("applicationsdeveloper6838@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_subject))
                    }
                    try {
                        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.btn_send_email)))
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.lbl_email_app_not_found), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("mail_dev_button")
            ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("applicationsdeveloper6838@gmail.com")
            }
        }

        // Social Networks Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    text = " SOCIAL NETWORKS ", // static spacing divider
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Divider(modifier = Modifier.weight(1f))
            }
        }

        // Social Platform List
        // We render beautifully designed, dynamic cards with actual platform colors and smooth clicking listeners!
        val socialPlatforms = listOf(
            SocialPlatformData(
                name = "WhatsApp",
                desc = context.getString(R.string.link_whatsapp),
                url = "https://wa.me/774440982",
                icon = Icons.Default.Chat,
                accentColor = Color(0xFF25D366)
            ),
            SocialPlatformData(
                name = "Telegram",
                desc = context.getString(R.string.link_telegram),
                url = "https://t.me/Gamalalhwish",
                icon = Icons.Default.Send,
                accentColor = Color(0xFF0088CC)
            ),
            SocialPlatformData(
                name = "LinkedIn",
                desc = context.getString(R.string.link_linkedin),
                url = "https://www.linkedin.com/in/gamal-alhwish",
                icon = Icons.Default.Work,
                accentColor = Color(0xFF0077B5)
            ),
            SocialPlatformData(
                name = "Facebook",
                desc = context.getString(R.string.link_facebook),
                url = "https://www.facebook.com/jmal.alhwysh.2025",
                icon = Icons.Default.Facebook,
                accentColor = Color(0xFF1877F2)
            ),
            SocialPlatformData(
                name = "Twitter / X",
                desc = context.getString(R.string.link_twitter),
                url = "https://x.com/alhwysh787472?s=09",
                icon = Icons.Default.Share,
                accentColor = Color(0xFF1DA1F2)
            ),
            SocialPlatformData(
                name = "TikTok",
                desc = context.getString(R.string.link_tiktok),
                url = "https://www.tiktok.com/@GamalAlmaqtaryForTechServices",
                icon = Icons.Default.QueueMusic,
                accentColor = Color(0xFF010101)
            ),
            SocialPlatformData(
                name = "Instagram",
                desc = context.getString(R.string.link_instagram),
                url = "https://www.instagram.com/gamal_almaqtary_tech_services",
                icon = Icons.Default.CameraAlt,
                accentColor = Color(0xFFE1306C)
            )
        )

        items(socialPlatforms) { platform ->
            SocialMediaCard(platform = platform) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(platform.url))
                    context.startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: Cannot open browser link.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class SocialPlatformData(
    val name: String,
    val desc: String,
    val url: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun SocialMediaCard(platform: SocialPlatformData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("social_card_${platform.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon backdrop circle styled with brand colors
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(platform.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = platform.icon,
                    contentDescription = platform.name,
                    tint = platform.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = platform.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = platform.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Direction navigation arrow
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
