package com.wuji.app.ui.screen.source

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.data.SubscribeSourceRepository
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.screen.SourceMarketScreen
import com.wuji.app.ui.screen.SourceMyScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 订阅源管理页 - 对齐原项目 source/ManageSource.vue:展示已导入源,入口至市场/我的源 */
object SourceManageScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<SourceManageScreenModel>()
        val state by model.state.collectAsState()
        val navigator = LocalNavigator.current
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "订阅源管理",
                    onBack = { navigator?.pop() },
                    actions = {
                        IconButton(onClick = { navigator?.push(SourceMyScreen) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "我的源")
                        }
                        IconButton(onClick = { navigator?.push(SourceMarketScreen) }) {
                            Icon(Icons.Outlined.Store, contentDescription = "源市场")
                        }
                    },
                )
            },
        ) { p ->
            if (state.sources.isEmpty()) {
                EmptyState("暂无订阅源,请前往市场导入或添加源链接", modifier = Modifier.fillMaxSize().padding(p))
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(p)) {
                    items(state.sources, key = { it.url }) { src ->
                        ListItem(
                            headlineContent = { Text(src.detail.name) },
                            supportingContent = { Text("${src.detail.urls.size} 个子源", style = MaterialTheme.typography.bodySmall) },
                        )
                    }
                }
            }
        }
    }
}

data class SourceManageState(val sources: List<com.wuji.app.source.model.SubscribeSource> = emptyList())

class SourceManageScreenModel(repo: SubscribeSourceRepository) :
    StateScreenModel<SourceManageState>(SourceManageState()) {
    init { mutableState.value = SourceManageState(repo.loadAll()) }
}
