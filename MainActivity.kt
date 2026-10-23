package com.example.ioslauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

data class AppItem(val label: String, val pkg: String, val icon: ImageBitmap)

private const val COLS = 4
private const val ROWS = 6

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apps = loadApps(this)
        setContent { HomeScreen(apps) { launch(it) } }
    }

    private fun launch(app: AppItem) {
        packageManager.getLaunchIntentForPackage(app.pkg)?.let { startActivity(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent) // Home pressed: stay on the home screen
    }
}

fun loadApps(ctx: Context): List<AppItem> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .map {
            AppItem(
                it.loadLabel(pm).toString(),
                it.activityInfo.packageName,
                it.loadIcon(pm).toBitmap(160, 160).asImageBitmap()
            )
        }
        .sortedBy { it.label.lowercase() }
}

@Composable
fun Icon(app: AppItem, showLabel: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            app.icon, app.label,
            Modifier.size(60.dp).clip(RoundedCornerShape(14.dp))
        )
        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                app.label, color = Color.White, fontSize = 11.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center, modifier = Modifier.width(68.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(all: List<AppItem>, onOpen: (AppItem) -> Unit) {
    val dock = all.take(4)
    val pages = all.drop(4).chunked(COLS * ROWS).ifEmpty { listOf(emptyList()) }
    val pager = rememberPagerState { pages.size }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        HorizontalPager(pager, Modifier.weight(1f).padding(top = 16.dp)) { p ->
            Column(
                Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pages[p].chunked(COLS).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { Icon(it, true) { onOpen(it) } }
                        repeat(COLS - row.size) { Spacer(Modifier.width(68.dp)) }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) {
                Box(
                    Modifier.padding(3.dp).size(7.dp).clip(CircleShape)
                        .background(if (it == pager.currentPage) Color.White else Color.White.copy(0.4f))
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.25f)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dock.forEach { Icon(it, false) { onOpen(it) } }
        }
    }
}
