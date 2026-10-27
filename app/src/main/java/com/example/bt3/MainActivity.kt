package com.example.bt3
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.sin

// ======================= MÀU SẮC =======================
val BlueTitle = Color(0xFF2196F3)
val ItemBg = Color(0xFFBBDEFB)
val BoxLight = Color(0xFFA4C2F4)
val BoxDark = Color(0xFF4F8CF0)
val RowBg = Color(0xFFF2F4F7)

// ======================= ROUTES =======================
object Routes {
    const val READY = "ready"
    const val LIST = "list"
    const val TEXT = "text"
    const val IMAGE = "image"
    const val TEXT_FIELD = "textfield"
    const val PASSWORD = "password"
    const val COLUMN = "column"
    const val ROW = "row"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = Color.White
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// ======================= NAVIGATION =======================
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.READY) {
        composable(Routes.READY) {
            ReadyScreen(onReady = {
                navController.navigate(Routes.LIST) {
                    popUpTo(Routes.READY) { inclusive = true } // bấm back không quay lại màn Start
                }
            })
        }
        composable(Routes.LIST) { ComponentsListScreen(navController) }
        composable(Routes.TEXT) { TextDetailScreen { navController.popBackStack() } }
        composable(Routes.IMAGE) { ImageDetailScreen { navController.popBackStack() } }
        composable(Routes.TEXT_FIELD) { TextFieldDetailScreen { navController.popBackStack() } }
        composable(Routes.PASSWORD) { PasswordFieldDetailScreen { navController.popBackStack() } }
        composable(Routes.COLUMN) { ColumnLayoutScreen { navController.popBackStack() } }
        composable(Routes.ROW) { RowLayoutScreen { navController.popBackStack() } }
    }
}

