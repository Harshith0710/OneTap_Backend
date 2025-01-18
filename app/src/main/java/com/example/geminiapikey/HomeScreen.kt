package com.example.geminiapikey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    val screenWidth = configuration.screenWidthDp.dp
    val buttonWidth = screenWidth / 2
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF101010), Color(0xFF282828))
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            Image(
                painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_2),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ellipse_78),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hi, Mukesh 👋",
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "How may I help you today?",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
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
                ActionButton("Chat with Bot", R.drawable.frame_5, Color(0xFFB39DDB), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth / 2))
                Spacer(Modifier.height(4.dp))
                ActionButton("Search by Image", R.drawable.frame_5__1_, Color(0xFFA5D6A7), onActionClick,
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
                ActionButton("Talk with Bot", R.drawable.frame_5__2_, Color(0xFFF48FB1), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth + 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Fun Things To Do",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FunOption("Lyrics", "Generate lyrics of a song for any music genre.")
            FunOption("Reply Writer", "Write an awesome reply to messages, emails and more.")
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    image: Int,
    color: Color,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, shape = RoundedCornerShape(16.dp))
            .clickable { onActionClick(text) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
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
                        .padding(24.dp)
                ) {
                    // Title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "QuickTap AI",
                            fontSize = 28.sp, // Larger font size for the title
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // Menu items
                    MenuItem("Home", Icons.Default.Home)
                    MenuItem("Privacy Policy", Icons.Default.Lock)

                    Spacer(modifier = Modifier.weight(1f)) // Pushes the "Logout" button to the bottom

                    // Logout button with styling
                    Text(
                        text = "Logout",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(
                                onClick = { /* Handle logout */ }
                            )
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 32.dp, vertical = 12.dp) // Padding inside the button
                    )
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
fun FunOption(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}
