package com.tvisha.imageviewer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items


import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.tvisha.imageviewer.R
import com.tvisha.imageviewer.TAG
import com.tvisha.imageviewer.ui.theme.ImageViewerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageViewerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        }
    ) { index ->
        itemContent(items[index])
    }
}

@SuppressLint("BanParcelableUsage")
private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}

@Composable
fun MainScreen() {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val mainViewmodel: MainViewmodel = viewModel()

    val photos = mainViewmodel.getPhotosPagingFlow().collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        content = {
            this.items(
                photos
            ) { photo ->
                photo?.let {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var isImageLoading by remember { mutableStateOf(false) }

//                        val painter = rememberAsyncImagePainter(
//                            model = photo.url,
//                        )

//                        isImageLoading = when (painter.state) {
//                            is AsyncImagePainter.State.Loading -> true
//                            else -> false
//                        }

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            if(photo.localPath.isBlank()){
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                        .height(screenHeight / 4)
                                        .width(screenWidth / 2),
                                    color = MaterialTheme.colors.primary,
                                )
                            }else {
                                ImageListItem(
                                    context = LocalContext.current,
                                    fileName = photo.localPath,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                        .height(screenHeight / 4)
                                        .width(screenWidth / 2)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
//                            LoadNetworkImage(url = photo.url, modifier = Modifier
//                                .padding(horizontal = 6.dp, vertical = 3.dp)
//                                .height(screenHeight / 4)
//                                .width(screenWidth / 2)
//                                .clip(RoundedCornerShape(8.dp)),)
//                            Image(
//                                modifier = Modifier
//                                    .padding(horizontal = 6.dp, vertical = 3.dp)
//                                    .height(screenHeight / 4)
//                                    .width(screenWidth / 2)
//                                    .clip(RoundedCornerShape(8.dp)),
//                                painter = painter,
//                                contentDescription = "Poster Image",
//                                contentScale = ContentScale.FillBounds,
//                            )

//                            if (isImageLoading) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier
//                                        .padding(horizontal = 6.dp, vertical = 3.dp),
//                                    color = MaterialTheme.colors.primary,
//                                )
//                            }
                        }
                    }
                }
            }

            val loadState = photos.loadState.mediator
            item {
                if (loadState?.refresh == LoadState.Loading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(8.dp),
                            text = "Refresh Loading"
                        )

                        CircularProgressIndicator(color = MaterialTheme.colors.primary)
                    }
                }

                if (loadState?.append == LoadState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colors.primary)
                    }
                }

                if (loadState?.refresh is LoadState.Error || loadState?.append is LoadState.Error) {
                    val isPaginatingError =
                        (loadState.append is LoadState.Error) || photos.itemCount > 1
                    val error = if (loadState.append is LoadState.Error)
                        (loadState.append as LoadState.Error).error
                    else
                        (loadState.refresh as LoadState.Error).error

                    val modifier = if (isPaginatingError) {
                        Modifier.padding(8.dp)
                    } else {
                        Modifier.fillMaxSize()
                    }
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (!isPaginatingError) {
                            Icon(
                                modifier = Modifier
                                    .size(64.dp),
                                imageVector = Icons.Rounded.Warning, contentDescription = null
                            )
                        }

                        Text(
                            modifier = Modifier
                                .padding(8.dp),
                            text = error.message ?: error.toString(),
                            textAlign = TextAlign.Center,
                        )

                        Button(
                            onClick = {
                                photos.refresh()
                            },
                            content = {
                                Text(text = "Refresh")
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = Color.White,
                            )
                        )
                    }
                }
            }
        }
    )
}

fun loadImageFromInternalStorage(context: Context, fileName: String): Bitmap? {
    return try {
        val fis: FileInputStream = context.openFileInput(fileName)
        val bitmap = BitmapFactory.decodeStream(fis)
        fis.close()
        Log.d("TAG", "Image loaded from internal storage: $fileName")
        bitmap
    } catch (e: FileNotFoundException) {
        Log.e("TAG", "File not found: " + e.message)
        null
    } catch (e: java.lang.Exception) {
        Log.e("TAG", "Error loading image from internal storage: " + e.message)
        null
    }
}

@Composable
fun ImageListItem(context: Context, fileName: String, modifier: Modifier) {
    val bitmap = loadImageFromInternalStorage(context =context, fileName = fileName)
    bitmap?.let {
        val painter: Painter = BitmapPainter(bitmap.asImageBitmap())
        Image(
            painter = painter,
            contentDescription = null, // Provide proper content description
            contentScale = ContentScale.Crop,
            modifier = modifier // Adjust size as needed
        )
    }
}


@Composable
fun LoadNetworkImage(url: String, modifier: Modifier = Modifier) {
    var image by remember { mutableStateOf<Bitmap?>(null) }

    // Load the image using a coroutine
    LaunchedEffect(url) {
        val inputStream = fetchImage(url)
        Log.d("imdfg", "out $inputStream")

        inputStream?.let {
            Log.d("imdfg", "success")

            image =  BitmapFactory.decodeStream(it)


        }
    }

    // Display the image if loaded, otherwise display a placeholder or error image
    image?.let {
        Log.d("imdfg", "show $it")

        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null, // Content description for accessibility, set to null for now
            modifier = modifier,
        )
    } ?: run {
        // Placeholder or error image
        Box(
            modifier = modifier,
        ) {
            Image(
                painterResource(R.drawable.default_pic),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Function to fetch image from URL
private suspend fun fetchImage(urlString: String): InputStream? {
   return withContext(Dispatchers.IO) {
         try {


            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000 // Set your desired timeout
            connection.readTimeout = 30000 // Set your desired timeout
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("imdfg", "$urlString")

                connection.inputStream
            } else {
                Log.d("imdfg", "null $urlString")

                null
            }

        } catch (e: Exception) {
            Log.d("imdfg", e.toString())
            null
        }
    }

}

// Custom NetworkImage class to handle InputStream
//private class NetworkImage(private val inputStream: InputStream) : Painter() {
//    override val intrinsicSize: android.graphics.Size
//        get() = android.graphics.Size(0, 0)
//
//    override fun draw(
//        canvas: androidx.compose.ui.graphics.Canvas,
//        size: androidx.compose.ui.geometry.Size
//    ) {
//        // Draw the image on the canvas
//        // You need to implement this based on the input stream and canvas APIs
//        // This is just a placeholder implementation
//    }
//}

