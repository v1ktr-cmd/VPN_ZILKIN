package com.khti.fastikvpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.ThemeMode
import com.khti.fastikvpn.core.theme.FastikTheme
import com.khti.fastikvpn.core.theme.FastikVpnTheme
import com.khti.fastikvpn.ui.mainscreen.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { viewModel.themeMode.value == null }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            FastikVpnTheme(themeMode = themeMode ?: ThemeMode.SYSTEM) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(onThemeSelected = viewModel::onThemeSelected)
                }
            }
        }
    }
}

@Composable
private fun MainContent(onThemeSelected: (ThemeMode) -> Unit) {
    var currentScreen by remember { mutableStateOf(0) }
    var isConnecting by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Логика запуска подключения с защитой от повторного нажатия
    val startConnecting = {
        if (!isConnecting && !isConnected) {
            isConnecting = true
            scope.launch {
                delay(2000) // Имитация подключения (2 секунды)
                isConnecting = false
                isConnected = true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentScreen) {
                0 -> HomeScreen(
                    isConnecting = isConnecting,
                    isConnected = isConnected,
                    onClickCircle = {
                        if (isConnecting) {
                            // Если уже идет загрузка — игнорируем клик
                        } else if (isConnected) {
                            isConnected = false // Отключаем
                        } else {
                            startConnecting() // Включаем
                        }
                    }
                )
                1 -> SettingsScreen(onThemeSelected)
                2 -> ProfileScreen()
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedNavButton(
                        iconResId = R.drawable.ic_home,
                        isSelected = currentScreen == 0
                    ) { currentScreen = 0 }

                    AnimatedNavButton(
                        iconResId = R.drawable.ic_settings,
                        isSelected = currentScreen == 1
                    ) { currentScreen = 1 }

                    AnimatedNavButton(
                        iconResId = R.drawable.ic_user,
                        isSelected = currentScreen == 2
                    ) { currentScreen = 2 }
                }
            }
        }
    }
}

@Composable
private fun AnimatedNavButton(
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.8f else 1f, tween(100), label = "scale")

    val iconSize = if (isSelected) 32.dp else 28.dp
    val iconAlpha = if (isSelected) 1f else 0.6f

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "Nav Icon",
            modifier = Modifier
                .size(iconSize)
                .alpha(iconAlpha)
        )
    }
}

@Composable
private fun HomeScreen(
    isConnecting: Boolean,
    isConnected: Boolean,
    onClickCircle: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(Dimens.ScreenPadding)) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = FastikTheme.colors.title,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 24.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(48.dp))

            Box(
                modifier = Modifier
                    .padding(Dimens.SpacingXl)
                    .size(Dimens.FirstCircle)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(FastikTheme.colors.connectGradientStart, FastikTheme.colors.connectGradientEnd)))
                    .clickable { onClickCircle() },
                contentAlignment = Alignment.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(64.dp))
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_shield),
                        contentDescription = "VPN Shield",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn)
                    )
                }
            }

            Text(
                text = when {
                    isConnecting -> "Подключение..."
                    isConnected -> "Защищено"
                    else -> "Нажмите для подключения"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingsScreen(onThemeSelected: (ThemeMode) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(Dimens.ScreenPadding)) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            color = FastikTheme.colors.title,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 24.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.theme_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onThemeSelected(ThemeMode.DARK) }) { Text(stringResource(R.string.theme_dark)) }
                Button(onClick = { onThemeSelected(ThemeMode.LIGHT) }) { Text(stringResource(R.string.theme_light)) }
                Button(onClick = { onThemeSelected(ThemeMode.SYSTEM) }) { Text(stringResource(R.string.theme_system)) }
            }
        }
    }
}

@Composable
private fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize().padding(Dimens.ScreenPadding)) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
            color = FastikTheme.colors.title,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 24.dp)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Информация о профиле появится позже",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
//коммент для новой ветки