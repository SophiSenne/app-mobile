package com.hanger.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.hanger.ui.theme.HangerBeige
import com.example.hanger.ui.theme.HangerBorder
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerGray
import com.example.hanger.ui.theme.HangerInputBg
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerTextMuted
import com.hanger.app.data.model.CategoryDto
import com.hanger.app.data.model.TypeDto
import com.hanger.app.data.repository.ActionResult
import com.hanger.app.data.repository.CategoriesRepository
import com.hanger.app.data.repository.CategoriesResult
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.ui.components.ErrorBanner
import com.hanger.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    postsRepository: PostsRepository = remember { PostsRepository() },
    categoriesRepository: CategoriesRepository = remember { CategoriesRepository() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var temperatureInput by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedTypeId by remember { mutableStateOf<Int?>(null) }

    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }

    val availableTypes = remember(selectedCategoryId, categories) {
        categories.find { it.id == selectedCategoryId }?.types.orEmpty()
    }

    LaunchedEffect(Unit) {
        when (val result = categoriesRepository.getCategories()) {
            is CategoriesResult.Success -> categories = result.categories
            is CategoriesResult.Error -> { /* silencia: campos ficam ocultos */ }
        }
    }

    val cameraImageUri = remember { createCameraImageUri(context) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { imageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) imageUri = cameraImageUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) cameraLauncher.launch(cameraImageUri) }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = HangerCream,
        topBar = {
            TopAppBar(
                title = {
                    Text("Novo look", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HangerInk)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = HangerInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HangerCream)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let { ErrorBanner(message = it) }

            // ── Imagem ────────────────────────────────────────────────────────
            ImagePickerArea(imageUri = imageUri, onClick = { showImageSourceSheet = true })

            // ── Título ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título *", color = HangerGray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Descrição ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Descrição (opcional)", color = HangerGray) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Temperatura ───────────────────────────────────────────────────
            OutlinedTextField(
                value = temperatureInput,
                onValueChange = { v ->
                    if (v.isEmpty() || v == "-" || v.toDoubleOrNull() != null) temperatureInput = v
                },
                label = { Text("Temperatura (°C, opcional)", color = HangerGray) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Thermostat, contentDescription = null, tint = HangerGray, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Categorias ────────────────────────────────────────────────────
            if (categories.isNotEmpty()) {
                SectionLabel("Categoria")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { category ->
                        val selected = selectedCategoryId == category.id
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedCategoryId = if (selected) null else category.id
                                selectedTypeId = null
                            },
                            label = { Text(category.name ?: "—", fontSize = 13.sp) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = chipColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // ── Tipos (aparece apenas quando categoria está selecionada) ──
                if (availableTypes.isNotEmpty()) {
                    SectionLabel("Tipo")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableTypes.forEach { type ->
                            val selected = selectedTypeId == type.id
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedTypeId = if (selected) null else type.id
                                },
                                label = { Text(type.name ?: "—", fontSize = 13.sp) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = chipColors(),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Publicar ──────────────────────────────────────────────────────
            PrimaryButton(
                text = "Publicar look",
                isLoading = isLoading,
                onClick = {
                    val uri = imageUri
                    when {
                        uri == null -> errorMessage = "Selecione uma imagem para continuar"
                        title.isBlank() -> errorMessage = "O título é obrigatório"
                        else -> {
                            errorMessage = null
                            isLoading = true
                            scope.launch {
                                val result = postsRepository.createPost(
                                    userId = userId,
                                    imageUri = uri,
                                    context = context,
                                    title = title.trim(),
                                    caption = caption.trim().ifBlank { null },
                                    categoryId = selectedCategoryId,
                                    typeId = selectedTypeId,
                                    temperature = temperatureInput.toDoubleOrNull()
                                )
                                isLoading = false
                                when (result) {
                                    is ActionResult.Success -> onPostCreated()
                                    is ActionResult.Error -> errorMessage = result.message
                                }
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
        }
    }

    // ── Bottom sheet seleção de origem da imagem ───────────────────────────────
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = sheetState,
            containerColor = HangerCream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    "Adicionar foto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = HangerInk,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ImageSourceOption(
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = HangerInk) },
                    label = "Tirar foto",
                    onClick = {
                        showImageSourceSheet = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                HorizontalDivider(color = HangerBorder, thickness = 0.5.dp)

                ImageSourceOption(
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = HangerInk) },
                    label = "Escolher da galeria",
                    onClick = {
                        showImageSourceSheet = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Subcomposables ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = HangerInk,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun ImagePickerArea(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(HangerBeige)
            .border(1.dp, HangerBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Imagem selecionada",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = "Trocar foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = HangerTextMuted, modifier = Modifier.size(40.dp))
                Text("Toque para adicionar foto", fontSize = 14.sp, color = HangerTextMuted)
            }
        }
    }
}

@Composable
private fun ImageSourceOption(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        icon()
        Text(label, fontSize = 15.sp, color = HangerInk)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = HangerInk,
    selectedLabelColor = Color.White,
    selectedLeadingIconColor = Color.White,
    containerColor = HangerInputBg,
    labelColor = HangerInk
)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = HangerInk,
    unfocusedBorderColor = HangerBorder,
    focusedContainerColor = HangerInputBg,
    unfocusedContainerColor = HangerInputBg
)

private fun createCameraImageUri(context: Context): Uri {
    val cacheDir = File(context.cacheDir, "camera_images").also { it.mkdirs() }
    val file = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
