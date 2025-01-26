package com.example.geminiapikey

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onActionClick: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidePanel (
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        QuickTapUIScreen(
            onActionClick = onActionClick,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

@Composable
fun QuickTapUIScreen(onActionClick: (String) -> Unit, onMenuClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val buttonWidth = screenWidth / 2
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
Box(
    Modifier
        .fillMaxSize()
        .background(
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFC1E3).copy(alpha = 0.6f), // Light pink
                Color(0xFF101010) // Dark background
            ),
            center = Offset(screenWidthPx / 2, screenHeightPx / 2), // Center in pixels
            radius = (screenWidthPx + screenHeightPx) / 5f // Adjust radius as needed
        )
        )
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeContent.union(WindowInsets.navigationBars)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(40.dp)
                    .background(
                        color = Color.DarkGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ){
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
            Image(
                painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_1),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
            )
            ProfilePicture(photoUrl = user?.photoUrl)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (user != null) {
            Text(
                text = "Hi, ${user.displayName}",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "How may I help\n you today?",
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonWidth + 4.dp)
            ) {
                ActionButton1("Chat with Bot", R.drawable.frame_5, Color(192, 159, 248), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth / 2))
                Spacer(Modifier.height(4.dp))
                ActionButton1("Search by Image", R.drawable.frame_5__2_, Color(176,176,176), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth / 2))
            }
            Spacer(Modifier.padding(2.dp))
            Column(
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonWidth + 4.dp)
                    .padding(2.dp)
            ) {
                ActionButton2("Talk \nwith Bot", R.drawable.frame_5__1_, Color(254,196,221), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth + 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FunOption()
        }
    }
}

}

@Composable
fun ActionButton1(
    text: String,
    image: Int,
    color: Color,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, shape = RoundedCornerShape(24.dp))
            .clickable { onActionClick(text) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column{
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = "Action Icon",
                    modifier = Modifier.size(32.dp)
                )
                Image(
                    painter = painterResource(R.drawable._icon__arrow_forward_),
                    contentDescription = "Forward Arrow",
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun ActionButton2(
    text: String,
    image: Int,
    color: Color,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, shape = RoundedCornerShape(24.dp))
            .clickable { onActionClick(text) }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = "Action Icon",
                    modifier = Modifier.size(32.dp)
                )
                Image(
                    painter = painterResource(R.drawable._icon__arrow_forward_),
                    contentDescription = "Forward Arrow",
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = text,
                fontSize = 36.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun SidePanel(onClose: () -> Unit) {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.statusBars())
    val heightPx = insets?.top ?: 0
    val density = LocalDensity.current.density
    val sidePanelHeight = heightPx / density
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sidePanelWidth = screenWidth * 0.75f // 75% of the screen width
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    onClose()
                }
            )
    ) {
        ModalDrawerSheet(
            drawerContainerColor = Color(52, 71, 75, 240), // Transparent to show the background image
            modifier = Modifier
                .width(sidePanelWidth)
                .padding(top = sidePanelHeight.dp)
                .clickable(
                    onClick = {
                        // Do nothing on click, to avoid closing the side panel
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Overlay content on top of the blurred background
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    // Title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), // Add padding to align with content,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_1),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.padding(start = 8.dp).size(screenWidth * 0.5f)
                        )
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Menu items
                    MenuItem("Home", Icons.Outlined.Home)
                    MenuItem2("${user?.displayName}", photoUrl = user?.photoUrl)
                    MenuItem("Privacy Policy", Icons.Outlined.Lock)
                    MenuItem2("Help Center",R.drawable.icons8_help_32)
                    MenuItem2("Terms of Use",R.drawable.icons8_analyze_64)
                    Spacer(modifier = Modifier.weight(1f)) // Pushes the "Logout" button to the bottom

                    // Logout button with styling
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Handle click for menu item */ }
                            .padding(vertical = 16.dp, horizontal = 32.dp), // Increased vertical padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logout_ic),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp) // Slightly larger icon size
                        )
                        Spacer(modifier = Modifier.width(5.dp)) // Increased space between icon and text
                        Text(
                            text = "Logout",
                            fontSize = 20.sp, // Larger font size for menu items
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click for menu item */ }
            .padding(vertical = 16.dp), // Increased vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp) // Slightly larger icon size
        )
        Spacer(modifier = Modifier.width(20.dp)) // Increased space between icon and text
        Text(
            text = title,
            fontSize = 20.sp, // Larger font size for menu items
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun MenuItem2(title: String, image: Int? = null, photoUrl: Uri? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click for menu item */ }
            .padding(vertical = 16.dp), // Increased vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // If photoUrl is available, display the profile picture, else use default image
        if (photoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // Placeholder background
            )
        } else {
            Image(
                painter = painterResource(image ?: R.drawable.ellipse_78),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // Placeholder background
            )
        }

        Spacer(modifier = Modifier.width(20.dp)) // Increased space between icon and text
        Text(
            text = title,
            fontSize = 20.sp, // Larger font size for menu items
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun FunOption() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent) // Background color of the section
            .padding(16.dp)
    ) {
        Text(
            text = "Fun Things To Do",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ActionItem(
            title = "Lyrics",
            description = "Generate lyrics of a song for any music genre.",
            icon = R.drawable.music_notes // Replace with your music icon drawable
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionItem(
            title = "Reply Writer",
            description = "Write an awesome reply to messages, emails and more.",
            icon = R.drawable.pen_new_square // Replace with your reply icon drawable
        )
    }
}

@Composable
fun ActionItem(title: String, description: String, icon: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161B22), shape = RoundedCornerShape(12.dp)) // Card background color
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = Color(0xFF9AA0A6), // Description text color
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ProfilePicture(photoUrl: Uri?) {
    if (photoUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(photoUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
                .clip(CircleShape) // Ensure it's circular
                .background(Color.Gray) // Placeholder background
        )
    } else {
        // Fallback in case photoUrl is null
        Image(
            painter = painterResource(id = R.drawable.ellipse_78),
            contentDescription = "Default Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
                .clip(CircleShape)
        )
    }
}
