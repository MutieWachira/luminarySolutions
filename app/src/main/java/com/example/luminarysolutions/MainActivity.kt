package com.example.luminarysolutions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.dashboard.CampaignDashboardScreen
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.navigation.Routes
import com.example.luminarysolutions.ui.register.RegisterScreen
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminarySolutionsTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN
                ) {
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            viewModel = viewModel(),
                            onLoginSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onRegisterClick = {
                                navController.navigate(Routes.REGISTER)
                            }
                        )
                    }
                    composable(Routes.REGISTER) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onLoginClick = {
                                navController.popBackStack()
                            }
                        )
                    }


                    composable(Routes.HOME) {
                        CampaignDashboardScreen(
                            onCampaignSelected = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel? = null,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Local fallback state for preview
    val emailLocal = remember { mutableStateOf("") }
    val passwordLocal = remember { mutableStateOf("") }
    val errorLocal = remember { mutableStateOf("") }
    val isLoadingLocal = remember { mutableStateOf(false) }
    val loginSuccessLocal = remember { mutableStateOf(false) }

    // Use real ViewModel state if provided, otherwise use local preview state
    val email by (viewModel?.email ?: emailLocal)
    val password by (viewModel?.password ?: passwordLocal)
    val errorMessage by (viewModel?.errorMessage ?: errorLocal)
    val isLoading by (viewModel?.isLoading ?: isLoadingLocal)
    val loginSuccess by (viewModel?.loginSuccess ?: loginSuccessLocal)

    // Provide functions that either forward to the ViewModel or update local state
    val onEmailChange: (String) -> Unit = { value ->
        if (viewModel != null) viewModel.onEmailChange(value) else emailLocal.value = value
    }
    val onPasswordChange: (String) -> Unit = { value ->
        if (viewModel != null) viewModel.onPasswordChange(value) else passwordLocal.value = value
    }
    val onLoginClickFn: () -> Unit = {
        if (viewModel != null) viewModel.onLoginClick()
        else {
            // simple preview behavior: simulate success
            isLoadingLocal.value = true
            // immediate success for preview
            isLoadingLocal.value = false
            loginSuccessLocal.value = true
        }
    }
    val onNavigationCompleteFn: () -> Unit = {
        if (viewModel != null) viewModel.onNavigationComplete()
        else loginSuccessLocal.value = false
    }

    var passwordVisible by remember { mutableStateOf(false) }

    if (loginSuccess) {
        LaunchedEffect(loginSuccess) {
            onLoginSuccess()
            onNavigationCompleteFn()
        }
    }
    if (errorMessage.isNotEmpty()) {
        LaunchedEffect(errorMessage) {
            onNavigationCompleteFn()
        }
    }


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Login to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            // FIX: Pass the new value 'it' directly to the ViewModel function
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = {Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginClickFn() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(0.9f).height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Login")
            }
        }

        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Sign Up")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LuminarySolutionsTheme {
        LoginScreen(
            onLoginSuccess = {},
            onRegisterClick = {})
    }
}
