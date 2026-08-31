package com.wuji.app.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.AppTopBar

/** 用户中心 - 对齐原项目 auth/User.vue */
object UserScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "用户中心", onBack = { navigator?.pop() }) }) { p ->
            Column(
                Modifier.fillMaxSize().padding(p).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("未登录", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { navigator?.push(LoginScreen) }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("去登录")
                }
            }
        }
    }
}

/** 登录页 - 对齐原项目 auth/Login.vue */
object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var account by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        Scaffold(topBar = { AppTopBar(title = "登录", onBack = { navigator?.pop() }) }) { p ->
            Column(
                Modifier.fillMaxSize().padding(p).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("账号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                )
                Button(onClick = { navigator?.pop() }) { Text("登录") }
            }
        }
    }
}
