package com.example.luminarysolutions.ui.client

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

/**
 * ClientPersonalDetailsScreen: Allows clients to view and edit their personal information.
 * Handles name, email, phone, bio, profile image, and secure password updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPersonalDetailsScreen(
    navController: NavController,
    viewModel: ClientPersonalDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Form states
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Password states
    var currentPasswordForEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswordUpdate by remember { mutableStateOf(false) }
    var showEmailUpdate by remember { mutableStateOf(false) }

    // Initialize form with user data
    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            name = it.name
            email = it.email
            phone = it.phoneNumber
            bio = it.bio
        }
    }

    // Handle success/error messages
    LaunchedEffect(uiState.updateSuccess, uiState.errorMessage) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully")
            viewModel.clearSuccess()
            showPasswordUpdate = false
            showEmailUpdate = false
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            currentPasswordForEmail = ""
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Information", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA))
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile Image Section
                ProfileImageSection(
                    imageUrl = uiState.user?.profileImageUrl,
                    selectedUri = selectedImageUri,
                    onEditClick = { imagePicker.launch("image/*") }
                )

                // General Information Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("General Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            leadingIcon = { Icon(Icons.Default.EditNote, null) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { viewModel.updateProfile(name, phone, bio, selectedImageUri) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !uiState.isUpdating,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            else Text("Save General Info")
                        }
                    }
                }

                // Email Update Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Email Address", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { showEmailUpdate = !showEmailUpdate }) {
                                Text(if (showEmailUpdate) "Cancel" else "Change")
                            }
                        }

                        if (!showEmailUpdate) {
                            Text(email, color = Color.Gray)
                        } else {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("New Email") },
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currentPasswordForEmail,
                                onValueChange = { currentPasswordForEmail = it },
                                label = { Text("Confirm with Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = { viewModel.updateEmail(email, currentPasswordForEmail) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = !uiState.isUpdating && currentPasswordForEmail.isNotBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Update Email")
                            }
                        }
                    }
                }

                // Password Update Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Security", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { showPasswordUpdate = !showPasswordUpdate }) {
                                Text(if (showPasswordUpdate) "Cancel" else "Change Password")
                            }
                        }

                        if (showPasswordUpdate) {
                            PasswordField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = "Current Password"
                            )
                            PasswordField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "New Password"
                            )
                            PasswordField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm New Password"
                            )

                            val passwordsMatch = newPassword == confirmPassword && newPassword.isNotBlank()
                            
                            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && !passwordsMatch) {
                                Text("Passwords do not match", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.updatePassword(currentPassword, newPassword) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = !uiState.isUpdating && passwordsMatch && currentPassword.isNotBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Update Password")
                            }
                        } else {
                            Text("••••••••", color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileImageSection(
    imageUrl: String?,
    selectedUri: Uri?,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(120.dp),
                shadowElevation = 4.dp
            ) {
                if (selectedUri != null) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "New Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }

            SmallFloatingActionButton(
                onClick = onEditClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Edit Image", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(12.dp)
    )
}