// ======================= MÀN HÌNH 1: I'M READY =======================
@Composable
fun ReadyScreen(onReady: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        ComposeLogo(Modifier.size(140.dp))
        Spacer(Modifier.height(48.dp))
        Text("Jetpack Compose", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Jetpack Compose is a modern UI toolkit for building native Android " +
                    "applications using a declarative programming approach.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(Modifier.weight(1.3f))
        Button(
            onClick = onReady,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueTitle),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("I'm ready", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** Vẽ logo Jetpack Compose bằng Canvas (không cần file ảnh) */
@Composable
fun ComposeLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2
        fun vertex(i: Int, radius: Float): Offset {
            val angle = Math.toRadians((-90 + 60 * i).toDouble())
            return Offset(c.x + radius * cos(angle).toFloat(), c.y + radius * sin(angle).toFloat())
        }
        val v = (0..5).map { vertex(it, r) }
        fun poly(pts: List<Offset>) = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
        drawPath(poly(listOf(v[5], v[0], v[1], c)), Color(0xFF3DDC84))  // trên - xanh lá
        drawPath(poly(listOf(c, v[1], v[2], v[3])), Color(0xFF4285F4))  // phải - xanh dương
        drawPath(poly(listOf(c, v[3], v[4], v[5])), Color(0xFF073042))  // trái - xanh đậm
        val inner = (0..5).map { vertex(it, r * 0.45f) }
        val innerPath = poly(inner)
        drawPath(innerPath, Color(0xFFD7EFFE))
        drawPath(innerPath, Color(0xFF073042), style = Stroke(width = r * 0.08f))
    }
}

// ======================= MÀN HÌNH 2: DANH SÁCH =======================
data class ComponentItem(val title: String, val desc: String, val route: String)

@Composable
fun ComponentsListScreen(navController: NavHostController) {
    val sections = listOf(
        "Display" to listOf(
            ComponentItem("Text", "Displays text", Routes.TEXT),
            ComponentItem("Image", "Displays an image", Routes.IMAGE)
        ),
        "Input" to listOf(
            ComponentItem("TextField", "Input field for text", Routes.TEXT_FIELD),
            ComponentItem("PasswordField", "Input field for passwords", Routes.PASSWORD)
        ),
        "Layout" to listOf(
            ComponentItem("Column", "Arranges elements vertically", Routes.COLUMN),
            ComponentItem("Row", "Arranges elements horizontally", Routes.ROW)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "UI Components List",
            color = BlueTitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )
        sections.forEach { (header, items) ->
            Text(
                header,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
            items.forEach { item ->
                ComponentCard(item) { navController.navigate(item.route) }
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ComponentCard(item: ComponentItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ItemBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(item.desc, fontSize = 14.sp)
    }
}

// ======================= THANH TIÊU ĐỀ CHUNG =======================
@Composable
fun DetailTopBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Text("<", color = BlueTitle, fontSize = 26.sp)   // nút quay lại
        }
        Text(
            title,
            color = BlueTitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun DetailScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(title, onBack)
        content()
    }
}

// ======================= CHI TIẾT: TEXT =======================
@Composable
fun TextDetailScreen(onBack: () -> Unit) {
    val styledText = buildAnnotatedString {
        append("The ")
        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append("quick") }
        append(" ")
        withStyle(SpanStyle(color = Color(0xFFB8860B), fontSize = 36.sp)) { append("Brown") }
        append("\nfox ")
        withStyle(SpanStyle(letterSpacing = 6.sp)) { append("jumps") }
        append(" ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) { append("over") }
        append("\n")
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append("the") }
        append(" ")
        withStyle(SpanStyle(fontFamily = FontFamily.Cursive, fontStyle = FontStyle.Italic)) { append("lazy") }
        append(" dog.")
    }

    DetailScaffold("Text Detail", onBack) {
        Spacer(Modifier.weight(1f))
        Text(
            text = styledText,
            fontSize = 28.sp,
            lineHeight = 46.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        HorizontalDivider(Modifier.padding(top = 16.dp, start = 24.dp))
        Spacer(Modifier.weight(1.3f))
    }
}

// ======================= CHI TIẾT: IMAGE =======================
@Composable
fun ImageDetailScreen(onBack: () -> Unit) {
    val imageUrl = "https://giaothongvantaitphcm.edu.vn/wp-content/uploads/2025/01/Logo-GTVT.png"

    DetailScaffold("Images", onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh 1: tải từ Internet (dùng thư viện Coil)
            AsyncImage(
                model = imageUrl,
                contentDescription = "Ảnh từ URL",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Text(
                imageUrl,
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Ảnh 2: ảnh có sẵn trong app (res/drawable/uth_campus.jpg)
            Image(
                painter = painterResource(id = R.drawable.uth_campus),
                contentDescription = "Ảnh trong app",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Text("In app", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// ======================= CHI TIẾT: TEXTFIELD =======================
@Composable
fun TextFieldDetailScreen(onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }

    DetailScaffold("TextField", onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Thông tin nhập") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = text.ifEmpty { "Tự động cập nhật dữ liệu theo textfield" },
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(120.dp))
        }
    }
}

// ======================= CHI TIẾT: PASSWORDFIELD =======================
@Composable
fun PasswordFieldDetailScreen(onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    DetailScaffold("PasswordField", onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Nhập mật khẩu") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (visible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { visible = !visible }) {
                        Text(if (visible) "Ẩn" else "Hiện", color = BlueTitle)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (password.isEmpty()) "Mật khẩu được ẩn khi nhập"
                else "Độ dài mật khẩu: ${password.length} ký tự",
                color = Color.Red,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(120.dp))
        }
    }
}

// ======================= CHI TIẾT: COLUMN LAYOUT =======================
@Composable
fun ColumnLayoutScreen(onBack: () -> Unit) {
    DetailScaffold("Column Layout", onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RowBg)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (index % 2 == 1) BoxDark else BoxLight)
                )
            }
        }
    }
}

// ======================= CHI TIẾT: ROW LAYOUT =======================
@Composable
fun RowLayoutScreen(onBack: () -> Unit) {
    DetailScaffold("Row Layout", onBack) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RowBg)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(BoxLight, BoxDark, BoxLight).forEach { color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}